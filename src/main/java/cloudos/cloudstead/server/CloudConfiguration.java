package cloudos.cloudstead.server;

import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.support.CloudOsEdition;
import cloudos.cslib.compute.CsCloud;
import cloudos.cslib.compute.CsCloudConfig;
import cloudos.cslib.compute.CsCloudFactory;
import cloudos.cslib.compute.meta.CsCloudType;
import cloudos.model.CsGeoRegion;
import cloudos.model.CsPlatform;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.s3.AmazonS3Client;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.security.ShaUtil.sha256_hex;

@Slf4j
public class CloudConfiguration implements AWSCredentials {

    private static final CsCloudFactory cloudFactory = new CsCloudFactory();

    @Getter @Setter private String cloudOsServerTarball;
    @Getter @Setter private String domain;

    @Getter @Setter private String aWSAccessKeyId;
    @Getter @Setter private String aWSSecretKey;

    @Getter @Setter private String storageUser;

    @Getter @Setter private CsCloudConfig[] providers;

    @Getter @Setter private String salt;
    @Getter @Setter private String dataKey;
    @Getter @Setter private String bucket;
    @Getter @Setter private String group;

    @Getter @Setter private String sslPem;
    @Getter @Setter private String sslKey;

    @Getter @Setter private File chefMaster;
    @Getter @Setter private File chefStagingDir;

    @Getter @Setter private int maxLaunchTries = 3;

    @Getter(lazy=true) private final AmazonIdentityManagementClient IAMclient = initIAMclient();
    private AmazonIdentityManagementClient initIAMclient() { return new AmazonIdentityManagementClient(this); }

    @Getter(lazy=true) private final AmazonS3Client s3Client = initS3client();
    private AmazonS3Client initS3client() { return new AmazonS3Client(this); }

    public CsCloud buildHostedCloud(String owner, CloudOs cloudOs) {
        return buildHostedCloud(owner, cloudOs.getName(), cloudOs.getEdition(), cloudOs.getCsRegion());
    }

    public CsCloud buildHostedCloud(String owner, String name, CloudOsEdition edition, CsGeoRegion region) {
        try {
            final CsCloudConfig config = getHostedCloudConfig(owner, name, edition, region);
            return cloudFactory.buildCloud(config);

        } catch (Exception e) {
            return die("Error building hosted cloud: " + e, e);
        }
    }

    public CsCloudConfig getHostedCloudConfig(String owner, String name, CloudOsEdition edition, CsGeoRegion region) {

        final CsCloudType<? extends CsCloud> cloudType = region.getCloudVendor();

        // lookup credentials in config yml using vendor name (jclouds provider name)
        final CsCloudConfig provider = getProvider(cloudType.getName());

        final String groupPrefix = "huc-" + sha256_hex(owner + storageUser + dataKey + name).substring(0, 10);
        final String instanceType = edition.getInstanceType(cloudType);
        if (instanceType == null) die("cloud "+cloudType.getName()+" does not support edition "+edition.name());

        final CsCloudConfig cloudConfig = new CsCloudConfig();
        cloudConfig.setType(cloudType);
        cloudConfig.setAccountId(provider.getAccountId());
        cloudConfig.setAccountSecret(provider.getAccountSecret());
        cloudConfig.setRegion(region.getName());
        cloudConfig.setInstanceSize(instanceType);
        cloudConfig.setImage(region.getImage(instanceType, CsPlatform.ubuntu_14_lts));
        cloudConfig.setGroupPrefix(groupPrefix);
        cloudConfig.setUser(name);
        cloudConfig.setDomain(domain);
        return cloudConfig;
    }

    public CsCloudConfig getProvider(String name) {
        if (empty(providers)) die("No cloud providers defined");
        for (CsCloudConfig provider : providers) {
            if (provider.getType().getName().equals(name) && provider.hasAccountId()) return provider;
        }
        return die("Provider not found: "+name);
    }

    public List<CsGeoRegion> getAllRegions() {
        final List<CsGeoRegion> regions = new ArrayList<>();
        for (CsCloudConfig provider : providers) {
            if (provider.hasAccountId()) regions.addAll(provider.getType().getRegions());
        }
        return regions;
    }

}
