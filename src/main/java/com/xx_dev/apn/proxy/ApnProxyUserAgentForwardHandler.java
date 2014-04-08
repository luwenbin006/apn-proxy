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

import com.xx_dev.apn.proxy.ApnProxyRemoteHandler.RemoteChannelInactiveCallback;
import com.xx_dev.apn.proxy.remotechooser.ApnProxyRemote;
import com.xx_dev.apn.proxy.remotechooser.ApnProxyRemoteChooser;
import com.xx_dev.apn.proxy.utils.Base64;
import com.xx_dev.apn.proxy.utils.HostNamePortUtil;
import com.xx_dev.apn.proxy.utils.HttpErrorUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxyUserAgentForwardHandler 14-1-8 16:13 (xmx) Exp $
 */
public class ApnProxyUserAgentForwardHandler extends ChannelInboundHandlerAdapter implements RemoteChannelInactiveCallback{

    private static final Logger logger = Logger.getLogger(ApnProxyUserAgentForwardHandler.class);

    public static final String HANDLER_NAME = "apnproxy.useragent.forward";

    private String remoteAddr;

    private Map<String, Channel> remoteChannelMap = new HashMap<String, Channel>();

    private List<HttpContent> httpContentBuffer = new ArrayList<HttpContent>();

    @Override
    public void channelRead(final ChannelHandlerContext uaChannelCtx, final Object msg) throws Exception {

        final Channel uaChannel = uaChannelCtx.channel();

        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;

            String originalHostHeader = httpRequest.headers().get(HttpHeaders.Names.HOST);
            final String originalHost = HostNamePortUtil.getHostName(originalHostHeader);
            final int originalPort = HostNamePortUtil.getPort(originalHostHeader, 80);

            final ApnProxyRemote apnProxyRemote = ApnProxyRemoteChooser.chooseRemoteAddr(
                    originalHost, originalPort);
            remoteAddr = apnProxyRemote.getRemote();

            uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY).set(
                    ApnProxyConnectionAttribute.build(uaChannelCtx.channel().remoteAddress().toString(),
                            originalHost, originalPort, remoteAddr));

            Channel remoteChannel = remoteChannelMap.get(remoteAddr);

            if (remoteChannel != null && remoteChannel.isActive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Use old remote channel : " + uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY));
                }
                HttpRequest request = constructRequestForProxy(httpRequest, apnProxyRemote);
                remoteChannel.writeAndFlush(request);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Create new remote channel: " + uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY));
                }

                Bootstrap bootstrap = new Bootstrap();
                bootstrap
                        .group(uaChannel.eventLoop())
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .option(ChannelOption.AUTO_READ, false)
                        .handler(
                                new ApnProxyRemoteChannelInitializer(apnProxyRemote, uaChannelCtx, this));

                // set local address
                if (StringUtils.isNotBlank(ApnProxyLocalAddressChooser.choose(apnProxyRemote
                        .getRemoteHost()))) {
                    bootstrap.localAddress(new InetSocketAddress((ApnProxyLocalAddressChooser
                            .choose(apnProxyRemote.getRemoteHost())), 0));
                }

                ChannelFuture remoteConnectFuture = bootstrap.connect(
                        apnProxyRemote.getRemoteHost(), apnProxyRemote.getRemotePort());

                remoteChannel = remoteConnectFuture.channel();
                remoteChannelMap.put(remoteAddr, remoteChannel);

                remoteConnectFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            future.channel().write(
                                    constructRequestForProxy((HttpRequest) msg, apnProxyRemote));

                            for (HttpContent hc : httpContentBuffer) {
                                future.channel().writeAndFlush(hc);

                                if (hc instanceof LastHttpContent) {
                                    future.channel().writeAndFlush(Unpooled.EMPTY_BUFFER)
                                            .addListener(new ChannelFutureListener() {
                                                @Override
                                                public void operationComplete(ChannelFuture future)
                                                        throws Exception {
                                                    if (future.isSuccess()) {
                                                        future.channel().read();
                                                    }

                                                }
                                            });
                                }
                            }
                            httpContentBuffer.clear();
                        } else {
                            String errorMsg = "remote connect to " + remoteAddr + " fail";
                            logger.error(errorMsg);
                            // send error response
                            HttpMessage errorResponseMsg = HttpErrorUtil.buildHttpErrorMessage(
                                    HttpResponseStatus.INTERNAL_SERVER_ERROR, errorMsg);
                            uaChannel.writeAndFlush(errorResponseMsg);
                            httpContentBuffer.clear();

                            future.channel().close();
                        }
                    }
                });

            }
            ReferenceCountUtil.release(msg);
        } else {
            Channel remoteChannel = remoteChannelMap.get(remoteAddr);

            HttpContent hc = ((HttpContent) msg);
            //hc.retain();

            //HttpContent _hc = hc.copy();

            if (remoteChannel != null && remoteChannel.isActive()) {
                remoteChannel.writeAndFlush(hc);

                if (hc instanceof LastHttpContent) {
                    remoteChannel.writeAndFlush(Unpooled.EMPTY_BUFFER)
                            .addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future)
                                        throws Exception {
                                    if (future.isSuccess()) {
                                        future.channel().read();
                                    }

                                }
                            });
                }
            } else {
                httpContentBuffer.add(hc);
            }
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext uaChannelCtx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("UA channel: inactive" + uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY));
        }
        for (Map.Entry<String, Channel> entry : remoteChannelMap.entrySet()) {
            final Channel remoteChannel = entry.getValue();
            remoteChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(
                    new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            remoteChannel.close();
                        }
                    });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext uaChannelCtx, Throwable cause) throws Exception {
        logger.error(cause.getMessage() + " " + uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY), cause);
        uaChannelCtx.close();
    }

    @Override
    public void remoteChannelInactive(final ChannelHandlerContext uaChannelCtx,
                                              final String inactiveRemoteAddr)
            throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Remote channel inactive, and flush end: " + uaChannelCtx.attr(ApnProxyConnectionAttribute.ATTRIBUTE_KEY));
        }

        remoteChannelMap.remove(inactiveRemoteAddr);

        if (uaChannelCtx.channel().isActive()) {
            uaChannelCtx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER);
        }



    }

    private HttpRequest constructRequestForProxy(HttpRequest httpRequest,
                                                 ApnProxyRemote apnProxyRemote) {

        String uri = httpRequest.getUri();

        if (!apnProxyRemote.isAppleyRemoteRule()) {
            uri = this.getPartialUrl(uri);
        }

        HttpRequest _httpRequest = new DefaultHttpRequest(httpRequest.getProtocolVersion(),
                httpRequest.getMethod(), uri);

        Set<String> headerNames = httpRequest.headers().names();
        for (String headerName : headerNames) {
            if (StringUtils.equalsIgnoreCase(headerName, "Proxy-Connection")) {
                continue;
            }

            if (StringUtils.equalsIgnoreCase(headerName, "Pragma")) {
                continue;
            }

            // if (StringUtils.equalsIgnoreCase(headerName, HttpHeaders.Names.CONNECTION)) {
            // continue;
            // }

            _httpRequest.headers().add(headerName, httpRequest.headers().getAll(headerName));
        }

        _httpRequest.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        // _httpRequest.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.IDENTITY);

        if (StringUtils.isNotBlank(apnProxyRemote.getProxyUserName())
                && StringUtils.isNotBlank(apnProxyRemote.getProxyPassword())) {
            String proxyAuthorization = apnProxyRemote.getProxyUserName() + ":"
                    + apnProxyRemote.getProxyPassword();
            try {
                _httpRequest.headers().set("Proxy-Authorization",
                        "Basic " + Base64.encodeBase64String(proxyAuthorization.getBytes("UTF-8")));
            } catch (UnsupportedEncodingException e) {
            }

        }

        return _httpRequest;
    }

    private String getPartialUrl(String fullUrl) {
        if (StringUtils.startsWith(fullUrl, "http")) {
            int idx = StringUtils.indexOf(fullUrl, "/", 7);
            return idx == -1 ? "/" : StringUtils.substring(fullUrl, idx);
        }

        return fullUrl;
    }

}