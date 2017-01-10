package io.daocloud.dce;

import java.net.URI;

import org.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;

class DockerClient {
    private static final int DEFAULT_TIMEOUT_SECONDS = 60000;

    private final URI dockerHost;
    private final int timeout;

    private DockerClient(URI dockerHost, int timeout) {
        this.timeout = timeout;
        if ("tcp".equals(dockerHost.getScheme())) {
            String dockerHostStr = dockerHost.toString();
            dockerHostStr = dockerHostStr.replace("tcp://", "http://");
            this.dockerHost = URI.create(dockerHostStr);
        } else if ("unix".equals(dockerHost.getScheme())) {
            this.dockerHost = dockerHost;

        } else {
            throw new DockerClientException("Unsupported protocol scheme found: '" + dockerHost
                    + "'. Only 'tcp://' or 'unix://' supported.");
        }
    }

    DockerClient(URI dockerHost) {
        this(dockerHost, DEFAULT_TIMEOUT_SECONDS);
    }

    private RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(this.timeout)
                .setConnectTimeout(this.timeout)
                .setSocketTimeout(this.timeout)
                .build();
    }

    private String getSanitizedBaseURL() {
        URI sanitizedURI = this.dockerHost;
        if ("unix".equals(this.dockerHost.getScheme())) {
            sanitizedURI = UnixConnectionSocketFactory.sanitizeUri(this.dockerHost);
        }
        return sanitizedURI.toString();
    }

    private HttpClient getClient() {
        if ("unix".equals(this.dockerHost.getScheme())) {
            return Utils.getUnixSocketHTTPClient(this.dockerHost);
        }
        return Utils.getHTTPClient(true);
    }

    JSONObject Info() throws DockerClientException {
        HttpClient client = this.getClient();
        HttpGet req = new HttpGet(this.getSanitizedBaseURL() + "/info");
        RequestConfig reqConfig = this.getRequestConfig();
        req.setConfig(reqConfig);

        String result;
        try {
            HttpResponse resp = client.execute(req);
            result = Utils.getResponseContent(resp);
        } catch (Exception e) {
            throw new DockerClientException(e.toString(), e);
        }
        return new JSONObject(result);
    }

    JSONObject ServiceInspect(String serviceId) throws DockerClientException {
        HttpClient client = this.getClient();
        HttpGet req = new HttpGet(this.getSanitizedBaseURL() + "/services/" + serviceId);
        RequestConfig reqConfig = this.getRequestConfig();
        req.setConfig(reqConfig);

        String result;
        try {
            HttpResponse resp = client.execute(req);
            result = Utils.getResponseContent(resp);
        } catch (Exception e) {
            throw new DockerClientException(e.toString(), e);
        }
        return new JSONObject(result);
    }
}
