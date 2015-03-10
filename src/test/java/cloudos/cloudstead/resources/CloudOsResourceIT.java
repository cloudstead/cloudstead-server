package cloudos.cloudstead.resources;

import cloudos.appstore.bundler.BundlerMain;
import cloudos.appstore.bundler.BundlerOptions;
import cloudos.appstore.model.app.AppManifest;
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
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.system.Sleep;
import org.cobbzilla.wizard.server.RestServer;
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
import static org.cobbzilla.util.io.FileUtil.toFileOrDie;
import static org.cobbzilla.util.io.StreamUtil.loadResourceAsStringOrDie;
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

    @Override public Map<String, String> getServerEnvironment() {
        final Map<String, String> env = super.getServerEnvironment();
        env.put("ROOTY_QUEUE_NAME", queueName);
        env.put("ROOTY_SECRET", rootySecret);
        return env;
    }

    @Override
    public void beforeStart(RestServer<CloudsteadConfiguration> server) {
        super.beforeStart(server);

        final File appTemp = FileUtil.createTempDirOrDie("appTemp");
        final File bundleDir = FileUtil.createTempDirOrDie("bundleDir");

        // Write manifest and databags from resources to tempdir
        final File manifestFile = new File(appTemp, AppManifest.CLOUDOS_MANIFEST_JSON);
        final String manifestData = loadResourceAsStringOrDie("apps/test/" + AppManifest.CLOUDOS_MANIFEST_JSON);
        toFileOrDie(manifestFile, manifestData);
        for (String databag : new String[]{"init", "config-metadata"}) {
            toFileOrDie(new File(abs(appTemp) + "/config/" + databag + ".json"), loadResourceAsStringOrDie("apps/test/config/" + databag + ".json"));
        }

        // Run the bundler on our test manifest
        final BundlerMain main = new BundlerMain(new String[] {
                BundlerOptions.OPT_MANIFEST, abs(manifestFile),
                BundlerOptions.OPT_OUTPUT_DIR, abs(bundleDir)
        });
        main.runOrDie();

        // todo: add a required fields to test-app's config, ensures we will have *some* config to fill out before launch

        final CloudsteadConfiguration configuration = (CloudsteadConfiguration) serverHarness.getConfiguration();
        final String chefSources = configuration.getCloudConfig().getChefSources();
        configuration.getCloudConfig().setChefSources(chefSources+" "+abs(new File(bundleDir, "chef")));
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
        final File stagingDir = cloudOs.getStagingDir((CloudsteadConfiguration) server.getConfiguration());
        for (String app : cloudOs.getAllApps()) {
            final File appDir = new File(abs(stagingDir) + "/cookbooks/" + app);
            assertTrue(appDir.exists() && appDir.isDirectory());
        }
    }
}