package cloudos.cloudstead.main;

import cloudos.model.instance.CloudOsAppBundle;
import cloudos.cloudstead.model.support.CloudOsEdition;
import cloudos.cloudstead.model.support.CloudOsRequest;
import cloudos.cslib.compute.meta.CsCloudTypeFactory;
import cloudos.model.CsGeoRegion;
import lombok.Getter;
import lombok.Setter;
import org.kohsuke.args4j.Option;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class CloudOsMainOptions extends CloudsteadMainOptions {

    public static final String USAGE_OPERATION = "The operation to perform";
    public static final String OPT_OPERATION = "-o";
    public static final String LONGOPT_OPERATION = "--operation";
    @Option(name=OPT_OPERATION, aliases=LONGOPT_OPERATION, usage=USAGE_OPERATION)
    @Getter @Setter private CloudOsOperation operation = CloudOsOperation.list;

    public static final String USAGE_NAME = "The name of the cloudstead. If omitted all cloudsteads will be listed.";
    public static final String OPT_NAME = "-n";
    public static final String LONGOPT_NAME = "--name";
    @Option(name=OPT_NAME, aliases=LONGOPT_NAME, usage=USAGE_NAME)
    @Getter @Setter private String name;
    public boolean hasName () { return !empty(name); }

    public static final String USAGE_PROVIDER = "The provider (PaaS vendor) for the cloudstead";
    public static final String OPT_PROVIDER = "-p";
    public static final String LONGOPT_PROVIDER = "--provider";
    @Option(name=OPT_PROVIDER, aliases=LONGOPT_PROVIDER, usage=USAGE_PROVIDER)
    @Getter @Setter private CsCloudTypeFactory.Type provider;
    public boolean hasProvider () { return !empty(provider); }

    public static final String USAGE_REGION = "The provider-specific region for the cloudstead";
    public static final String OPT_REGION = "-r";
    public static final String LONGOPT_REGION = "--region";
    @Option(name=OPT_REGION, aliases=LONGOPT_REGION, usage=USAGE_REGION)
    @Getter @Setter private String region;
    public boolean hasRegion () { return !empty(region); }

    public static final String USAGE_EDITION = "The edition for the cloudstead";
    public static final String OPT_EDITION = "-e";
    public static final String LONGOPT_EDITION = "--edition";
    @Option(name=OPT_EDITION, aliases=LONGOPT_EDITION, usage=USAGE_EDITION)
    @Getter @Setter private CloudOsEdition edition = CloudOsEdition.starter;

    public static final String USAGE_APP_BUNDLE = "The app_bundle for the cloudstead";
    public static final String OPT_APP_BUNDLE = "-b";
    public static final String LONGOPT_APP_BUNDLE = "--app-bundle";
    @Option(name=OPT_APP_BUNDLE, aliases=LONGOPT_APP_BUNDLE, usage=USAGE_APP_BUNDLE)
    @Getter @Setter private CloudOsAppBundle appBundle = CloudOsAppBundle.basic;

    public static final String USAGE_ADD_APPS = "Additional apps. Separate multiple apps with spaces or commas.";
    public static final String OPT_ADD_APPS = "-A";
    public static final String LONGOPT_ADD_APPS = "--add-apps";
    @Option(name=OPT_ADD_APPS, aliases=LONGOPT_ADD_APPS, usage=USAGE_ADD_APPS)
    @Getter @Setter private String additionalApps;

    public static final String USAGE_CONFIG = "The configuration JSON for the cloudstead";
    public static final String OPT_CONFIG = "-c";
    public static final String LONGOPT_CONFIG = "--config";
    @Option(name=OPT_CONFIG, aliases=LONGOPT_CONFIG, usage=USAGE_CONFIG)
    @Getter @Setter private String config;

    public boolean hasConfig () { return !empty(config); }

    public CloudOsRequest getCloudOsRequest() {
        return new CloudOsRequest()
                .setName(getName())
                .setEdition(getEdition())
                .setRegion(getCsGeoRegion())
                .setAppBundle(getAppBundle())
                .setAdditionalApps(getAdditionalApps());
    }

    public CsGeoRegion getCsGeoRegion() {
        return new CsGeoRegion().setCloudVendor(provider.getType()).setName(region);
    }
}
