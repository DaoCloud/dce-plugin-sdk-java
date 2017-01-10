package io.daocloud.dce;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;


public class Utils {
    public static String getResponseContent(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    public static HttpClient getHTTPClient(boolean ignoreSSLVerify) {
        HttpClientBuilder builder = HttpClientBuilder.create();

        if (ignoreSSLVerify) {
            SSLContext insecureSSLContext;
            try {
                insecureSSLContext = SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {
                    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        return true;
                    }
                }).build();
            } catch (GeneralSecurityException e) {
                insecureSSLContext = null;
            }

            builder = builder.setSSLContext(insecureSSLContext).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }
        return builder.build();
    }

    public static HttpClient getUnixSocketHTTPClient(URI socketUri) {
        HttpClientBuilder builder = HttpClientBuilder.create();

        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("unix", new UnixConnectionSocketFactory(socketUri));
        Registry<ConnectionSocketFactory> registry = registryBuilder.build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);

        return builder.build();
    }

    public static void setBasicAuth(HttpRequest request, URI requestURI) {
        String userInfo = requestURI.getUserInfo();
        if (userInfo == null) {
            return;
        }
        String encodedUserInfo = Base64.encodeBase64String(userInfo.getBytes());
        request.addHeader("Authorization", "Basic " + encodedUserInfo);
    }
}
