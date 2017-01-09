package io.daocloud.dce;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;


class DockerClient {
    private static final int DEFAULT_TIMEOUT_SECONDS = 60000;

    private final URI dockerHost;
    private final int timeout;

    private DockerClient(URI dockerHost, int timeout) {
        if ("tcp".equals(dockerHost.getScheme()) || "unix".equals(dockerHost.getScheme())) {
            if ("tcp".equals(dockerHost.getScheme())) {
                String dockerHostStr = dockerHost.toString();
                dockerHostStr = dockerHostStr.replace("tcp://", "http://");
                dockerHost = URI.create(dockerHostStr);
            }
            this.dockerHost = dockerHost;
            this.timeout = timeout;
        } else {
            throw new DockerClientException("Unsupported protocol scheme found: '" + dockerHost
                    + "'. Only 'tcp://' or 'unix://' supported.");
        }
    }

    DockerClient(URI dockerHost) {
        this(dockerHost, DEFAULT_TIMEOUT_SECONDS);
    }

    private RequestConfig getReqConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(this.timeout)
                .setConnectTimeout(this.timeout)
                .setSocketTimeout(this.timeout)
                .build();
    }

    private HttpClient getClient() {
        return HttpClientBuilder.create().build();
    }

    JSONObject Info() throws DockerClientException {
        HttpClient client = this.getClient();
        HttpGet req = new HttpGet(this.dockerHost + "/info");
        RequestConfig reqConfig = this.getReqConfig();
        req.setConfig(reqConfig);

        BufferedReader rd;
        try {
            HttpResponse resp = client.execute(req);
            rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
        } catch (Exception e) {
            throw new DockerClientException(e.toString(), e);
        }

        StringBuffer result = new StringBuffer();
        String line = "";

        try {
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            throw new DockerClientException(e.toString(), e);
        }

        return new JSONObject(result.toString());
    }

    JSONObject ServiceInspect(String service_id) throws DockerClientException {
        HttpClient client = this.getClient();
        HttpGet req = new HttpGet(this.dockerHost + "/services/" + service_id);
        RequestConfig reqConfig = this.getReqConfig();
        req.setConfig(reqConfig);

        BufferedReader rd;
        try {
            HttpResponse resp = client.execute(req);
            rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
        } catch (Exception e) {
            throw new DockerClientException(e.toString(), e);
        }

        StringBuffer result = new StringBuffer();
        String line = "";

        try {
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            throw new DockerClientException(e.toString(), e);
        }

        return new JSONObject(result.toString());
    }
}
