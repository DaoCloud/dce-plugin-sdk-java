package io.daocloud.dce;

import java.net.URI;


public class DockerClient {
    private static final int DEFAULT_TIMEOUT_SECONDS = 60;

    private final URI dockerHost;
    private final int timeout;

    public DockerClient(URI dockerHost, int timeout) {
        if ("tcp".equals(dockerHost.getScheme()) || "unix".equals(dockerHost.getScheme())) {
            this.dockerHost = dockerHost;
            this.timeout = timeout;
        } else {
            throw new DockerClientException("Unsupported protocol scheme found: '" + dockerHost
                    + "'. Only 'tcp://' or 'unix://' supported.");
        }
    }

    public DockerClient(URI dockerHost) {
        this(dockerHost, DEFAULT_TIMEOUT_SECONDS);
    }
}
