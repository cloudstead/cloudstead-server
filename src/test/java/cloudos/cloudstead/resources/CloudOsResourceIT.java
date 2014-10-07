package cloudos.cloudstead.resources;

import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.support.AdminResponse;
import cloudos.cloudstead.model.support.CloudOsRequest;
import cloudos.cloudstead.service.cloudos.CloudOsStatus;
import cloudos.cslib.compute.instance.CsInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.cobbzilla.util.http.HttpStatusCodes;
import org.cobbzilla.util.system.CommandShell;
import org.cobbzilla.wizard.util.RestResponse;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.cobbzilla.util.json.JsonUtil.fromJson;
import static org.cobbzilla.util.json.JsonUtil.toJson;
import static org.cobbzilla.wizardtest.RandomUtil.randomEmail;
import static org.junit.Assert.*;

@Slf4j
public class CloudOsResourceIT extends ApiResourceITBase {

    public static final String DOC_TARGET = "CloudOs CRUD";

    private final String queueName = "test_cloudosresourceit_"+RandomStringUtils.randomAlphanumeric(10);
    private final String rootySecret = RandomStringUtils.randomAlphanumeric(30);
    private final File hostsFile = tempFile();
    private CloudOsStatus status = null;

    private File tempFile() {
        try {
            return File.createTempFile(getClass().getName(), ".tmp");
        } catch (IOException e) {
            throw new IllegalStateException("error creating temp file: "+e, e);
        }
    }

    private final File svc = CommandShell.tempScript("true");

    @Override public Map<String, String> getServerEnvironment() {
        final Map<String, String> env = super.getServerEnvironment();
        env.put("ROOTY_QUEUE_NAME", queueName);
        env.put("ROOTY_SECRET", rootySecret);
        return env;
    }

    @After public void teardown () throws Exception {
        final CloudOs cloudOs = status.getCloudOs();
        if (status != null) {
            if (cloudOs != null && cloudOs.isRunning()) {
                try {
                    doDelete(ApiConstants.CLOUDOS_ENDPOINT + "/" + cloudOs.getName());
                    while (doGet(ApiConstants.CLOUDOS_ENDPOINT +"/"+cloudOs.getName()+"/status").status != 404) {
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    final CsInstance instance = cloudOs.getInstance();
                    final String ip = instance != null ? instance.getPublicIp() : "unknown-ip";
                    log.warn("Error deleting CloudOs ("+e+"), there may be a stray instance running at " + ip, e);
                    throw e;
                }
            }
        }
    }

    @Test
    public void testCloudOSCrud() throws Exception {

        RestResponse response;
        apiDocs.startRecording(DOC_TARGET, "create a cloudOs instance");

        final String email = randomEmail();
        final AdminResponse adminResponse = registerAndActivateAdmin(email);
        setToken(adminResponse.getSession());

        apiDocs.addNote("get list of cloudos instances - should be empty");
        response = doGet(ApiConstants.CLOUDOS_ENDPOINT);
        assertEquals(HttpStatusCodes.OK, response.status);
        assertEquals(0, fromJson(response.json, CloudOs[].class).length);

        String name = "test"+RandomStringUtils.randomAlphanumeric(10).toLowerCase();

        final CloudOsRequest cloudOsRequest = new CloudOsRequest(name);

        apiDocs.addNote("create a new cloudOs instance");
        response = doPut(ApiConstants.CLOUDOS_ENDPOINT +"/"+name, toJson(cloudOsRequest));
        assertEquals(HttpStatusCodes.OK, response.status);

        status = fromJson(response.json, CloudOsStatus.class);

        while (!status.isCompleted()) {
            apiDocs.addNote("check status of cloudOs launch");
            Thread.sleep(SECONDS.toMillis(30));
            response = doGet(ApiConstants.CLOUDOS_ENDPOINT +"/"+name+"/status");
            assertEquals(200, response.status);
            status = fromJson(response.json, CloudOsStatus.class);
            if (status.isCompleted()) break;
        }

        assertEquals(name.toLowerCase(), status.getCloudOs().getName().toLowerCase());
        assertFalse(status.isError());
        assertTrue(status.isSuccess());
        assertTrue(status.getHistory().size() > 0);
        assertTrue(status.getCloudOs().isRunning());
    }
}