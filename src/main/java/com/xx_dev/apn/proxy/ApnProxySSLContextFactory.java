package com.xx_dev.apn.proxy;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApnProxySSLContextFactory.java, v 0.1 2013-3-26 上午11:22:10 xmx Exp $
 */
public class ApnProxySSLContextFactory {

    private static Logger     logger     = Logger.getLogger(ApnProxySSLContextFactory.class);

    private static SSLContext sslcontext = null;

    static {

        try {
            sslcontext = SSLContext.getInstance("TLS");

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

            KeyStore ks = KeyStore.getInstance("JKS");
            KeyStore tks = KeyStore.getInstance("JKS");

            String keyStorePath = ApnProxyConfig.getStringConfig("apn.proxy.key_store");
            String keyStorePassword = ApnProxyConfig.getStringConfig("apn.proxy.key_store_password");

            ks.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
            tks.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());

            String keyPassword = ApnProxyConfig.getStringConfig("apn.proxy.key_store_password");
            kmf.init(ks, keyPassword.toCharArray());
            tmf.init(tks);

            sslcontext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    public static SSLContext getSSLContext() {
        return sslcontext;
    }

}