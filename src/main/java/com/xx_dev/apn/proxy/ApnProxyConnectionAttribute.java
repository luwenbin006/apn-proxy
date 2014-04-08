package com.xx_dev.apn.proxy;

import io.netty.util.AttributeKey;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxyConnectionAttribute 2014-04-08 14:15 (xmx) Exp $
 */
public class ApnProxyConnectionAttribute {

    public static final AttributeKey<ApnProxyConnectionAttribute> ATTRIBUTE_KEY = AttributeKey.valueOf("connection_context");

    private String ua;

    private String originHost;

    private int originPort;

    private String remoteAddr;

    private ApnProxyConnectionAttribute() {

    }

    public static ApnProxyConnectionAttribute build(String ua, String originHost, int originPort, String remoteAddr) {
        ApnProxyConnectionAttribute instance = new ApnProxyConnectionAttribute();
        instance.ua = ua;
        instance.originHost = originHost;
        instance.originPort = originPort;
        instance.remoteAddr = remoteAddr;


        return instance;
    }

    public String getRemoteAddr() {
        return this.remoteAddr;
    }

    public String toString() {
        return "UA: " + ua + ", ORIGIN: " + originHost + ":" + originPort + ", REMOTE: " + remoteAddr;
    }
}
