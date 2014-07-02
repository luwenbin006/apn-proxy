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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxyAESEncoder 14-6-28 12:09 (xmx) Exp $
 */
public class ApnProxyAESEncoder extends MessageToByteEncoder<ByteBuf> {

    private static final Logger logger = Logger.getLogger(ApnProxyAESEncoder.class);

    private byte[] key;
    private byte[] iv;

    public ApnProxyAESEncoder(byte[] key, byte[] iv) {
        this.key = key;
        this.iv = iv;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        try {
            Key securekey = new SecretKeySpec(key, "AES");
            Cipher c1 = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c1.init(Cipher.ENCRYPT_MODE, securekey, new IvParameterSpec(iv));
            byte[] array = new byte[msg.readableBytes()];
            msg.readBytes(array);
            byte[] raw = c1.doFinal(array);
            int length = raw.length;
            out.writeInt(length);
            out.writeBytes(raw);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
