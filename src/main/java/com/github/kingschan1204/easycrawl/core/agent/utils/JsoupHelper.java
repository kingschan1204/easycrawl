package com.github.kingschan1204.easycrawl.core.agent.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.net.ssl.*;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * jsoup 通用工具
 *
 * @author kings.chan
 * @create 2019-03-28 10:24
 **/
@Slf4j
public class JsoupHelper {

    /**
     * jsoup 通用请求方法
     *
     * @param pageUrl  url
     * @param method   方法
     * @param timeOut  超时时间单位毫秒
     * @param useAgent 请求头
     * @param referer  来源url
     * @return
     */
    public static WebPage request(String pageUrl, Connection.Method method,
                                  Integer timeOut, String useAgent, String referer) {
        return request(
                pageUrl, method,
                timeOut, useAgent, referer, null,
                null, null,
                true, true);
    }


    /**
     * jsoup 通用请求方法
     *
     * @param pageUrl           url
     * @param method            方法
     * @param timeOut           超时时间单位毫秒
     * @param useAgent          请求头
     * @param referer           来源url
     * @param heads             http head头
     * @param cookie            cookie
     * @param proxy             是否使用代理
     * @param ignoreContentType 是否忽略内容类型
     * @param ignoreHttpErrors  是否忽略http错误
     * @return
     */
    public static WebPage request(String pageUrl, Connection.Method method,
                                  Integer timeOut, String useAgent, String referer,
                                  Map<String, String> heads,
                                  Map<String, String> cookie, Proxy proxy,
                                  Boolean ignoreContentType, Boolean ignoreHttpErrors) {
        long start = System.currentTimeMillis();
        WebPage webPage = null;
        Connection.Response response = null;
        try {
            log.debug(pageUrl);
            if (pageUrl.contains("https")) {
                trustAllHttpsCertificates();
                HttpsURLConnection.setDefaultHostnameVerifier(hv);
            }
            Connection connection = Jsoup.connect(pageUrl)
                    .timeout(null == timeOut ? 8000 : timeOut)
                    .method(null == method ? Connection.Method.GET : method)
                    .maxBodySize(0);//默认是1M，设置0 则为无限制
            if (null != useAgent) {
                connection.userAgent(useAgent);
            }
            if (null != ignoreContentType) {
                connection.ignoreContentType(ignoreContentType);
            }
            if (null != ignoreHttpErrors) {
                connection.ignoreHttpErrors(ignoreHttpErrors);
            }
            if (null != referer) {
                connection.referrer(referer);
            }
            if (null != proxy) {
                connection.proxy(proxy);
            }
            if (null != cookie) {
                connection.cookies(cookie);
            }
            if (null != heads) {
                connection.headers(heads);
            }
            response = connection.execute();
            webPage = new WebPage(System.currentTimeMillis() - start, response.statusCode(), response.statusMessage(), response.charset(), response.contentType(), response.body(), response.cookies());
            return webPage;
        } catch (SocketTimeoutException ex) {
            log.error("【网络超时】 {}\r\n超时时间：{}", pageUrl, System.currentTimeMillis() - start);
            return new WebPage(System.currentTimeMillis() - start, 408, null, "", null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("crawlPage {} {}", pageUrl, e);
            return new WebPage(System.currentTimeMillis() - start, null, null, null, null, null, null);
        }
    }

    static HostnameVerifier hv = new HostnameVerifier() {
        public boolean verify(String urlHostName, SSLSession session) {
//            log.warn("Warning: URL Host: {}  vs. {}", urlHostName, session.getPeerHost());
            return true;
        }
    };

    private static void trustAllHttpsCertificates() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    static class miTM implements TrustManager, X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
        }
    }

    /**
     * 启用ssl
     *
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     */
    /*static void enableSSLSocket() throws KeyManagementException, NoSuchAlgorithmException {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }*/
}