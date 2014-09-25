package cloudos.cloudstead.server;

import cloudos.cloudstead.resources.ServiceKeyRequestsResource;
import cloudos.dns.DnsClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.sendgrid.SendGrid;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.wizard.server.config.DatabaseConfiguration;
import org.cobbzilla.wizard.server.config.HasDatabaseConfiguration;
import org.cobbzilla.wizard.server.config.RestServerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration @Slf4j
public class CloudsteadConfiguration extends RestServerConfiguration
        implements HasDatabaseConfiguration /*, HasMqConfiguration, HasDocStoreConfiguration*/ {

    @Setter private DatabaseConfiguration database;
    @Bean public DatabaseConfiguration getDatabase() { return database; }

    @Setter private CloudConfiguration cloudConfig;
    @Bean public CloudConfiguration getCloudConfig() { return cloudConfig; }

    @Bean public DnsClient getDnsClient () { return new DnsClient(cloudOsDns); }

    @Setter private ApiConnectionInfo sendGridConfig;
    @Getter(lazy=true) private final SendGrid sendGrid = initSendGrid();
    private SendGrid initSendGrid() { return new SendGrid(sendGridConfig); }

    @Getter @Setter private ApiConnectionInfo authy;

    @Getter @Setter private ApiConnectionInfo cloudOsDns;

    public String getServiceRequestEndpoint () {
        return getPublicUriBase() + getHttp().getBaseUri() + ServiceKeyRequestsResource.ENDPOINT;
    }
}
