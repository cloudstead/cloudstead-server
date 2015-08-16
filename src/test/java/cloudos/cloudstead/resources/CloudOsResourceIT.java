package cloudos.cloudstead.resources;

import cloudos.appstore.model.app.config.AppConfigurationMap;
import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.support.AdminResponse;
import cloudos.cloudstead.model.support.CloudOsRequest;
import cloudos.cslib.compute.digitalocean.DigitalOceanCloudType;
import cloudos.model.instance.CloudOsState;
import cloudos.cloudstead.service.CloudOsStatus;
import cloudos.cslib.compute.CsCloud;
import cloudos.cslib.compute.instance.CsInstance;
import cloudos.cslib.compute.meta.CsCloudType;
import cloudos.model.CsGeoRegion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.cobbzilla.util.http.HttpStatusCodes;
import org.cobbzilla.util.system.Sleep;
import org.cobbzilla.wizard.util.RestResponse;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cloudos.cloudstead.resources.ApiConstants.CLOUDOS_ENDPOINT;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.json.JsonUtil.fromJson;
import static org.cobbzilla.util.json.JsonUtil.toJson;
import static org.cobbzilla.wizardtest.RandomUtil.randomEmail;
import static org.cobbzilla.wizardtest.RandomUtil.randomName;
import static org.junit.Assert.*;

@Slf4j
public class CloudOsResourceIT extends ApiResourceITBase {

    public static final String DOC_TARGET = "CloudOs CRUD";
    private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(30);

    private final String queueName = "test_cloudosresourceit_"+RandomStringUtils.randomAlphanumeric(10);
    private final String rootySecret = RandomStringUtils.randomAlphanumeric(30);
    private String name;
    private CloudOs cloudOs;
    private File stagingDir;

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
        }
    }

    @Before public void setUp() throws Exception {
        name = "test"+ RandomStringUtils.randomAlphanumeric(10).toLowerCase();
    }

    @Test public void testCloudOSCrud() throws Exception {

        RestResponse response;
        CloudOs[] instances;
        AppConfigurationMap appConfig;
        Map<String, List<ConstraintViolationBean>> violations;
        List<ConstraintViolationBean> vlist;

        apiDocs.startRecording(DOC_TARGET, "create a CloudOs instance");

        final String uri = CLOUDOS_ENDPOINT + "/" + name;
        final String email = randomEmail();
        final AdminResponse adminResponse = registerAndActivateAdmin(email);
        setToken(adminResponse.getSession());

        apiDocs.addNote("list cloudos instances - should be empty");
        instances = fromJson(get(CLOUDOS_ENDPOINT).json, CloudOs[].class);
        assertEquals(0, instances.length);

        final CsCloudType<? extends CsCloud> cloudType = getConfiguration().getCloudConfig().getProvider(DigitalOceanCloudType.TYPE.getName()).getType();
        final CsGeoRegion region = cloudType.getRegions().iterator().next();
        final CloudOsRequest cloudOsRequest = new CloudOsRequest(name).setRegion(region);

        apiDocs.addNote("create a new CloudOs instance");
        response = doPut(uri, toJson(cloudOsRequest));
        assertEquals(HttpStatusCodes.OK, response.status);
        cloudOs = fromJson(response.json, CloudOs.class);
        assertEquals(name, cloudOs.getName());
        assertEquals(CloudOsState.initial, cloudOs.getState());

        // the server will not return the stagingDir via REST, so we reach into the DB layer to grab it
        stagingDir = getBean(CloudOsDAO.class).findByName(cloudOs.getName()).getStagingDirFile();
        assertTrue(stagingDir.exists() && stagingDir.isDirectory());

        int numApps = cloudOs.getAllApps().size();

        // Make sure chef staging dir contains cookbooks for all apps
        assertCookbookDirsExist(cloudOs);

        apiDocs.addNote("list cloudos instances -- should be one");
        instances = fromJson(get(CLOUDOS_ENDPOINT).json, CloudOs[].class);
        assertEquals(1, instances.length);
        assertEquals(name, instances[0].getName());

        apiDocs.addNote("update CloudOs instance, add two more apps");
        cloudOsRequest.setAdditionalApps("jira simple-test-app");
        cloudOs = fromJson(post(uri, toJson(cloudOsRequest)).json, CloudOs.class);
        assertEquals(numApps + 2, cloudOs.getAllApps().size());
        assertCookbookDirsExist(cloudOs);

        apiDocs.addNote("try to launch the instance, should fail due to missing config");
        response = doPost(uri + "/launch", null);
        assertEquals(HttpStatusCodes.UNPROCESSABLE_ENTITY, response.status);
        appConfig = fromJson(response.json, AppConfigurationMap.class);
        violations = appConfig.getViolations();
        assertEquals(1, violations.size());
        vlist = violations.get("simple-test-app");
        assertNotNull(vlist);
        assertEquals(1, vlist.size());
        assertEquals("{err.init.test_setting.empty}", vlist.get(0).getMessageTemplate());

        apiDocs.addNote("fetch configuration for the apps to be installed");
        appConfig = fromJson(get(uri + "/config").json, AppConfigurationMap.class);
        assertNotNull(vlist);
        assertEquals(1, vlist.size());
        assertEquals("{err.init.test_setting.empty}", vlist.get(0).getMessageTemplate());

        apiDocs.addNote("update configuration for the apps to be installed, shouldn't find any other violations");
        appConfig.getConfig("simple-test-app").getCategory("init").set("test_setting", randomName());
        appConfig = fromJson(post(uri + "/config", toJson(appConfig)).json, AppConfigurationMap.class);
        assertTrue(appConfig.getViolations().isEmpty());

        apiDocs.addNote("try again to launch the instance, should now work");
        response = post(uri + "/launch", null);
        assertEquals(200, response.status);

        // Ensure databags exist
        assertTrue(new File(abs(stagingDir) + "/data_bags/base/admin.json").exists());

        CloudOsStatus status = fromJson(response.json, CloudOsStatus.class);
        while (!status.isCompleted() && !status.getCloudOs().isRunning()) {
            Sleep.sleep(SECONDS.toMillis(3));
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
        // This check is disabled for now, since all apps now come from the appstore, and
        // the MockAppStoreApiClient always returns the same tarball
        // Re-enable when we can pre-populate our Mock appstore client with bundles for each app
        if (true) return;
        for (String app : cloudOs.getAllApps()) {
            final File appDir = new File(abs(stagingDir) + "/cookbooks/" + app);
            assertTrue(appDir.exists() && appDir.isDirectory());
        }
    }

}