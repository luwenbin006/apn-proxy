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

package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.remotechooser.ApnProxyRemote;
import com.xx_dev.apn.proxy.remotechooser.ApnProxyRemoteChooser;
import com.xx_dev.apn.proxy.utils.HostNamePortUtil;
import com.xx_dev.apn.proxy.utils.LoggerUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxySchemaHandler 14-1-8 16:13 (xmx) Exp $
 */
public class ApnProxySchemaHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(ApnProxySchemaHandler.class);

    public static final String HANDLER_NAME = "apnproxy.schema";

    @Override
    public void channelRead(ChannelHandlerContext uaChannelCtx, final Object msg) throws Exception {

        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;

            String originalHost = HostNamePortUtil.getHostName(httpRequest);
            int originalPort = HostNamePortUtil.getPort(httpRequest);

            ApnProxyRemote apnProxyRemote = ApnProxyRemoteChooser.chooseRemoteAddr(
                    originalHost, originalPort);

            Channel uaChannel = uaChannelCtx.channel();

            ApnProxyConnectionAttribute apnProxyConnectionAttribute = ApnProxyConnectionAttribute.build(
                    uaChannel.remoteAddress().toString(), httpRequest.getMethod().name(),
                    httpRequest.getUri(), httpRequest.getProtocolVersion().text(),
                    httpRequest.headers().get(HttpHeaders.Names.USER_AGENT), apnProxyRemote);

            uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY).set(apnProxyConnectionAttribute);
            uaChannel.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY).set(apnProxyConnectionAttribute);

            if (httpRequest.getMethod().equals(HttpMethod.CONNECT)) {
                if (uaChannelCtx.pipeline().get(ApnProxyUserAgentForwardHandler.HANDLER_NAME) != null) {
                    uaChannelCtx.pipeline().remove(ApnProxyUserAgentForwardHandler.HANDLER_NAME);
                }
                if (uaChannelCtx.pipeline().get(ApnProxyUserAgentTunnelHandler.HANDLER_NAME) == null) {
                    uaChannelCtx.pipeline().addLast(ApnProxyUserAgentTunnelHandler.HANDLER_NAME, new ApnProxyUserAgentTunnelHandler());
                }
            } else {
                if (uaChannelCtx.pipeline().get(ApnProxyUserAgentForwardHandler.HANDLER_NAME) == null) {
                    uaChannelCtx.pipeline().addLast(ApnProxyUserAgentForwardHandler.HANDLER_NAME, new ApnProxyUserAgentForwardHandler());
                }
            }
        }

        LoggerUtil.debug(logger, uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY), "UA msg", msg);

        uaChannelCtx.fireChannelRead(msg);
    }
}
