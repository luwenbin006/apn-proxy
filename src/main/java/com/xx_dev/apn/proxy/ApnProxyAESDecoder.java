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
import io.netty.handler.codec.ReplayingDecoder;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxyAESDecoder 14-6-28 12:09 (xmx) Exp $
 */
public class ApnProxyAESDecoder extends ReplayingDecoder<ApnProxyAESDecoder.STATE> {

    enum STATE {
        READ_MAGIC_NUMBER,
        READ_LENGTH,
        READ_CONTENT
    }

    private int length;

    Cipher c1;
    Key securekey;
    IvParameterSpec iv;

    public ApnProxyAESDecoder(byte[] key, byte[] iv) {
        super(STATE.READ_MAGIC_NUMBER);
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
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (this.state()) {
            case READ_MAGIC_NUMBER: {
                int magicNumber = in.readInt();
                if (magicNumber != 0x34ed2b11) {
                    throw new Exception("Wrong magic number!");
                }
                this.checkpoint(STATE.READ_LENGTH);
            }
            case READ_LENGTH: {
                length = in.readInt();
                if (length > 3000) {
                    ctx.close();
                }
                this.checkpoint(STATE.READ_CONTENT);
            }
            case READ_CONTENT: {
                c1.init(Cipher.DECRYPT_MODE, securekey, iv);
                byte[] data = new byte[length];
                in.readBytes(data, 0, length);
                byte[] raw = c1.doFinal(data);
                ByteBuf outBuf = ctx.alloc().buffer();
                outBuf.writeBytes(raw);
                out.add(outBuf);
                this.checkpoint(STATE.READ_MAGIC_NUMBER);
                break;
            }
            default:
                throw new Error("Shouldn't reach here.");
        }


    }


}
