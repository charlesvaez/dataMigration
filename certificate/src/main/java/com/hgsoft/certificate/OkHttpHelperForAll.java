package com.hgsoft.certificate;

import okhttp3.*;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class OkHttpHelperForAll {
    private OkHttpClient okHttpClient;
    private RequestBody requestBody;
    private Request request;
    private String url;
    private String method = "get";

    private volatile static OkHttpHelperForAll instance;

    public static final int HTTP_SUCCESS = 1;
    public static final int HTTP_FAILURE = 0;

    public static class ReqMethod {
        public static String POST = "post";
        public static String GET = "get";
    }

    public OkHttpHelperForAll() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10000, TimeUnit.MILLISECONDS);
        builder.readTimeout(10000, TimeUnit.MILLISECONDS);

        X509TrustManager trustManager;
        SSLSocketFactory sslSocketFactory;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        builder.sslSocketFactory(sslSocketFactory);
        builder.hostnameVerifier(new TrustAllHostnameVerifier());
        okHttpClient = builder.build();
    }

    public static OkHttpHelperForAll newInstance(){
        if (instance == null){
            synchronized (OkHttpHelperForAll.class){
                if (instance == null){
                    instance = new OkHttpHelperForAll();
                }
            }
        }
        return instance;
    }

    public OkHttpHelperForAll addRequestBody(RequestBody body){
        this.requestBody = body;
        return this;
    }

    public OkHttpHelperForAll addUrl(String url){
        this.url = url;
        return this;
    }

    public OkHttpHelperForAll setMethod(String method) {
        this.method = method;
        return this;
    }


    public void start(){
        if (method.equals(ReqMethod.POST)){
            if (requestBody==null){
                return;
            }
            request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept","application/json; charset=utf-8")
                    .post(requestBody)
                    .tag(url)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(url)
                    .tag(url)
                    .build();
        }

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                final String rep = response.body().string();
                System.out.println("onResponse: "+rep);
            }
        });
    }

    private InputStream trustedCertificatesInputStream(String certificatePath) {
        InputStream inputStream = null;
        try {
            inputStream =  new FileInputStream(certificatePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    private X509TrustManager trustManagerForCertificates(InputStream in)
            throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        char[] password = "123456".toCharArray();
        // Put the certificates a key store.
        KeyStore keyStore = newEmptyKeyStore(password);

        int index = 0;
        for (Certificate certificate : certificates) {
            System.out.println("trustManagerForCertificates: "+certificate.toString());
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }
        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream in = null;
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    //信任所有的服务器,返回true
    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }


}
