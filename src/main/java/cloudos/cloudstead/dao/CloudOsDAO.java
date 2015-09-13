package cloudos.cloudstead.dao;

import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.model.instance.CloudOsState;
import cloudos.cloudstead.server.CloudConfiguration;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.databag.*;
import cloudos.dns.DnsClient;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.cobbzilla.sendgrid.SendGrid;
import org.cobbzilla.sendgrid.SendGridPermissions;
import org.cobbzilla.sendgrid.SendGridUser;
import org.cobbzilla.util.dns.DnsRecord;
import org.cobbzilla.util.dns.DnsRecordMatch;
import org.cobbzilla.util.dns.DnsType;
import org.cobbzilla.util.http.HttpUtil;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.security.ShaUtil;
import org.cobbzilla.util.system.ConnectionInfo;
import org.cobbzilla.wizard.dao.UniquelyNamedEntityDAO;
import org.cobbzilla.wizard.validation.SimpleViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import rooty.toots.vendor.VendorDatabag;
import rooty.toots.vendor.VendorDatabagSetting;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cloudos.appstore.model.app.config.AppConfiguration.getShasum;
import static org.cobbzilla.util.http.HttpUtil.DEFAULT_CERT_NAME;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.*;
import static org.cobbzilla.util.json.JsonUtil.fromJson;
import static org.cobbzilla.util.json.JsonUtil.toJson;
import static org.hibernate.criterion.Restrictions.*;

@Repository @Slf4j
public class CloudOsDAO extends UniquelyNamedEntityDAO<CloudOs> {

    public CloudOs findByName(String name) { return findByUniqueField("name", name.toLowerCase()); }
    public List<CloudOs> findByAdmin(String uuid) { return findByField("adminUuid", uuid); }

    public List<CloudOs> findActiveByAdmin(String uuid) {
        return list(criteria().add(and(eq("adminUuid", uuid), not(in("state", CloudOsState.INACTIVE)))));
    }

    public static final String[] INIT_CONFIGS = {
            "aws_access_key", "aws_secret_key", "aws_iam_user", "s3_bucket", "authy.user"
    };

    public static final int DEFAULT_TTL = (int) TimeUnit.HOURS.toSeconds(1);
    public static final int DEFAULT_MX_RANK = 10;

    public static final String[] EMAIL_CONFIGS = {
            "smtp_relay.username", "smtp_relay.password", "smtp_relay.host", "smtp_relay.port"
    };

    @Autowired protected CloudsteadConfiguration configuration;
    @Autowired protected AdminDAO adminDAO;

    @Override public Object preCreate(@Valid CloudOs cloudOs) {

        final Admin admin = adminDAO.findByUuid(cloudOs.getAdminUuid());
        if (admin == null) die("preCreate: admin does not exist: "+cloudOs.getAdminUuid());

        final CloudConfiguration cloudConfig = configuration.getCloudConfig();
        cloudOs.setName(cloudOs.getName());

        final String ucid = cloudOs.getUcid();

        final String iamUser = setupAws(admin, cloudOs);

        // SSL key & cert
        final String stagingDir = cloudOs.getStagingDir();
        final File cloudOsCertDir = FileUtil.mkdirOrDie(new File(abs(stagingDir) + "/certs/cloudos"));
        try {
            Files.copy(new File(cloudConfig.getSslPem()), new File(cloudOsCertDir, DEFAULT_CERT_NAME + ".pem"));
            Files.copy(new File(cloudConfig.getSslKey()), new File(cloudOsCertDir, DEFAULT_CERT_NAME + ".key"));
        } catch (IOException e) {
            die("preCreate: error writing certificate files: "+e, e);
        }

        // lots of settings to define for the new instance, these go into the cloudos/init.json cloudOsDatabag
        final List<VendorDatabagSetting> settings = new ArrayList<>();

        // standard base databag
        final BaseDatabag baseDatabag = new BaseDatabag()
                .setHostname(cloudOs.getName())
                .setParent_domain(cloudConfig.getDomain())
                .setSsl_cert_name(HttpUtil.DEFAULT_CERT_NAME);

        // we can fill out most of this now.
        // we have to wait until after we have launched (and an IP address) to set DNS and SMTP configuration
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

        try {
            toFile(new File(abs(stagingDir) + "/data_bags/base/base.json"), toJson(baseDatabag));
            toFile(new File(abs(stagingDir) + "/data_bags/cloudos/init.json"), toJson(cloudOsDatabag));
            toFile(new File(abs(stagingDir) + "/data_bags/cloudos/ports.json"), toJson(new PortsDatabag(3001)));
            writeAdminDatabag(admin, cloudOs);

        } catch (Exception e) {
            die("preCreate: error writing databags: "+e, e);
        }

        return super.preCreate(cloudOs);
    }

    @Override public Object preUpdate(@Valid CloudOs cloudOs) {
        cloudOs.setName(cloudOs.getName());
        return super.preUpdate(cloudOs);
    }

    @Override
    public void delete(String uuid) {

        final CloudOs cloudOs = findByUuid(uuid);
        if (cloudOs == null) return;

        final File stagingDir = cloudOs.getStagingDirFile();
        if (stagingDir.exists()) {
            if (!FileUtils.deleteQuietly(stagingDir)) {
                log.error("Error deleting cloudos staging directory: "+abs(stagingDir));
            }
        }

        super.delete(uuid);
    }

    protected static String salt(CloudConfiguration cloudConfig) { return cloudConfig.getDataKey() + cloudConfig.getStorageUser(); }
    protected static String salt(CloudsteadConfiguration configuration) { return salt(configuration.getCloudConfig()); }

    protected String setupAws(Admin admin, CloudOs cloudOs) {

        final CloudConfiguration cloudConfig = configuration.getCloudConfig();
        final String salt = salt(cloudConfig);
        final String hostname = cloudOs.getName();

        final String iamUser = cloudOs.getIAMuser(admin, hostname, salt);
        final String iamPath = cloudOs.getIAMpath(admin);

        // Does the user already exist?
        boolean userExists = false;
        final AmazonIdentityManagementClient IAMclient = cloudConfig.getIAMclient();
        try {
            final GetUserResult getUserResult = IAMclient.getUser(new GetUserRequest().withUserName(iamUser));
            if (getUserResult.getUser() != null) {
                userExists = true;
                if (getUserResult.getUser().getPath().equals(iamPath)) {
                    // somehow the user was already created by this admin on a previous call. This is OK.
                } else {
                    // the user exists and that path indicates that a different admin created it, so it's an error
                    final String msg = "Duplicate account error";
                    log.error(msg);
                    throw new SimpleViolationException("{err.cloudos.init.creatingCloudAdminAccount.notUnique}", msg);
                }
            }
        } catch (NoSuchEntityException e) {
            // OK, the user does not exist, that's fine

        } catch (Exception e) {
            // any other problem is fatal
            final String msg = "Error looking up IAM user";
            log.error(msg, e);
            throw new SimpleViolationException("{err.cloudos.init.creatingCloudAdminAccount.checkingExistenceOfIAMuser}", msg);
        }

        final CreateAccessKeyResult accessKey;
        if (!userExists) {
            // Create IAM user and add to group
            try {
                IAMclient.createUser(new CreateUserRequest(iamUser).withPath(iamPath));
                IAMclient.addUserToGroup(new AddUserToGroupRequest(cloudConfig.getGroup(), iamUser));
                accessKey = IAMclient.createAccessKey(new CreateAccessKeyRequest().withUserName(iamUser));
            } catch (Exception e) {
                final String msg = "Error adding IAM user";
                log.error(msg, e);
                throw new SimpleViolationException("{setup.error.creatingCloudAdminAccount.iamAdd.serverError}", msg);
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
                final String msg = "Error creating new IAM key";
                log.error(msg, e);
                throw new SimpleViolationException("{setup.error.creatingCloudAdminAccount.iamKey.serverError}", msg);
            }
        }

        cloudOs.setS3accessKey(accessKey.getAccessKey().getAccessKeyId());
        cloudOs.setS3secretKey(accessKey.getAccessKey().getSecretAccessKey());
        return iamUser;
    }

    public String setupDns(String publicIp, String hostname, String fqdn, CloudOs cloudOs) {
        final DnsClient dnsClient = configuration.getDnsClient();
        try {
            // find any previous entries for this cloudstead
            final DnsRecordMatch subdomainMatcher = new DnsRecordMatch().setSubdomain(fqdn);
            final List<DnsRecord> existing = dnsClient.list(subdomainMatcher);

            if (!existing.isEmpty()) {
                if (dnsClient.remove(subdomainMatcher) <= 0) {
                    die("Error removing records, expected to remove some but didn't");
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
            final String dnsApiKey = dnsClient.createOrUpdateUser(hostname, configuration.getCloudConfig().getDomain());

            final File stagingDir = cloudOs.getStagingDirFile();
            final File initJson = new File(abs(stagingDir) + "/data_bags/cloudos/init.json");
            final CloudOsDatabag cloudOsDatabag = fromJson(initJson, CloudOsDatabag.class);
            cloudOsDatabag.setDns(configuration.getCloudOsDns().getBaseUri(), cloudOs.getName(), dnsApiKey);
            toFile(initJson, toJson(cloudOsDatabag));

            return dnsApiKey;

        } catch (Exception e) {
            log.error("Error updating DNS: "+e, e);
            return null;
        }
    }

    public void setupSendGrid(Admin admin, CloudOs cloudOs) throws Exception {

        final SendGridUser sendGridUser = new SendGridUser()
                .setName(cloudOs.getSendgridUser(admin, cloudOs.getName(), salt(configuration)))
                // ensure password contains both letters and numbers, or SendGrid will reject it
                .setPassword(randomAlphabetic(10) + randomNumeric(10))
                .setPermissions(new SendGridPermissions().setEmail());
        configuration.getSendGrid().addOrEditUser(sendGridUser);

        final File stagingDir = cloudOs.getStagingDirFile();
        final ConnectionInfo smtp_relay = new ConnectionInfo(SendGrid.SMTP_RELAY, SendGrid.SMTP_RELAY_PORT, sendGridUser.getUsername(), sendGridUser.getPassword());
        final EmailDatabag emailDatabag = new EmailDatabag().setSmtpRelay(smtp_relay).setVendor(new VendorDatabag());
        for (String config : EMAIL_CONFIGS) {
            emailDatabag.getVendor().addSetting(new VendorDatabagSetting(config, getShasum(emailDatabag, config)));
        }
        toFile(new File(abs(stagingDir) + "/data_bags/email/init.json"), toJson(emailDatabag));
    }

    public void writeAdminDatabag(Admin admin, CloudOs cloudOs) throws Exception {
        // write admin databag -- determine where any auto-generated passwords will be sent
        final AdminDatabag adminDatabag = new AdminDatabag(admin.getFirstName(), admin.getEmail());
        final File stagingDir = cloudOs.getStagingDirFile();
        toFile(new File(abs(stagingDir)+"/data_bags/base/admin.json"), toJson(adminDatabag));
    }

}
