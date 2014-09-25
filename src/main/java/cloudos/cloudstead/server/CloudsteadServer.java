package cloudos.cloudstead.server;

import org.cobbzilla.wizard.server.RestServerBase;
import org.cobbzilla.wizard.server.config.factory.ConfigurationSource;

import java.util.List;

public class CloudsteadServer extends RestServerBase<CloudsteadConfiguration> {

    private static final String[] API_CONFIG_YML = {"cloudstead-config.yml"};

    @Override protected String getListenAddress() { return LOCALHOST; }

    // args are ignored, config is loaded from the classpath
    public static void main(String[] args) throws Exception {
        final List<ConfigurationSource> configSources = getStreamConfigurationSources(CloudsteadServer.class, API_CONFIG_YML);
        main(CloudsteadServer.class, configSources);
    }

}
