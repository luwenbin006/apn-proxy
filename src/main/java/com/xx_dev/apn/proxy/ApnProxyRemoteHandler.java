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

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxyRemoteHandler 14-1-8 16:13 (xmx) Exp $
 */
public class ApnProxyRemoteHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(ApnProxyRemoteHandler.class);

    public static final String HANDLER_NAME = "apnproxy.proxy.remote";

    private ChannelHandlerContext uaChannelCtx;

    private RemoteChannelInactiveCallback remoteChannelInactiveCallback;

    private int remainMsgCount = 0;

    public ApnProxyRemoteHandler(ChannelHandlerContext uaChannelCtx,
                                 RemoteChannelInactiveCallback remoteChannelInactiveCallback) {
        this.uaChannelCtx = uaChannelCtx;
        this.remoteChannelInactiveCallback = remoteChannelInactiveCallback;
    }

    @Override
    public void channelActive(ChannelHandlerContext remoteChannelCtx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Remote channel active, " + uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY));
        }
        remoteChannelCtx.read();
    }

    public void channelRead(final ChannelHandlerContext remoteChannelCtx, final Object msg) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Remote msg: " + msg + ", " + uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY));
        }

        remainMsgCount++;

        if (remainMsgCount <= 5) {
            remoteChannelCtx.read();
        }


        HttpObject ho = (HttpObject) msg;


        if (ho instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) ho;
            httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            httpResponse.headers().set("Proxy-Connection", HttpHeaders.Values.KEEP_ALIVE);
        }

        if (uaChannelCtx.channel().isActive()) {
            uaChannelCtx.writeAndFlush(ho).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Write to UA finished: " + future.isSuccess() +", " + uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY));
                    }
                    if (future.isSuccess()) {
                        remainMsgCount --;
                        remoteChannelCtx.read();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Fire read again" +", " + uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY));
                        }
                    } else {
                        remoteChannelCtx.close();
                    }
                }
            });
        } else {
            remoteChannelCtx.close();
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext remoteChannelCtx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Remote channel inactive, " + uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY));
        }

        final String remoteAddr = uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY).get().getRemoteAddr();

        remoteChannelInactiveCallback.remoteChannelInactive(uaChannelCtx, remoteAddr);

        remoteChannelCtx.fireChannelInactive();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext remoteChannelCtx, Throwable cause) throws Exception {
        logger.error(cause.getMessage() + uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY), cause);
        remoteChannelCtx.close();
    }

    public interface RemoteChannelInactiveCallback {
        public void remoteChannelInactive(ChannelHandlerContext uaChannelCtx,
                                                  String remoeAddr) throws Exception;
    }

}
