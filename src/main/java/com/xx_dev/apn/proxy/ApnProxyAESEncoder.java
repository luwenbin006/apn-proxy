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
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxyAESEncoder 14-6-28 12:09 (xmx) Exp $
 */
public class ApnProxyAESEncoder extends MessageToByteEncoder<ByteBuf> {

    private static final Logger logger = Logger.getLogger(ApnProxyAESEncoder.class);

    Cipher c1;
    Key securekey;
    IvParameterSpec iv;

    public ApnProxyAESEncoder(byte[] key, byte[] iv) {
        this.securekey = new SecretKeySpec(key, "AES");
        try {
            c1 = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        this.iv = new IvParameterSpec(iv);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        try {

            while(msg.readableBytes() > 0) {
                c1.init(Cipher.ENCRYPT_MODE, securekey, iv);

                int readLength = msg.readableBytes();
                if (readLength > 1024*512) {
                    readLength = 1024*512;
                }

                byte[] array = new byte[readLength];
                msg.readBytes(array, 0, readLength);

                byte[] raw = c1.doFinal(array);
                int length = raw.length;

                out.writeInt(0x34ed2b11);//magic number
                out.writeInt(length);
                out.writeBytes(raw);
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
