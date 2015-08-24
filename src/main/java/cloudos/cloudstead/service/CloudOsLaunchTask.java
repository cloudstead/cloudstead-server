package cloudos.cloudstead.service;

import cloudos.appstore.ApiConstants;
import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.AppStoreCloudAccount;
import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.support.CloudOsEdition;
import cloudos.cloudstead.server.CloudConfiguration;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cslib.compute.CsCloud;
import cloudos.deploy.CloudOsLaunchTaskBase;
import cloudos.model.CsGeoRegion;
import cloudos.model.auth.ApiToken;
import cloudos.model.instance.CloudOsEvent;
import cloudos.model.instance.CloudOsState;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.wizard.dao.DAO;
import org.cobbzilla.wizard.task.ITask;
import org.cobbzilla.wizard.util.RestResponse;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.json.JsonUtil.toJson;

@Slf4j
public class CloudOsLaunchTask
        extends CloudOsLaunchTaskBase<Admin, CloudOs, CloudsteadTaskResult>
        implements ITask<CloudsteadTaskResult> {

    private final CloudsteadConfiguration configuration;
    private final CloudConfiguration cloudConfig;

    public CloudOsLaunchTask(Admin admin,
                             CloudOs cloudOs,
                             CloudsteadConfiguration configuration,
                             CloudOsDAO cloudOsDAO,
                             DAO<CloudOsEvent> eventDAO) {
        this.configuration = configuration;
        this.cloudConfig = configuration.getCloudConfig();
        init(admin, cloudOs, cloudOsDAO, eventDAO);
    }

    @Override protected CsCloud buildCloud() {
        final CloudOs cloudOs = result.getCloudOs();
        final String name = cloudOs.getName();
        final CloudOsEdition edition = cloudOs.getEdition();
        final CsGeoRegion region = cloudOs.getCsRegion();
        return cloudConfig.buildHostedCloud(result.getAdmin().getUuid(), name, edition, region);
    }

    @Override protected int getMaxLaunchTries() {
        return configuration.getCloudConfig().getMaxLaunchTries();
    }

    @Override protected boolean preLaunch() {
        try {
            ((CloudOsDAO) cloudOsDAO).writeAdminDatabag(admin(), cloudOs());
            return true;

        } catch (Exception e) {
            result.error("{setup.error.adminDatabag}", "Error writing admin databag: " + e);
            updateState(cloudOs(), CloudOsState.error);
            return false;
        }
    }

    @Override protected boolean setupDns () {
        // create DNS record for hostname
        result.update("{setup.creatingDnsRecord}");
        final String publicIp = instance.getPublicIp();
        final String hostname = cloudOs().getName();
        final String dnsApiKey = ((CloudOsDAO) cloudOsDAO).setupDns(publicIp, hostname, getFqdn(), cloudOs());
        if (dnsApiKey == null) {
            result.error("{setup.error.creatingDnsRecord.serverError}", "Error updating DNS entry");
            return false;
        }
        return true;
    }

    @Override protected boolean setupMailCreds() {
        // generate sendgrid credentials
        result.update("{setup.generatingSendgridCredentials}");
        try {
            ((CloudOsDAO) cloudOsDAO).setupSendGrid(admin(), cloudOs());
            return true;

        } catch (Exception e) {
            log.error("Error setting up email relay: "+e, e);
            result.error("{setup.error.emailRelaySetup}", "Error setting up email relay");
            return false;
        }
    }

    @Override protected boolean addAppStoreAccount(String hostname, String ucid) {

        result.update("{setup.createAppStoreAccount}");
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
            result.error("{setup.error.createAppStoreAccount}", "Error setting up appstore account");
            if (response != null) log.error("Error setting up appstore account: "+response);
            return false;
        }
        return true;
    }

}
