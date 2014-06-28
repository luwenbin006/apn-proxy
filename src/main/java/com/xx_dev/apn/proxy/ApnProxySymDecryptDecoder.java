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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.List;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxySymDecryptDecoder 14-6-28 12:09 (xmx) Exp $
 */
public class ApnProxySymDecryptDecoder extends ReplayingDecoder<ApnProxySymDecryptDecoder.STATE>{

    private String key = "apn-proxy-test-sym-key";

    enum STATE {
        READ_LENGTH,
        READ_CONTENT
    }

    private int length;

    public ApnProxySymDecryptDecoder() {
        super(STATE.READ_LENGTH);
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (this.state()) {
            case READ_LENGTH : {
                length = in.readInt();
                this.checkpoint(STATE.READ_CONTENT);
            }
            case READ_CONTENT: {
                Key securekey = new SecretKeySpec(key.getBytes(Charset.forName("UTF-8")), "AES");
                Cipher c1 = Cipher.getInstance("AES/CBC/PKCS5Padding");
                c1.init(Cipher.DECRYPT_MODE, securekey);
                byte[] data = new byte[length];
                in.readBytes(data, 0, length);
                byte[] raw = c1.doFinal(data);
                ByteBuf outBuf = ctx.alloc().buffer();
                outBuf.writeBytes(raw);
                out.add(outBuf);
                this.checkpoint(STATE.READ_LENGTH);
            }
            default:
                throw new Error("Shouldn't reach here.");
        }



    }




}
