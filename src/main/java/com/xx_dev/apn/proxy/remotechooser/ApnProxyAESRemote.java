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

package com.xx_dev.apn.proxy.remotechooser;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.remotechooser.ApnProxyAESRemote 2014-06-29 11:04 (xmx) Exp $
 */
public class ApnProxyAESRemote extends ApnProxyRemote{

    private byte[] key;

    private byte[] iv;

    final public byte[] getKey() {
        return key;
    }

    final public void setKey(byte[] key) {
        this.key = key;
    }

    final public byte[] getIv() {
        return iv;
    }

    final public void setIv(byte[] iv) {
        this.iv = iv;
    }

}
