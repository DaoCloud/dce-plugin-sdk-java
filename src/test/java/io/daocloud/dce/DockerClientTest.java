package io.daocloud.dce;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.json.JSONObject;

import java.net.URI;

/**
 * Unit test for simple App.
 */
public class DockerClientTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DockerClientTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(DockerClientTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testInfo() {
        DockerClient c = new DockerClient(URI.create("tcp://192.168.1.137:2370"));
        try {
            JSONObject info = c.Info();
            assertTrue(info.length() > 0);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    public void testInfoUnixSocket() throws Exception {
        DockerClient c = new DockerClient(URI.create("unix:///var/run/docker.sock"));
        try {
            JSONObject info = c.Info();
            assertTrue(info.length() > 0);
        } catch (Exception e) {
            throw e;
        }
    }

    public void testServiceInspect() {
        DockerClient c = new DockerClient(URI.create("tcp://192.168.1.137:2370"));
        try {
            JSONObject service = c.ServiceInspect("dce_base");
            assertTrue(service.length() > 0);
        } catch (Exception e) {
            assertTrue(false);
        }
    }
}
