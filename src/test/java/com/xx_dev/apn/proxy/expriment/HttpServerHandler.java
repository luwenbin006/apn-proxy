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

package com.xx_dev.apn.proxy.expriment;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.expriment.HttpServerHandler 14-1-8 16:13 (xmx) Exp $
 */
public class HttpServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof LastHttpContent) {

            DefaultHttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            httpResponse.headers().add(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);

            ctx.write(httpResponse);

            String s = "1234567890";

            for (int i = 0; i < 100000; i++) {
                DefaultHttpContent c1 = new DefaultHttpContent(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8));

                ctx.writeAndFlush(c1);
            }

            DefaultLastHttpContent c3 = new DefaultLastHttpContent();

            ctx.writeAndFlush(c3);
        }

        ReferenceCountUtil.release(msg);
    }
}
