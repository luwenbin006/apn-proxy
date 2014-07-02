package com.xx_dev.apn.proxy;

import com.xx_dev.apn.proxy.remotechooser.ApnProxyRemote;
import io.netty.util.AttributeKey;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxyConnectionAttribute 2014-04-08 14:15 (xmx) Exp $
 */
public class ApnProxyConnectionAttribute {

    public static final AttributeKey<ApnProxyConnectionAttribute> ATTRIBUTE_KEY = AttributeKey.valueOf("connection_context");

    private String uaAddress;

    private String method;

    private String url;

    private String httpVersion;

    private String ua;

    private ApnProxyRemote remote;

    private ApnProxyConnectionAttribute() {

    }

    public static ApnProxyConnectionAttribute build(String uaAddress, String method, String url, String httpVersion, String ua, ApnProxyRemote remote) {
        ApnProxyConnectionAttribute instance = new ApnProxyConnectionAttribute();

        instance.uaAddress = uaAddress;
        instance.method = method;
        instance.url = url;
        instance.httpVersion = httpVersion;
        instance.ua = ua;
        instance.remote = remote;

        return instance;
    }

    public ApnProxyRemote getRemote() {
        return this.remote;
    }

    public String toString() {
        return uaAddress + ", " + method + " " + url + " " + httpVersion + ", UA: " + ua + ", REMOTE: " + remote;
    }
}
