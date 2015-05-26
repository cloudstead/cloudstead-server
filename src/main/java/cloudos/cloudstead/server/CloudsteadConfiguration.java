package cloudos.cloudstead.server;

import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.support.ApiToken;
import cloudos.cloudstead.resources.ApiConstants;
import cloudos.cloudstead.resources.AuthResource;
import cloudos.dns.DnsClient;
import cloudos.server.HasTwoFactorAuthConfiguration;
import cloudos.service.TwoFactorAuthService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.mail.SimpleEmailMessage;
import org.cobbzilla.mail.sender.SmtpMailConfig;
import org.cobbzilla.mail.service.TemplatedMailSenderConfiguration;
import org.cobbzilla.sendgrid.SendGrid;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.wizard.cache.redis.HasRedisConfiguration;
import org.cobbzilla.wizard.server.config.DatabaseConfiguration;
import org.cobbzilla.wizard.server.config.HasDatabaseConfiguration;
import org.cobbzilla.wizard.server.config.RestServerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@Configuration @Slf4j
public class CloudsteadConfiguration extends RestServerConfiguration
        implements HasDatabaseConfiguration, HasRedisConfiguration, HasTwoFactorAuthConfiguration, TemplatedMailSenderConfiguration {

    // expire 10 minutes before server will automatically expire it
    private static final long EXPIRATION_SECONDS = ApiToken.EXPIRATION_SECONDS - TimeUnit.MINUTES.toSeconds(10);

    @Setter private DatabaseConfiguration database;
    @Bean public DatabaseConfiguration getDatabase() { return database; }

    @Getter @Setter private CloudsteadRedisConfiguration redis = new CloudsteadRedisConfiguration(this);

    @Getter @Setter private String emailTemplateRoot;
    @Getter @Setter private Map<String, SimpleEmailMessage> emailSenderNames = new HashMap<>();
    @Getter @Setter private SmtpMailConfig smtp;

    @Getter @Setter private ApiConnectionInfo adminAuthy;
    @Getter @Setter private ApiConnectionInfo authy;

    private TwoFactorAuthService twoFactorAuthService = null;
    @Override public TwoFactorAuthService getTwoFactorAuthService () {
        if (twoFactorAuthService == null) twoFactorAuthService = new TwoFactorAuthService(authy);
        return twoFactorAuthService;
    }

    @Setter private CloudConfiguration cloudConfig;
    @Bean public CloudConfiguration getCloudConfig() { return cloudConfig; }

    @Getter @Setter private ApiConnectionInfo cloudOsDns;
    @Setter private DnsClient dnsClient;
    public DnsClient getDnsClient () {
        if (dnsClient == null) dnsClient = new DnsClient(cloudOsDns);
        return dnsClient;
    }

    @Getter @Setter private ApiConnectionInfo appStore;
    @Setter private AppStoreApiClient appStoreClient;
    public AppStoreApiClient getAppStoreClient () {
        if (appStoreClient == null) appStoreClient = new AppStoreApiClient(appStore);
        if (appStoreClient.getTokenAge()/1000 > EXPIRATION_SECONDS) {
            try {
                if (appStoreClient.hasToken()) {
                    appStoreClient.refreshToken();
                } else {
                    appStoreClient.refreshToken(appStoreClient.getConnectionInfo().getUser(), appStoreClient.getConnectionInfo().getPassword());
                }
            } catch (Exception e) {
                die("Error refreshing API token: "+e, e);
            }
        }
        return appStoreClient;
    }

    @Setter private ApiConnectionInfo sendGridConfig;
    @Setter private SendGrid sendGrid;
    public SendGrid getSendGrid() {
        if (sendGrid == null) sendGrid = new SendGrid(sendGridConfig);
        return sendGrid;
    }

    public String getServiceRequestEndpoint () {
        return getPublicUriBase() + getHttp().getBaseUri() + ApiConstants.SERVICEKEYS_ENDPOINT;
    }

    public String getEmailVerificationUrl(String key) {
        return new StringBuilder()
                .append(getPublicUriBase()).append(getHttp().getBaseUri())
                .append(ApiConstants.AUTH_ENDPOINT).append(AuthResource.EP_ACTIVATE)
                .append("/").append(key).toString();
    }

    public String getResetPasswordUrl(String key) {
        return new StringBuilder().append(getPublicUriBase()).append("/reset_password.html?key=").append(key).toString();
    }
}
