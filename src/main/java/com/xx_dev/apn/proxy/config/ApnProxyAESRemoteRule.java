package com.xx_dev.apn.proxy.config;

/**
 * Created by xmx on 6/29/14.
 */
public class ApnProxyAESRemoteRule extends ApnProxyRemoteRule {

    private byte[] key;

    private byte[] iv;

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }
}
