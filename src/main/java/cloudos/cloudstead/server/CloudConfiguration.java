package cloudos.cloudstead.server;

import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.support.CloudOsEdition;
import cloudos.cloudstead.model.support.CloudOsGeoRegion;
import cloudos.cslib.compute.CsCloud;
import cloudos.cslib.compute.CsCloudConfig;
import cloudos.cslib.compute.CsCloudFactory;
import cloudos.cslib.compute.mock.MockCloud;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.s3.AmazonS3Client;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.io.FileUtil;
import rooty.toots.chef.ChefDirSynchronizer;

import java.io.File;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.security.ShaUtil.sha256_hex;

@Slf4j
public class CloudConfiguration implements AWSCredentials {

    private static final CsCloudFactory cloudFactory = new CsCloudFactory();

    @Getter @Setter private String cloudOsServerTarball;
    @Getter @Setter private String domain;

    @Getter @Setter private String aWSAccessKeyId;
    @Getter @Setter private String aWSSecretKey;

    @Getter @Setter private String doClientId;
    @Getter @Setter private String doApiKey;

    @Getter @Setter private String cloudUser;

    @Getter @Setter private String salt;
    @Getter @Setter private String dataKey;
    @Getter @Setter private String bucket;
    @Getter @Setter private String group;

    @Getter @Setter private String sslPem;
    @Getter @Setter private String sslKey;

    @Getter @Setter private String chefSources;
    @Getter @Setter private File chefStagingDir;

    private File chefDir = null;
    private ChefDirSynchronizer chefSync = null;

    public synchronized File getChefDir () {
        if (chefDir == null) {
            chefDir = FileUtil.createTempDirOrDie(CloudConfiguration.class.getName()+"_chef_");
            chefSync = new ChefDirSynchronizer(chefSources, chefDir);
            chefSync.fire();
        }
        return chefDir;
    }

    @Getter(lazy=true) private final AmazonIdentityManagementClient IAMclient = initIAMclient();
    private AmazonIdentityManagementClient initIAMclient() { return new AmazonIdentityManagementClient(this); }

    @Getter(lazy=true) private final AmazonS3Client s3Client = initS3client();
    private AmazonS3Client initS3client() { return new AmazonS3Client(this); }

    public CsCloud buildHostedCloud(String owner, CloudOs cloudOs) {
        return buildHostedCloud(owner, cloudOs.getName(), cloudOs.getEdition(), cloudOs.getRegion());
    }

    public CsCloud buildHostedCloud(String owner, String name, CloudOsEdition edition, CloudOsGeoRegion region) {
        try {
            final CsCloudConfig config = getHostedCloudConfig(owner, name, edition, region);
            return cloudFactory.buildCloud(config);

        } catch (Exception e) {
            return die("Error building hosted cloud: " + e, e);
        }
    }

    public CsCloudConfig getHostedCloudConfig(String owner, String name, CloudOsEdition edition, CloudOsGeoRegion region) {

        if (doClientId.equals("mock")) return new CsCloudConfig().setCloudClass(MockCloud.class.getName());

        final String groupPrefix = "huc-" + sha256_hex(owner + cloudUser + dataKey + name).substring(0, 10);

        // todo: fetch cloud api keys based on edition.getProvider()...
        return new CsCloudConfig()
                .setAccountId(doClientId)
                .setAccountSecret(doApiKey)
                .setRegion(edition.getRegionName(region))
                .setInstanceSize(edition.getInstanceType())
                .setImage(edition.getImage())
                .setGroupPrefix(groupPrefix)
                .setProvider(edition.getProvider())
                .setUser(name)
                .setDomain(domain)
                .setCloudClass(edition.getCloudClass().getName());
    }

}
