package cloudos.cloudstead.service;

import cloudos.appstore.ApiConstants;
import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.AppStoreCloudAccount;
import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.support.CloudOsEdition;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cslib.compute.CsCloud;
import cloudos.dao.CloudOsEventDAO;
import cloudos.deploy.CloudOsLaunchTaskBase;
import cloudos.model.CsGeoRegion;
import org.cobbzilla.wizard.model.ApiToken;
import cloudos.model.instance.CloudOsState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.wizard.task.ITask;
import org.cobbzilla.wizard.util.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.json.JsonUtil.toJson;

@Slf4j @NoArgsConstructor
public class CloudOsLaunchTask
        extends CloudOsLaunchTaskBase<Admin, CloudOs, CloudsteadTaskResult>
        implements ITask<CloudsteadTaskResult> {

    @Autowired private CloudsteadConfiguration configuration;
    @Autowired @Getter(AccessLevel.PROTECTED) private CloudOsDAO cloudOsDAO;
    @Autowired @Getter(AccessLevel.PROTECTED) private CloudOsEventDAO eventDAO;

    @Override public Mode getMode() { return Mode.inline; }

    @Override protected CsCloud buildCloud() {
        final CloudOs cloudOs = result.getCloudOs();
        final String name = cloudOs.getName();
        final CloudOsEdition edition = cloudOs.getEdition();
        final CsGeoRegion region = cloudOs.getCsRegion();
        return configuration.getCloudConfig().buildHostedCloud(result.getAdmin().getUuid(), name, edition, region);
    }

    @Override protected int getMaxLaunchTries() { return configuration.getCloudConfig().getMaxLaunchTries(); }

    @Override protected boolean preLaunch() {
        try {
            cloudOsDAO.writeAdminDatabag(admin(), cloudOs());
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
        final String dnsApiKey = cloudOsDAO.setupDns(publicIp, hostname, getFqdn(), cloudOs());
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
            cloudOsDAO.setupSendGrid(admin(), cloudOs());
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
