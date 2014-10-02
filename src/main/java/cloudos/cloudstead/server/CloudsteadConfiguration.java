package cloudos.cloudstead.server;

import cloudos.cloudstead.resources.ServiceKeyRequestsResource;
import cloudos.dns.DnsClient;
import cloudos.service.TwoFactorAuthService;
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
public class CloudsteadConfiguration extends RestServerConfiguration implements HasDatabaseConfiguration {

    @Setter private DatabaseConfiguration database;
    @Bean public DatabaseConfiguration getDatabase() { return database; }

    @Getter @Setter private ApiConnectionInfo adminAuthy;
    @Getter(lazy=true) private final TwoFactorAuthService twoFactorAuthService = initTwoFactorAuthService();
    private TwoFactorAuthService initTwoFactorAuthService() { return new TwoFactorAuthService(adminAuthy); }

    @Setter private CloudConfiguration cloudConfig;
    @Bean public CloudConfiguration getCloudConfig() { return cloudConfig; }

    @Setter private DnsClient dnsClient;
    public DnsClient getDnsClient () {
        if (dnsClient == null) dnsClient = new DnsClient(cloudOsDns);
        return dnsClient;
    }

    @Setter private ApiConnectionInfo sendGridConfig;
    @Setter private SendGrid sendGrid;
    public SendGrid getSendGrid() {
        if (sendGrid == null) sendGrid = new SendGrid(sendGridConfig);
        return sendGrid;
    }

    @Getter @Setter private ApiConnectionInfo authy;

    @Getter @Setter private ApiConnectionInfo cloudOsDns;

    public String getServiceRequestEndpoint () {
        return getPublicUriBase() + getHttp().getBaseUri() + ServiceKeyRequestsResource.ENDPOINT;
    }
}
