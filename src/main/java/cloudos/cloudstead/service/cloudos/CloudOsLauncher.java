package cloudos.cloudstead.service.cloudos;

import cloudos.appstore.ApiConstants;
import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.AppStoreCloudAccount;
import cloudos.appstore.model.support.ApiToken;
import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.support.CloudOsEdition;
import cloudos.cloudstead.model.support.CloudOsGeoRegion;
import cloudos.cloudstead.model.support.CloudOsState;
import cloudos.cloudstead.server.CloudConfiguration;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cslib.compute.CsCloud;
import cloudos.cslib.compute.instance.CsInstance;
import cloudos.cslib.compute.instance.CsInstanceRequest;
import cloudos.cslib.compute.mock.MockCsInstance;
import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.system.Command;
import org.cobbzilla.util.system.CommandProgressFilter;
import org.cobbzilla.util.system.CommandResult;
import org.cobbzilla.util.system.CommandShell;
import org.cobbzilla.wizard.util.RestResponse;
import rooty.toots.chef.ChefHandler;
import rooty.toots.chef.ChefSolo;
import rooty.toots.chef.ChefSoloEntry;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.toFile;
import static org.cobbzilla.util.json.JsonUtil.fromJson;
import static org.cobbzilla.util.json.JsonUtil.toJson;
import static org.cobbzilla.util.system.CommandShell.chmod;
import static rooty.toots.chef.ChefSolo.SOLO_JSON;

@Slf4j
public class CloudOsLauncher implements Runnable {

    @Getter private CloudOsStatus status;
    private final CloudsteadConfiguration configuration;
    private final CloudOsDAO cloudOsDAO;
    private final CloudConfiguration cloudConfig;

    private CsCloud cloud = null;
    private CsInstance instance = null;

    public CloudOsLauncher(CloudOsStatus status, CloudsteadConfiguration configuration, CloudOsDAO cloudOsDAO) {
        this.status = status;
        this.configuration = configuration;
        this.cloudOsDAO = cloudOsDAO;
        this.cloudConfig = configuration.getCloudConfig();

        final CloudOs cloudOs = status.getCloudOs();
        final String name = cloudOs.getName();
        final CloudOsEdition edition = cloudOs.getEdition();
        final CloudOsGeoRegion region = cloudOs.getRegion();

        this.cloud = cloudConfig.buildHostedCloud(status.getAdmin().getUuid(), name, edition, region);
    }

    protected String getFqdn() { return cloud.getConfig().getFqdn(instance.getHost()); }

    protected void updateState(CloudOs cloudOs, CloudOsState state) {
        cloudOs.updateState(state);
        cloudOsDAO.update(cloudOs);
    }

    @Override
    public void run() {
        boolean success = false;
        final int maxRetries = configuration.getCloudConfig().getMaxLaunchRetries();
        for (int tries = 0; tries < maxRetries; tries++) {
            status.initRetry();
            try {
                launch();
                if (!status.hasError()) {
                    // Give the server 10 seconds to get up and running. It should be already, but just in case.
                    Thread.sleep(10000);

                    if (cloudOsIsRunning()) {
                        status.success("{setup.success}");
                        success = true;
                        updateState(status.getCloudOs(), CloudOsState.live);
                        break;

                    } else {
                        final String fqdn = instance != null ? getFqdn() : "no-fqdn";
                        die("launch completed OK but instance ("+ fqdn +") was not running!");
                    }
                }

            } catch (Exception e) {
                if (tries == maxRetries-1) {
                    status.error("{setup.error.unexpected.final}", "An unexpected error occurred during setup, we are giving up");
                } else {
                    status.error("{setup.error.unexpected.willRetry}", "An unexpected error occurred during setup, we will retry");
                }

            } finally {
                if (!success && instance != null) {
                    updateState(status.getCloudOs(), CloudOsState.destroying);
                    try {
                        if (cloud != null && !cloud.teardown(instance)) {
                            log.error("error tearing down instance that failed to come up properly (returned false)");
                            updateState(status.getCloudOs(), CloudOsState.error);
                        } else {
                            updateState(status.getCloudOs(), CloudOsState.destroyed);
                        }
                    } catch (Exception e) {
                        log.error("error tearing down instance that failed to come up properly: " + e, e);
                    }
                }
            }
        }
    }

    private boolean cloudOsIsRunning() {
        if (instance == null || !status.isCompleted()) return false;
        if (instance instanceof MockCsInstance) return true;

        // todo: try this a few times before giving up
        final String url = "https://" + instance.getPublicIp() + "/";
        final CommandLine command = new CommandLine("curl")
                .addArgument("--insecure") // since we are requested via the IP address, the cert will not match
                .addArgument("--header").addArgument("Host: "+getFqdn()) // pass FQDN via Host header
                .addArgument("--silent")
                .addArgument("--location")                              // follow redirects
                .addArgument("--write-out").addArgument("%{http_code}") // just print status code
                .addArgument("--output").addArgument("/dev/null")       // and ignore data
                .addArgument(url);
        try {
            final CommandResult result = CommandShell.exec(command);
            final String statusCode = result.getStdout();
            return result.isZeroExitStatus() && statusCode != null && statusCode.trim().startsWith("2");

        } catch (IOException e) {
            log.warn("cloudOsIsRunning: Error fetching "+url+" with Host header="+getFqdn()+": "+e);
            return false;
        }
    }

    private boolean resetCloudOs() {

        final Admin admin = status.getAdmin();
        final CloudOs cloudOs = status.getCloudOs();

        CsInstance instance;
        if (!cloudOs.getAdminUuid().equals(admin.getUuid())) {
            status.error("{setup.error.notOwner}", "Another user owns this cloud");
            return false;
        }

        // if the instance is running, stop it and relaunch
        status.update("{setup.instanceLookup}");
        instance = cloudOs.getInstance();

        if (instance != null) {
            try {
                status.update("{setup.teardownPreviousInstance}");
                updateState(cloudOs, CloudOsState.destroying);
                cloud.teardown(instance);
                updateState(cloudOs, CloudOsState.destroyed);

            } catch (Exception e) {
                log.error("Error tearing down instance prior to relaunch (marching bravely forward!): " + e, e);
                status.update("{setup.teardownPreviousInstance.nonFatalError}");
                updateState(cloudOs, CloudOsState.error);
            }
        }
        return true;
    }

    // todo: this can be massively parallelized... use promises/futures to do as much async as possible
    // only a few things truly need to happen "in order", to be documented :)
    private void launch() {

        final Admin admin = status.getAdmin();
        CloudOs cloudOs = status.getCloudOs();

        // this will handle tearing down any existing instance
        if (!resetCloudOs()) {
            // problem, error status set in resetCloudOs
            return;
        }

        try {
            cloudOsDAO.writeAdminDatabag(admin, cloudOs);
        } catch (Exception e) {
            status.error("{setup.error.adminDatabag}", "Error writing admin databag: "+e);
            updateState(cloudOs, CloudOsState.error);
            return;
        }

        // start instance
        final String hostname = cloudOs.getName();
        status.update("{setup.startingMasterInstance}");
        final CsInstanceRequest instanceRequest = new CsInstanceRequest().setHost(hostname);
        try {
            updateState(cloudOs, CloudOsState.starting);
            instance = cloud.newInstance(instanceRequest);

            cloudOs.setInstanceJson(toJson(instance));
            cloudOs = cloudOsDAO.update(cloudOs);
            updateState(cloudOs, CloudOsState.started);

        } catch (Exception e) {
            status.error("{setup.error.startingMasterInstance.serverError}", "Error booting compute instance in cloud: "+e);
            updateState(cloudOs, CloudOsState.error);
            return;
        }

        // create DNS record for hostname
        status.update("{setup.creatingDnsRecord}");
        final String publicIp = instance.getPublicIp();

        final String dnsApiKey = cloudOsDAO.setupDns(publicIp, hostname, getFqdn(), cloudOs);
        if (dnsApiKey == null) {
            status.error("{setup.error.creatingDnsRecord.serverError}", "Error updating DNS entry");
            return;
        }

        // notify app store of new cloud; set appstore connection info
        final String ucid = cloudOs.getUcid();
        if (!addAppStoreAccount(getFqdn(), ucid)) return;

        // generate sendgrid credentials
        status.update("{setup.generatingSendgridCredentials}");
        try {
            cloudOsDAO.setupSendGrid(admin, cloudOs);
        } catch (Exception e) {
            log.error("Error setting up email relay: "+e, e);
            status.error("{setup.error.emailRelaySetup}", "Error setting up email relay");
            return;
        }

        // run chef-solo to configure the box
        status.update("{setup.cheffing}");
        updateState(cloudOs, CloudOsState.cheffing);

        final File stagingDir = cloudOs.getStagingDirFile();
        CommandResult commandResult = null;
        try {
            final CommandLine chefSolo = new CommandLine(new File(stagingDir, "deploy.sh"))
                    .addArgument(hostname + "@" + publicIp)
                    .addArgument(SOLO_JSON);

            // setup system env for deploy.sh script
            final Map<String, String> chefSoloEnv = new HashMap<>();
            chefSoloEnv.put("INIT_FILES", abs(stagingDir));

            // decrypt the private key and put it on disk somewhere, so that deploy.sh works without asking for a passphrase
            // we can make this a lot easier... what a PITA
            final String privateKey = instance.getKey();
            if (privateKey == null) {
                log.warn("instance had no private key (mock instance?), skipping chef setup");

            } else {
                // be careful, this is a plaintext key (low risk though, since after we set the thing up, we lock ourselves out -- though this is still a todo)
                @Cleanup("delete") final File keyFile = File.createTempFile("cloudos", ".key");
                chmod(keyFile, "600");
                toFile(abs(keyFile), privateKey);
                chefSoloEnv.put("SSH_KEY", abs(keyFile)); // add key to env

                final CommandProgressFilter filter = getLaunchProgressFilter(stagingDir);
                final Command command = new Command(chefSolo)
                        .setDir(stagingDir)
                        .setEnv(chefSoloEnv)
                        .setOut(filter)
                        .setCopyToStandard(true);
                commandResult = CommandShell.exec(command);

                if (!commandResult.isZeroExitStatus()) {
                    die("Error running chef-solo: " + commandResult.getException(), commandResult.getException());
                }
                updateState(cloudOs, CloudOsState.cheffed);
                log.info("chef-solo result:\nexit=" + commandResult.getExitStatus() + "\nout=\n" + commandResult.getStdout() + "\nerr=\n" + commandResult.getStderr());
            }

        } catch (Exception e) {
            status.error("{setup.error.cheffing.serverError}", "Error running chef-solo");
            log.error("Error running chef ("+e+"): stdout:\n"+ ((commandResult == null) ? null : commandResult.getStdout()) + "\n\nstderr:\n"+((commandResult == null) ? null : commandResult.getStderr()));
            updateState(cloudOs, CloudOsState.error);
            return;

        } finally {
            if (stagingDir != null && stagingDir.exists()) {
                try { FileUtils.deleteDirectory(stagingDir); } catch (Exception e) {
                    log.warn("Error deleting chefDir (" + stagingDir + "): " + e, e);
                }
            }
        }

        log.info("launch completed OK: "+hostname+"."+cloudConfig.getDomain());
        updateState(cloudOs, CloudOsState.setup_complete);
        status.completed();
    }

    public static final String[] CHEF_BOOTSTRAP_INDICATORS = {
            "Reading package lists", "Preconfiguring packages", "Current default time zone",
            "Running depmod.", "Updating certificates in /etc/ssl/certs",
            "INFO: Chef-client pid"
    };
    protected CommandProgressFilter getLaunchProgressFilter(File chefDir) throws Exception {

        final CommandProgressFilter filter = new CommandProgressFilter()
                .setCallback(new CloudOsLaunchProgressCallback(status));

        final int chefBootstrapPct = 30;
        final int chefRunPct = 100 - chefBootstrapPct;
        int pct = 1;
        int delta = chefBootstrapPct / CHEF_BOOTSTRAP_INDICATORS.length;

        for (String indicator : CHEF_BOOTSTRAP_INDICATORS) {
            filter.addIndicator(indicator, pct);
            pct += delta;
        }

        final ChefSolo solo = fromJson(FileUtil.toString(new File(chefDir, SOLO_JSON)), ChefSolo.class);
        int numEntries = 0;
        for (ChefSoloEntry entry : solo.getEntries()) {
            if (entry.isRecipe("default") || entry.isRecipe("validate")) {
                numEntries++;
            }
        }

        delta = chefRunPct / numEntries;
        pct = chefBootstrapPct;
        for (ChefSoloEntry entry : solo.getEntries()) {
            if (entry.isRecipe("default") || entry.isRecipe("validate")) {
                filter.addIndicator(ChefHandler.getChefProgressPattern(entry), pct);
                pct += delta;
            }
        }
        return filter;
    }

    private boolean addAppStoreAccount(String hostname, String ucid) {

        status.update("{setup.createAppStoreAccount}");
        final AppStoreApiClient appStoreClient = configuration.getAppStoreClient();
        final ApiConnectionInfo appStoreConfig = configuration.getAppStore();

        final AppStoreCloudAccount storeAccount = new AppStoreCloudAccount()
                .setUri("https://"+hostname+"/api/appstore/verify")
                .setUcid(ucid);

        RestResponse response = null;
        ApiToken token = null;
        try {
            token = appStoreClient.refreshToken(appStoreConfig.getUser(), appStoreConfig.getPassword());
            appStoreClient.setToken(token.getToken());
            response = appStoreClient.doPost(ApiConstants.CLOUDS_ENDPOINT+"/"+ucid, null);
            if (response.status == 404) {
                response = appStoreClient.doPost(ApiConstants.CLOUDS_ENDPOINT, toJson(storeAccount));
                if (!response.isSuccess()) die("appstore account creation failed: " + response);
            } else {
                log.info("Using existing appstore account: "+response.json);
            }

        } catch (Exception e) {
            log.error("Exception setting up appstore account: "+e, e);
        } finally {
            if (token != null) {
                try { appStoreClient.deleteToken(token.getToken()); } catch (Exception e) {
                    log.warn("Error deleting token: "+e);
                }
            }
        }

        if (response == null || !response.isSuccess()) {
            status.error("{setup.error.createAppStoreAccount}", "Error setting up appstore account");
            if (response != null) log.error("Error setting up appstore account: "+response);
            return false;
        }
        return true;
    }

}
