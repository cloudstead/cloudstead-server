package cloudos.cloudstead.server;

import cloudos.cslib.compute.CsCloud;
import cloudos.cslib.compute.CsCloudConfig;
import cloudos.cslib.compute.CsCloudFactory;
import cloudos.cslib.compute.digitalocean.DigitalOceanCloud;
import cloudos.cslib.compute.jclouds.JcloudBase;
import cloudos.cslib.compute.mock.MockCloud;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.s3.AmazonS3Client;
import lombok.Getter;
import lombok.Setter;

import static org.cobbzilla.util.security.ShaUtil.sha256_hex;

public class CloudConfiguration implements AWSCredentials {

    // todo: pick these based on how many accounts the cloudos will have, and based on geo
    private static final String CLOUDOS_INSTANCE_SIZE = "4gb";
    private static final String CLOUDOS_REGION = "sfo1";
    private static final String CLOUDOS_IMAGE = "ubuntu-14-04-x64";

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
    @Getter @Setter private String cloudOsChefDir;

    @Getter(lazy=true) private final AmazonIdentityManagementClient IAMclient = initIAMclient();
    private AmazonIdentityManagementClient initIAMclient() { return new AmazonIdentityManagementClient(this); }

    @Getter(lazy=true) private final AmazonS3Client s3Client = initS3client();
    private AmazonS3Client initS3client() { return new AmazonS3Client(this); }

    public CsCloud buildHostedCloud(String owner, String name) {
        try {
            final CsCloudConfig config = getHostedCloudConfig(owner, name);
            return cloudFactory.buildCloud(config);

        } catch (Exception e) {
            throw new IllegalStateException("Error building hosted cloud: "+e, e);
        }
    }

    public CsCloudConfig getHostedCloudConfig(String owner, String name) {

        if (doClientId.equals("mock")) return new CsCloudConfig().setCloudClass(MockCloud.class.getName());

        final String groupPrefix = "huc-" + sha256_hex(owner + cloudUser + dataKey + name).substring(0, 10);

        return new CsCloudConfig()
                .setAccountId(doClientId)
                .setAccountSecret(doApiKey)
                .setRegion(CLOUDOS_REGION)
                .setInstanceSize(CLOUDOS_INSTANCE_SIZE)
                .setImage(CLOUDOS_IMAGE)
                .setGroupPrefix(groupPrefix)
                .setProvider(JcloudBase.PROVIDER_DIGITALOCEAN)
                .setUser(name)
                .setDomain(domain)
                .setCloudClass(DigitalOceanCloud.class.getName());
    }

}
