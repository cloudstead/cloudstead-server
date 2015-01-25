package cloudos.cloudstead.service.cloudos;

import cloudos.appstore.ApiConstants;
import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.AppStoreCloudAccount;
import cloudos.appstore.model.support.ApiToken;
import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.support.CloudOsRequest;
import cloudos.cloudstead.server.CloudConfiguration;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cslib.compute.CsCloud;
import cloudos.cslib.compute.instance.CsInstance;
import cloudos.cslib.compute.instance.CsInstanceRequest;
import cloudos.cslib.compute.mock.MockCsInstance;
import cloudos.databag.BaseDatabag;
import cloudos.databag.CloudOsDatabag;
import cloudos.databag.EmailDatabag;
import cloudos.databag.PortsDatabag;
import cloudos.dns.DnsClient;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;
import com.google.common.io.Files;
import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.cobbzilla.sendgrid.SendGrid;
import org.cobbzilla.sendgrid.SendGridPermissions;
import org.cobbzilla.sendgrid.SendGridUser;
import org.cobbzilla.util.dns.DnsRecord;
import org.cobbzilla.util.dns.DnsRecordMatch;
import org.cobbzilla.util.dns.DnsType;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.util.reflect.ReflectionUtil;
import org.cobbzilla.util.security.ShaUtil;
import org.cobbzilla.util.system.CommandResult;
import org.cobbzilla.util.system.CommandShell;
import org.cobbzilla.util.system.ConnectionInfo;
import org.cobbzilla.wizard.util.RestResponse;
import rooty.toots.vendor.VendorDatabag;
import rooty.toots.vendor.VendorDatabagSetting;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.cobbzilla.util.io.FileUtil.toFile;
import static org.cobbzilla.util.json.JsonUtil.toJson;
import static org.cobbzilla.util.security.ShaUtil.sha256_hex;

@Slf4j
public class CloudOsLauncher implements Runnable {

    public static final int DEFAULT_TTL = (int) TimeUnit.HOURS.toSeconds(1);
    public static final int MAX_TRIES = 3;

    public static final String[] INIT_CONFIGS = {
            "aws_access_key", "aws_secret_key", "aws_iam_user", "s3_bucket", "authy.user"
    };
    public static final String[] EMAIL_CONFIGS = {
            "smtp_relay.username", "smtp_relay.password", "smtp_relay.host", "smtp_relay.port"
    };
    private static final int DEFAULT_MX_RANK = 10;
    public static final String CLOUDOS_CERT_NAME = "ssl-https";

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
        this.cloud = cloudConfig.buildHostedCloud(status.getAdmin().getUuid(), status.getRequest().getName());
    }

    private String getFqdn() {
        return cloud.getConfig().getFqdn(instance.getHost());
    }

    @Override
    public void run() {
        boolean success = false;
        for (int tries = 0; tries < MAX_TRIES; tries++) {
            status.initRetry();
            try {
                launch();
                if (!status.hasError()) {
                    // Give the server 10 seconds to get up and running. It should be already, but just in case.
                    Thread.sleep(10000);

                    if (cloudOsIsRunning()) {
                        status.success("{setup.success}");
                        success = true;
                        break;
                    } else {
                        final String fqdn = instance != null ? getFqdn() : "no-fqdn";
                        throw new IllegalStateException("launch completed OK but instance ("+ fqdn +") was not running!");
                    }
                }

            } catch (Exception e) {
                if (tries == MAX_TRIES-1) {
                    status.error("{setup.error.unexpected.final}", "An unexpected error occurred during setup, we are giving up");
                } else {
                    status.error("{setup.error.unexpected.willRetry}", "An unexpected error occurred during setup, we will retry");
                }

            } finally {
                if (!success && instance != null) {
                    try {
                        if (cloud != null && !cloud.teardown(instance)) {
                            log.error("error tearing down instance that failed to come up properly (returned false)");
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

    private boolean resetCloudOs(Admin admin, CloudOsRequest request) {
        CloudOs cloudOs = cloudOsDAO.findByName(request.getName());
        if (cloudOs == null) {
            cloudOs = new CloudOs();
            cloudOs.setAdminUuid(admin.getUuid());
            cloudOs.setName(request.getName());
            cloudOs.initUcid();

            try {
                cloudOs = cloudOsDAO.create(cloudOs);
            } catch (Exception e) {
                status.error("{setup.creatingCloudOs.error}", "Error saving new CloudOs to DB");
                return false;
            }
            status.setCloudOs(cloudOs);

        } else {
            status.setCloudOs(cloudOs);

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
                    cloud.teardown(instance);

                } catch (Exception e) {
                    log.error("Error tearing down instance prior to relaunch (marching bravely forward!): " + e, e);
                    status.update("{setup.teardownPreviousInstance.nonFatalError}");
                }
            } else {
                log.warn("CloudOs exists in DB but has no instance information: " + cloudOs);
            }
        }
        return true;
    }

    // todo: this can be massively parallelized... use promises/futures to do as much async as possible
    // only a few things truly need to happen "in order", to be documented :)
    private void launch() {

        final CloudOsRequest request = status.getRequest();
        final Admin admin = status.getAdmin();

        // this will handle tearing down any existing instance
        if (!resetCloudOs(admin, request)) {
            // problem
            return;
        }

        CloudOs cloudOs = status.getCloudOs();

        final String hostname = request.getName();
        final String salt = cloudConfig.getDataKey() + cloudConfig.getCloudUser();
        final String iamUser = CloudOs.getIAMuser(admin, hostname, salt);
        final String iamPath = CloudOs.getIAMpath(admin);

        // Does the user already exist?
        boolean userExists = false;
        status.update("{setup.creatingCloudAdminAccount}");
        final AmazonIdentityManagementClient IAMclient = cloudConfig.getIAMclient();
        try {
            final GetUserResult getUserResult = IAMclient.getUser(new GetUserRequest().withUserName(iamUser));
            if (getUserResult.getUser() != null) {
                userExists = true;
                if (getUserResult.getUser().getPath().equals(iamPath)) {
                    // somehow the user was already created by this admin on a previous call. This is OK.
                } else {
                    // the user exists and that path indicates that a different admin created it, so it's an error
                    status.error("{setup.error.creatingCloudAdminAccount.notUnique}", "Duplicate account error", hostname);
                    return;
                }
            }
        } catch (NoSuchEntityException e) {
            // OK, the user does not exist, that's fine

        } catch (Exception e) {
            // any other problem is fatal
            status.error("{setup.error.creatingCloudAdminAccount.checkingExistenceOfIAMuser}", "Error looking up IAM user");
            return;
        }

        final CreateAccessKeyResult accessKey;
        if (!userExists) {
            // Create IAM user and add to group
            try {
                IAMclient.createUser(new CreateUserRequest(iamUser).withPath(iamPath));
                IAMclient.addUserToGroup(new AddUserToGroupRequest(cloudConfig.getGroup(), iamUser));
                accessKey = IAMclient.createAccessKey(new CreateAccessKeyRequest().withUserName(iamUser));
            } catch (Exception e) {
                status.error("{setup.error.creatingCloudAdminAccount.iamAdd.serverError}", "Error adding IAM user");
                return;
            }

        } else {
            try {
                // nuke all other keys
                final ListAccessKeysResult keyList = IAMclient.listAccessKeys(new ListAccessKeysRequest().withUserName(iamUser));
                for (AccessKeyMetadata md : keyList.getAccessKeyMetadata()) {
                    IAMclient.deleteAccessKey(new DeleteAccessKeyRequest().withUserName(iamUser).withAccessKeyId(md.getAccessKeyId()));
                }
                accessKey = IAMclient.createAccessKey(new CreateAccessKeyRequest().withUserName(iamUser));
            } catch (Exception e) {
                status.error("{setup.error.creatingCloudAdminAccount.iamKey.serverError}", "Error creating new IAM key");
                return;
            }
        }

        cloudOs.setS3accessKey(accessKey.getAccessKey().getAccessKeyId());
        cloudOs.setS3secretKey(accessKey.getAccessKey().getSecretAccessKey());

        // save things here, now that we have s3 keys
        status.update("{setup.savingCloudAdminAccount}");
        try {
            cloudOs = cloudOsDAO.update(cloudOs);
        } catch (Exception e) {
            status.error("{setup.error.savingCloudAdminAccount.serverError}", "Error updating cloudOs in DB");
            return;
        }

        // start instance on digitalocean
        status.update("{setup.startingMasterInstance}");
        final CsInstanceRequest instanceRequest = new CsInstanceRequest().setHost(hostname);
        try {
            instance = cloud.newInstance(instanceRequest);
        } catch (Exception e) {
            status.error("{setup.error.startingMasterInstance.serverError}", "Error booting compute instance in cloud: "+e);
            return;
        }

        // update with instance masterIp and mark as running
        status.update("{setup.updatingCloudOsToMarkAsRunning}");
        try {
            cloudOs.setRunning(true);
            cloudOs.setInstanceJson(toJson(instance));
            cloudOs = cloudOsDAO.update(cloudOs);

        } catch (Exception e) {
            status.error("{setup.error.updatingCloudOsToMarkAsRunning.serverError}", "Error updating cloudOs in DB");
            return;
        }

        // create DNS record for hostname
        status.update("{setup.creatingDnsRecord}");
        final String publicIp = instance.getPublicIp();

        final String dnsApiKey = setupDns(publicIp, hostname, getFqdn());
        if (dnsApiKey == null) {
            status.error("{setup.error.creatingDnsRecord.serverError}", "Error updating DNS entry");
            return;
        }

        // notify app store of new cloud; set appstore connection info
        final String ucid = cloudOs.getUcid();
        if (!addAppStoreAccount(getFqdn(), ucid)) return;

        // generate sendgrid credentials
        status.update("{setup.generatingSendgridCredentials}");
        final SendGridUser sendGridUser = new SendGridUser()
                .setName(CloudOs.getSendgridUser(admin, hostname, salt))
                .setPassword(RandomStringUtils.randomAlphanumeric(20))
                .setPermissions(new SendGridPermissions().setEmail());
        try {
            configuration.getSendGrid().addOrEditUser(sendGridUser);
        } catch (Exception e) {
            status.error("{setup.error.generatingSendgridCredentials.apiError}", "Error adding/editing sendgrid credentials");
            return;
        }

        // setup init-files for chef-solo run: hostname, ssl key+cert and cloud-init databag
        status.update("{setup.buildingInitializationFile}");
        final File initFilesDir = Files.createTempDir();
        try {
            // SSL key & cert
            final File cloudOsCertDir = new File(initFilesDir.getAbsolutePath() + "/certs/cloudos");

            if (!cloudOsCertDir.exists() && !cloudOsCertDir.mkdirs()) {
                throw new IllegalStateException("error creating cloudOsCertDir: "+cloudOsCertDir.getAbsolutePath());
            }
            Files.copy(new File(cloudConfig.getSslPem()), new File(cloudOsCertDir, CLOUDOS_CERT_NAME+".pem"));
            Files.copy(new File(cloudConfig.getSslKey()), new File(cloudOsCertDir, CLOUDOS_CERT_NAME+".key"));

            // lots of settings to define for the new instance, these go into the cloudos/init.json cloudOsDatabag
            final List<VendorDatabagSetting> settings = new ArrayList<>();

            final BaseDatabag baseDatabag = new BaseDatabag()
                    .setHostname(hostname)
                    .setParent_domain(cloudConfig.getDomain())
                    .setSsl_cert_name(CLOUDOS_CERT_NAME);

            final CloudOsDatabag cloudOsDatabag = new CloudOsDatabag()
                    .setUcid(ucid)
                    .setAws_access_key(cloudOs.getS3accessKey())
                    .setAws_secret_key(cloudOs.getS3secretKey())
                    .setAws_iam_user(iamUser)
                    .setS3_bucket(cloudConfig.getBucket())
                    .setRun_as("cloudos")
                    .setServer_tarball(cloudConfig.getCloudOsServerTarball())
                    .setRecovery_email(admin.getEmail())
                    .setAdmin_initial_pass(admin.getHashedPassword().getHashedPassword()) // todo: allow cloudstead-specific password
                    .setAuthy(configuration.getAuthy())
                    .setDns(configuration.getCloudOsDns().getBaseUri(), hostname, dnsApiKey)
                    .setAppstore(configuration.getAppStore().getBaseUri(), ucid)
                    .setVendor(new VendorDatabag()
                            .setService_key_endpoint(configuration.getServiceRequestEndpoint())
                            .setSsl_key_sha(ShaUtil.sha256_file(cloudConfig.getSslKey()))
                            .setSettings(settings));

            for (String config : INIT_CONFIGS) {
                // for now this is the only setting that must be changed before allowing ssh access
                // (in addition to changing out the cert, see ServiceKeyHandler in rooty-toots for more info)
                boolean blockSsh = "authy.user".equals(config);
                settings.add(new VendorDatabagSetting(config, getShasum(cloudOsDatabag, config), blockSsh));
            }

            final ConnectionInfo smtp_relay = new ConnectionInfo(SendGrid.SMTP_RELAY, SendGrid.SMTP_RELAY_PORT, sendGridUser.getUsername(), sendGridUser.getPassword());
            final EmailDatabag emailDatabag = new EmailDatabag().setSmtp_relay(smtp_relay).setVendor(new VendorDatabag());
            for (String config : EMAIL_CONFIGS) {
                emailDatabag.getVendor().addSetting(new VendorDatabagSetting(config, getShasum(emailDatabag, config)));
            }

            toFile(new File(initFilesDir.getAbsolutePath() + "/data_bags/cloudos/base.json"), toJson(baseDatabag));
            toFile(new File(initFilesDir.getAbsolutePath() + "/data_bags/cloudos/init.json"), toJson(cloudOsDatabag));
            toFile(new File(initFilesDir.getAbsolutePath() + "/data_bags/cloudos/ports.json"), toJson(new PortsDatabag(3001)));
            toFile(new File(initFilesDir.getAbsolutePath() + "/data_bags/email/init.json"), toJson(emailDatabag));

        } catch (Exception e) {
            log.warn("Error building initialization file: "+e, e);
            status.error("{setup.error.buildingInitializationFile.serverError}", "Error creating initialization files for new instance");
            return;
        }

        // run chef-solo to configure the box
        status.update("{setup.cheffing}");
        final File cloudOsChefDir = new File(cloudConfig.getCloudOsChefDir());
        final CommandLine chefSolo = new CommandLine(new File(cloudOsChefDir, "deploy.sh"));
        chefSolo.addArgument(hostname + "@" + publicIp);

        // setup system env for deploy.sh script
        final Map<String, String> chefSoloEnv = new HashMap<>();
        chefSoloEnv.put("INIT_FILES", initFilesDir.getAbsolutePath());

        // do not use default json editor (won't be found), use the one installed here
        chefSoloEnv.put("JSON_EDIT", "cstead json");

        // cloudsteads callback to this server (the one that launched it) to manage DNS
        chefSoloEnv.put("DISABLE_DNS", "true");

        CommandResult commandResult = null;
        try {
            // decrypt the private key and put it on disk somewhere, so that deploy.sh works without asking for a passphrase
            // we can make this a lot easier... what a PITA
            final String privateKey = instance.getKey();
            if (privateKey == null) {
                log.warn("instance had no private key (mock instance?), skipping chef setup");

            } else {
                // be careful, this is a plaintext key (low risk though, since after we set the thing up, we lock ourselves out -- though this is still a todo)
                @Cleanup("delete") final File keyFile = File.createTempFile("cloudos", ".key");
                CommandShell.chmod(keyFile, "600");
                toFile(keyFile.getAbsolutePath(), privateKey);
                chefSoloEnv.put("SSH_KEY", keyFile.getAbsolutePath()); // add key to env

                commandResult = CommandShell.exec(chefSolo, null, cloudOsChefDir, chefSoloEnv);
                if (!commandResult.isZeroExitStatus()) {
                    throw new IllegalStateException("Error running chef-solo: " + commandResult.getException(), commandResult.getException());
                }
                log.info("chef-solo result:\nexit=" + commandResult.getExitStatus() + "\nout=\n" + commandResult.getStdout() + "\nerr=\n" + commandResult.getStderr());
            }

        } catch (Exception e) {
            status.error("{setup.error.cheffing.serverError}", "Error running chef-solo");
            log.error("Error running chef ("+e+"): stdout:\n"+ ((commandResult == null) ? null : commandResult.getStdout()) + "\n\nstderr:\n"+((commandResult == null) ? null : commandResult.getStderr()));
            return;

        } finally {
            try { FileUtils.deleteDirectory(initFilesDir); } catch (Exception e) {
                log.warn("Error deleting initFilesDir ("+initFilesDir+"): "+e, e);
            }
        }

        log.info("launch completed OK: "+hostname+"."+cloudConfig.getDomain());
        status.completed();
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
                if (!response.isSuccess()) throw new IllegalStateException("appstore account creation failed: "+response);
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

    private String getShasum(Object databag, String config) {
        return sha256_hex(String.valueOf(ReflectionUtil.get(databag, config)));
    }

    private String setupDns(String publicIp, String hostname, String fqdn) {
        final DnsClient dnsClient = configuration.getDnsClient();
        try {
            // find any previous entries for this cloudstead
            final DnsRecordMatch subdomainMatcher = new DnsRecordMatch().setSubdomain(fqdn);
            final List<DnsRecord> existing = dnsClient.list(subdomainMatcher);

            if (!existing.isEmpty()) {
                if (dnsClient.remove(subdomainMatcher) <= 0) {
                    throw new IllegalStateException("Error removing records, expected to remove some but didn't");
                }
            }

            // basic hostname
            dnsClient.write((DnsRecord) new DnsRecord()
                    .setTtl(DEFAULT_TTL)
                    .setType(DnsType.A)
                    .setFqdn(fqdn)
                    .setValue(publicIp));

            // email routing
            dnsClient.write((DnsRecord) new DnsRecord()
                    .setTtl(DEFAULT_TTL)
                    .setType(DnsType.A)
                    .setFqdn("mx." + fqdn)
                    .setValue(publicIp));

            dnsClient.write((DnsRecord) new DnsRecord()
                    .setTtl(DEFAULT_TTL)
                    .setOption("rank", String.valueOf(DEFAULT_MX_RANK))
                    .setType(DnsType.MX)
                    .setFqdn(fqdn)
                    .setValue("mx." + fqdn));

            // cloudos can call back to dnsServer to define more names if needed (after installing apps),
            // we generate an API key here for the dnsServer
            return dnsClient.createOrUpdateUser(hostname);

        } catch (Exception e) {
            log.error("Error updating DNS: "+e, e);
            return null;
        }
    }

}
