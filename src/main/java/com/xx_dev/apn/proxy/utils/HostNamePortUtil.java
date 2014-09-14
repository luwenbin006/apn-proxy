/*
 * Copyright (c) 2014 The APN-PROXY Project
 *
 * The APN-PROXY Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.xx_dev.apn.proxy.utils;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.utils.HostNamePortUtil 14-1-8 16:13 (xmx) Exp $
 */
public class HostNamePortUtil {

    private static final Logger logger = Logger.getLogger(HostNamePortUtil.class);

    public static String getHostName(HttpRequest httpRequest) {
        String originalHostHeader = httpRequest.headers().get(HttpHeaders.Names.HOST);
        if (StringUtils.isNotBlank(originalHostHeader)) {
            String originalHost = StringUtils.split(originalHostHeader, ": ")[0];
            return originalHost;
        } else {
            String uriStr = httpRequest.getUri();
            try {
                URI uri = new URI(uriStr);

                String originalHost = uri.getHost();

                return originalHost;
            } catch (URISyntaxException e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    }

    public static int getPort(HttpRequest httpRequest) {
        int originalPort = 80;

        if (httpRequest.getMethod().equals(HttpMethod.CONNECT)) {
            originalPort = 443;
        }

        String originalHostHeader = httpRequest.headers().get(HttpHeaders.Names.HOST);
        if (StringUtils.isNotBlank(originalHostHeader)) {
            if (StringUtils.split(originalHostHeader, ": ").length == 2) {
                originalPort = Integer.parseInt(StringUtils.split(originalHostHeader, ": ")[1]);
            }
        } else {
            String uriStr = httpRequest.getUri();
            try {
                URI uri = new URI(uriStr);

                if (uri.getPort() > 0) {
                    originalPort = uri.getPort();
                }
            } catch (URISyntaxException e) {
                logger.error(e.getMessage(), e);
                originalPort = -1;
            }
        }


        return originalPort;
    }

}
