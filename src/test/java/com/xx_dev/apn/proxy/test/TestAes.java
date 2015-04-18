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

package com.xx_dev.apn.proxy.test;

import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.test.TestAes 2015-03-09 14:18 (xmx) Exp $
 */
public class TestAes {

    @Test
    public void test() {
        try {
            Key securekey = new SecretKeySpec("fuckgfw123456789".getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec("1234567890123456".getBytes());

            Cipher c1 = Cipher.getInstance("AES/CFB/NoPadding");
            c1.init(Cipher.ENCRYPT_MODE, securekey, iv);
            byte[] raw = c1.doFinal(new byte[] { 1, 2, 3 });

            Cipher c2 = Cipher.getInstance("AES/CFB/NoPadding");
            c2.init(Cipher.DECRYPT_MODE, securekey, iv);
            byte[] orig = c2.doFinal(raw);


            byte[] orig2 = c2.doFinal(new byte[]{raw[0]});

            System.out.println(orig2);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

    }
}
