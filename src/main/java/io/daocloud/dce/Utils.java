package io.daocloud.dce;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;

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
            builder = builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }
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
