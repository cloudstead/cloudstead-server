package cloudos.cloudstead.model.support;

import cloudos.cslib.compute.CsCloud;
import cloudos.cslib.compute.digitalocean.DigitalOceanCloud;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import static cloudos.cslib.compute.jclouds.JcloudBase.PROVIDER_AWS_EC2;
import static cloudos.cslib.compute.jclouds.JcloudBase.PROVIDER_DIGITALOCEAN;
import static org.cobbzilla.util.string.StringUtil.empty;

// todo: these should eventually be managed in the DB
public enum CloudOsEdition {

    starter   (DigitalOceanCloud.class, PROVIDER_DIGITALOCEAN, "2gb", "ubuntu-14-04-x64"),
    master    (DigitalOceanCloud.class, PROVIDER_DIGITALOCEAN, "4gb", "ubuntu-14-04-x64"),
    business  (DigitalOceanCloud.class, PROVIDER_DIGITALOCEAN, "8gb", "ubuntu-14-04-x64"),
    king      (DigitalOceanCloud.class, PROVIDER_DIGITALOCEAN, "16gb", "ubuntu-14-04-x64"),
    emperor   (DigitalOceanCloud.class, PROVIDER_DIGITALOCEAN, "32gb", "ubuntu-14-04-x64"),
    maximus   (DigitalOceanCloud.class, PROVIDER_DIGITALOCEAN, "48gb", "ubuntu-14-04-x64"),
    supermax  (DigitalOceanCloud.class, PROVIDER_DIGITALOCEAN, "64gb", "ubuntu-14-04-x64");

    @Getter private Class<? extends CsCloud> cloudClass;
    @Getter private String provider;
    @Getter private String instanceType;
    @Getter private String image;

    CloudOsEdition(Class<? extends CsCloud> cloudClass, String provider, String instanceType, String image) {
        this.cloudClass = cloudClass;
        this.provider = provider;
        this.instanceType = instanceType;
        this.image = image;
    }

    @JsonCreator public static CloudOsEdition create (String val) {
        return empty(val) ? null : CloudOsEdition.valueOf(val.toLowerCase());
    }

    public String getRegionName(CloudOsGeoRegion region) {
        switch (provider) {
            case PROVIDER_DIGITALOCEAN:
                switch (region) {
                    case us_east: return "nyc1";
                    case us_west: return "sfo1";
                    case eu: return "ams1";
                    case singapore: return "sgp1";
                    default: throw new IllegalArgumentException("Region "+region+" is not supported for provider "+provider);
                }

            case PROVIDER_AWS_EC2:
                switch (region) {
                    case us_east: return "us-east-1";
                    case us_west: return "us-west-1";
                    case eu: return "eu-west-1";
                    case singapore: return "ap-southeast-1";
                    default: throw new IllegalArgumentException("Region "+region+" is not supported for provider "+provider);
                }

            default: throw new IllegalArgumentException("getRegionName: unsupported provider: "+provider);
        }
    }
}
