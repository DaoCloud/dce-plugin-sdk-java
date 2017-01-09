package io.daocloud.dce;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;

class PluginSDK {
    private final DockerClient client;

    PluginSDK(URI dockerHost) {
        this.client = new DockerClient(dockerHost);
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

    URI pluginStorageURL() {
        String storageURL = System.getenv("DCE_PLUGIN_STORAGE_URL");
        String hostIP = this.detectHostIP();
        int[] DCEPorts = this.detectDCEPorts();

        int controllerPort = DCEPorts[1];
        int controllerSSLPort = DCEPorts[2];

        storageURL = storageURL.replace("{DCE_HOST}", hostIP);
        if (storageURL.startsWith("http://")) {
            return URI.create(storageURL.replace("{DCE_PORT}", Integer.toString(controllerPort)));
        }
        return URI.create(storageURL.replace("{DCE_PORT}", Integer.toString(controllerSSLPort)));
    }

    JSONObject SetConfig(JSONObject config) {
        String configStr = config.toString();
        return this.SetConfig(configStr);
    }

    JSONObject SetConfig(String config) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut req = new HttpPut(this.pluginStorageURL());
        req.addHeader("Content-Type", "application/json");
        HttpEntity entity = new ByteArrayEntity(config.getBytes());
        req.setEntity(entity);

        BufferedReader rd;
        try {
            HttpResponse resp = client.execute(req);
            rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
        } catch (Exception e) {
            throw new PluginSDKException(e.toString(), e);
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

    JSONObject GetConfig() {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet req = new HttpGet(this.pluginStorageURL());
        req.addHeader("Content-Type", "application/json");

        BufferedReader rd;
        try {
            HttpResponse resp = client.execute(req);
            rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
        } catch (Exception e) {
            throw new PluginSDKException(e.toString(), e);
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
