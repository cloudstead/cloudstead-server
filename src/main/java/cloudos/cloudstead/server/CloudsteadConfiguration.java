package cloudos.cloudstead.server;

import cloudos.cloudstead.resources.ActivationsResource;
import cloudos.cloudstead.resources.ApiConstants;
import cloudos.dns.DnsClient;
import cloudos.server.HasTwoFactorAuthConfiguration;
import cloudos.service.TwoFactorAuthService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.mail.sender.SmtpMailConfig;
import org.cobbzilla.mail.service.TemplatedMailSenderConfiguration;
import org.cobbzilla.sendgrid.SendGrid;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.wizard.server.config.DatabaseConfiguration;
import org.cobbzilla.wizard.server.config.HasDatabaseConfiguration;
import org.cobbzilla.wizard.server.config.RestServerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration @Slf4j
public class CloudsteadConfiguration extends RestServerConfiguration
        implements HasDatabaseConfiguration, HasTwoFactorAuthConfiguration, TemplatedMailSenderConfiguration {

    @Setter private DatabaseConfiguration database;
    @Bean public DatabaseConfiguration getDatabase() { return database; }

    @Getter @Setter private String emailTemplateRoot;
    @Getter @Setter private SmtpMailConfig smtpMailConfig;

    @Getter @Setter private ApiConnectionInfo adminAuthy;

    private TwoFactorAuthService twoFactorAuthService = null;
    @Override public TwoFactorAuthService getTwoFactorAuthService () {
        if (twoFactorAuthService == null) twoFactorAuthService = new TwoFactorAuthService(authy);
        return twoFactorAuthService;
    }

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
        return getPublicUriBase() + getHttp().getBaseUri() + ApiConstants.SERVICEKEYS_ENDPOINT;
    }

    public String getEmailVerificationUrl(String key) {
        return new StringBuilder()
                .append(getPublicUriBase()).append(getHttp().getBaseUri()).append(ApiConstants.ACTIVATIONS_ENDPOINT)
                .append("?").append(ActivationsResource.PARAM_KEY).append("=").append(key).toString();
    }

    public String getResetPasswordUrl(String key) {
        return new StringBuilder()
                .append(getPublicUriBase()).append(getHttp().getBaseUri())
                .append("/reset_password.html?key=").append(key).toString();
    }
}
