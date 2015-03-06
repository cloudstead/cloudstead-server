package cloudos.cloudstead.resources;

import cloudos.appstore.model.app.config.AppConfigurationMap;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.support.AdminResponse;
import cloudos.cloudstead.model.support.CloudOsRequest;
import cloudos.cloudstead.model.support.CloudOsState;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cloudstead.service.cloudos.CloudOsStatus;
import cloudos.cslib.compute.instance.CsInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.cobbzilla.util.http.HttpStatusCodes;
import org.cobbzilla.util.system.Sleep;
import org.cobbzilla.wizard.util.RestResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cloudos.cloudstead.resources.ApiConstants.CLOUDOS_ENDPOINT;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.json.JsonUtil.fromJson;
import static org.cobbzilla.util.json.JsonUtil.toJson;
import static org.cobbzilla.wizardtest.RandomUtil.randomEmail;
import static org.junit.Assert.*;

@Slf4j
public class CloudOsResourceIT extends ApiResourceITBase {

    public static final String DOC_TARGET = "CloudOs CRUD";
    private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(30);

    private final String queueName = "test_cloudosresourceit_"+RandomStringUtils.randomAlphanumeric(10);
    private final String rootySecret = RandomStringUtils.randomAlphanumeric(30);
    private String name;
    private CloudOs cloudOs;

    @Override public Map<String, String> getServerEnvironment() {
        final Map<String, String> env = super.getServerEnvironment();
        env.put("ROOTY_QUEUE_NAME", queueName);
        env.put("ROOTY_SECRET", rootySecret);
        return env;
    }

    @After public void teardown () throws Exception {
        if (cloudOs == null) return;
        try {
            final String uri = CLOUDOS_ENDPOINT + "/" + name;
            log.info("deleting: " + uri + ": " + doDelete(uri));
            final long start = System.currentTimeMillis();
            while (doGet(uri).status != 404 && System.currentTimeMillis() - start < TIMEOUT) {
                Sleep.sleep(1000);
            }
            if (doGet(uri).status != 404) die("never died");

        } catch (Exception e) {
            final CsInstance instance = cloudOs.getInstance();
            final String ip = instance != null ? instance.getPublicIp() : "unknown-ip";
            final String msg = "Error deleting CloudOs (" + e + "), there may be a stray instance running at " + ip;
            log.warn(msg, e);
            die(msg, e);
        }
    }

    @Before public void setUp() throws Exception {
        name = "test"+ RandomStringUtils.randomAlphanumeric(10).toLowerCase();
    }

    @Test public void testCloudOSCrud() throws Exception {

        RestResponse response;
        CloudOs[] instances;
        AppConfigurationMap appConfig;

        apiDocs.startRecording(DOC_TARGET, "create a CloudOs instance");

        final String uri = CLOUDOS_ENDPOINT + "/" + name;
        final String email = randomEmail();
        final AdminResponse adminResponse = registerAndActivateAdmin(email);
        setToken(adminResponse.getSession());

        apiDocs.addNote("list cloudos instances - should be empty");
        instances = fromJson(get(CLOUDOS_ENDPOINT).json, CloudOs[].class);
        assertEquals(0, instances.length);

        final CloudOsRequest cloudOsRequest = new CloudOsRequest(name);

        apiDocs.addNote("create a new CloudOs instance");
        response = doPut(uri, toJson(cloudOsRequest));
        assertEquals(HttpStatusCodes.OK, response.status);
        cloudOs = fromJson(response.json, CloudOs.class);
        assertEquals(name, cloudOs.getName());
        assertEquals(CloudOsState.initial, cloudOs.getState());

        int numApps = cloudOs.getAllApps().size();

        // Make sure chef staging dir contains cookbooks for all apps
        assertCookbookDirsExist(cloudOs);

        apiDocs.addNote("list cloudos instances -- should be one");
        instances = fromJson(get(CLOUDOS_ENDPOINT).json, CloudOs[].class);
        assertEquals(0, instances.length);
        assertEquals(name, instances[0].getName());

        apiDocs.addNote("update CloudOs instance, add an app");
        cloudOsRequest.setAdditionalApps("jira");
        cloudOs = fromJson(post(uri, toJson(cloudOsRequest)).json, CloudOs.class);
        assertEquals(numApps + 1, cloudOs.getAllApps().size());
        assertCookbookDirsExist(cloudOs);

        apiDocs.addNote("try to launch the instance, should fail due to missing config");
        response = doPost(uri + "/launch", null);
        assertEquals(HttpStatusCodes.UNPROCESSABLE_ENTITY, response.status);

        apiDocs.addNote("fetch configuration for the apps to be installed");
        appConfig = fromJson(get(uri + "/config").json, AppConfigurationMap.class);
        // todo: make some assertions about this

        apiDocs.addNote("update configuration for the apps to be installed");
        appConfig = fromJson(post(uri + "/config", toJson(appConfig)).json, AppConfigurationMap.class);

        apiDocs.addNote("try again to launch the instance, should now work");
        CloudOsStatus status = fromJson(post(uri + "/launch", null).json, CloudOsStatus.class);
        while (!status.isCompleted()) {
            Sleep.sleep(SECONDS.toMillis(30));
            apiDocs.addNote("check status of cloudos launch");
            status = fromJson(get(uri+"/status").json, CloudOsStatus.class);
        }

        assertEquals(name.toLowerCase(), status.getCloudOs().getName().toLowerCase());
        assertFalse(status.isError());
        assertTrue(status.isSuccess());
        assertTrue(status.getHistory().size() > 0);
        assertTrue(status.getCloudOs().isRunning());
    }

    private void assertCookbookDirsExist(CloudOs cloudOs) {
        final File stagingDir = getBean(CloudsteadConfiguration.class).getCloudConfig().getChefStagingDir(cloudOs);
        for (String app : cloudOs.getAllApps()) {
            final File appDir = new File(abs(stagingDir) + "/cookbooks/" + app);
            assertTrue(appDir.exists() && appDir.isDirectory());
        }
    }
}