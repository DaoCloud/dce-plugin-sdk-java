package io.daocloud.dce;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONArray;
import org.json.JSONObject;


import java.net.URI;
import java.util.HashMap;

public class PluginSDK {
    private static final String DEFAULT_DOCKER_HOST = "unix://var/run/docker.sock";

    private final DockerClient client;

    PluginSDK(URI dockerHost) {
        this.client = new DockerClient(dockerHost);
    }

    PluginSDK() {
        this(URI.create(DEFAULT_DOCKER_HOST));
    }

    String detectHostIP() {
        JSONObject info = this.client.Info();
        return info.getJSONObject("Swarm").getString("NodeAddr");
    }

    int[] detectDCEPorts() {
        JSONObject dce_base = this.client.ServiceInspect("dce_base");
        JSONArray envArr = dce_base.getJSONObject("Spec").getJSONObject("TaskTemplate").getJSONObject("ContainerSpec").getJSONArray("Env");

        HashMap<String, String> env = new HashMap<String, String>();
        for (Object envObj : envArr) {
            String e = envObj.toString();
            if (!e.contains("=")) {
                continue;
            }

            String[] parts = e.split("=", 2);
            env.put(parts[0], parts[1]);
        }
        int swarmPort = Integer.parseInt(env.get("SWARM_PORT"));
        int controllerPort = Integer.parseInt(env.get("CONTROLLER_PORT"));
        int controllerSSLPort = Integer.parseInt(env.get("CONTROLLER_SSL_PORT"));

        return new int[]{swarmPort, controllerPort, controllerSSLPort};
    }

    private URI getPluginStorageURL(String URLTemplate) {
        String hostIP = this.detectHostIP();
        int[] DCEPorts = this.detectDCEPorts();

        int controllerPort = DCEPorts[1];
        int controllerSSLPort = DCEPorts[2];

        String storageURL = URLTemplate.replace("{DCE_HOST}", hostIP);
        if (storageURL.startsWith("http://")) {
            return URI.create(storageURL.replace("{DCE_PORT}", Integer.toString(controllerPort)));
        }
        return URI.create(storageURL.replace("{DCE_PORT}", Integer.toString(controllerSSLPort)));
    }

    private URI getPluginStorageURL() throws PluginSDKException {
        String URLTemplate = System.getenv("DCE_PLUGIN_STORAGE_URL");
        if (URLTemplate == null) {
            throw new PluginSDKException("environment `DCE_PLUGIN_STORAGE_URL` is missed");
        }
        return this.getPluginStorageURL(URLTemplate);
    }

    JSONObject SetConfig(JSONObject config) throws PluginSDKException {
        URI storageURL = this.getPluginStorageURL();
        return this.SetConfig(config, storageURL);
    }

    JSONObject SetConfig(JSONObject config, URI storageURL) throws PluginSDKException {
        String configStr = config.toString();
        HttpClient client = Utils.getHTTPClient(true);
        HttpPut req = new HttpPut(storageURL);
        Utils.setBasicAuth(req, storageURL);

        req.addHeader("Content-Type", "application/json");
        HttpEntity entity = new ByteArrayEntity(configStr.getBytes());
        req.setEntity(entity);

        String result;
        try {
            HttpResponse resp = client.execute(req);
            result = Utils.getResponseContent(resp);
        } catch (Exception e) {
            throw new PluginSDKException(e.toString(), e);
        }
        return new JSONObject(result);
    }

    JSONObject GetConfig() throws PluginSDKException {
        URI storageURL = this.getPluginStorageURL();
        return this.GetConfig(storageURL);
    }

    JSONObject GetConfig(URI storageURL) throws PluginSDKException {
        HttpClient client = Utils.getHTTPClient(true);
        HttpGet req = new HttpGet(storageURL);
        Utils.setBasicAuth(req, storageURL);
        req.addHeader("Content-Type", "application/json");

        String result;
        try {
            HttpResponse resp = client.execute(req);
            result = Utils.getResponseContent(resp);
        } catch (Exception e) {
            throw new PluginSDKException(e.toString(), e);
        }
        return new JSONObject(result);
    }
}
