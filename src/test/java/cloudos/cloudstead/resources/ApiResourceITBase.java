package cloudos.cloudstead.resources;

import cloudos.cloudstead.model.support.AdminRequest;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cloudstead.server.CloudsteadServer;
import cloudos.dns.mock.MockDnsClient;
import org.cobbzilla.sendgrid.mock.MockSendGrid;
import org.cobbzilla.util.http.HttpStatusCodes;
import org.cobbzilla.util.system.CommandShell;
import org.cobbzilla.wizard.server.config.factory.ConfigurationSource;
import org.cobbzilla.wizard.server.config.factory.StreamConfigurationSource;
import org.cobbzilla.wizard.util.RestResponse;
import org.cobbzilla.wizardtest.resources.ApiDocsResourceIT;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.cobbzilla.util.json.JsonUtil.toJson;
import static org.junit.Assert.assertEquals;

public class ApiResourceITBase extends ApiDocsResourceIT<CloudsteadConfiguration, CloudsteadServer> {

    public static final String TEST_ENV_FILE = ".cloudstead-test.env";
    private static final Map<String, String> environment;
    static {
        try {
            environment = CommandShell.loadShellExports(TEST_ENV_FILE);
        } catch (IOException e) {
            throw new IllegalStateException("Error reading env file: "+TEST_ENV_FILE+": "+e, e);
        }
    }
    protected Map<String, String> getServerEnvironment() { return environment; }

    @Override protected List<ConfigurationSource> getConfigurations() {
        return StreamConfigurationSource.fromResources(getClass(), "cloudstead-config-test.yml");
    }

    @Override public void beforeServerStart() throws Exception {
        // register mocks for DNS
        final CloudsteadConfiguration configuration = (CloudsteadConfiguration) serverHarness.getConfiguration();
        configuration.setDnsClient(new MockDnsClient());
        configuration.setSendGrid(new MockSendGrid());
    }

    @Override protected Class<? extends CloudsteadServer> getRestServerClass() { return CloudsteadServer.class; }

    @Override protected String getTokenHeader() { return ApiConstants.H_API_KEY; }

    protected RestResponse registerAdmin(String email) throws Exception {
        RestResponse response;
        final AdminRequest request = newAdminRequest(email);

        apiDocs.addNote("register an admin");
        response = doPost(AdminsResource.ENDPOINT, toJson(request));
        assertEquals(HttpStatusCodes.OK, response.status);
        return response;
    }

    protected AdminRequest newAdminRequest(String email) {
        final AdminRequest request = new AdminRequest();
        request.setFirstName(randomAlphanumeric(10));
        request.setLastName(randomAlphanumeric(10));
        request.setEmail(email);
        request.setMobilePhone(randomNumeric(10));
        request.setPassword(randomAlphanumeric(10));
        request.setMobilePhone(randomNumeric(10));
        request.setMobilePhoneCountryCode(1);
        request.setTos(true);
        return request;
    }

}
