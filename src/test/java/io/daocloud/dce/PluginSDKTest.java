package io.daocloud.dce;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.json.JSONObject;

import java.net.URI;

/**
 * Unit test for simple App.
 */
public class PluginSDKTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PluginSDKTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(PluginSDKTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testDetectHostIP() {
        PluginSDK sdk = new PluginSDK(URI.create("tcp://192.168.1.137:2370"));
        String IP = sdk.detectHostIP();
        assertTrue("192.168.1.137".equals(IP));
    }

    public void testDetectDCEPorts() {
        PluginSDK sdk = new PluginSDK(URI.create("tcp://192.168.1.137:2370"));
        int[] ports = sdk.detectDCEPorts();
        assertTrue(ports[0] == 2375 && ports[1] == 80 && ports[2] == 443);
    }
}
