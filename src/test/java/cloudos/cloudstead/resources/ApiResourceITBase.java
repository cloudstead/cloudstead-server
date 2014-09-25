package cloudos.cloudstead.resources;

import cloudos.cloudstead.model.support.AdminRequest;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cloudstead.server.CloudsteadServer;
import org.apache.commons.lang3.RandomStringUtils;
import org.cobbzilla.util.http.HttpStatusCodes;
import org.cobbzilla.util.system.CommandShell;
import org.cobbzilla.wizard.server.config.factory.ConfigurationSource;
import org.cobbzilla.wizard.server.config.factory.StreamConfigurationSource;
import org.cobbzilla.wizard.util.RestResponse;
import org.cobbzilla.wizardtest.resources.ApiDocsResourceIT;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    @Override
    protected List<ConfigurationSource> getConfigurations() {
        return StreamConfigurationSource.fromResources(getClass(), "cloudstead-config-test.yml");
    }

    @Override protected Class<? extends CloudsteadServer> getRestServerClass() { return CloudsteadServer.class; }

    @Override protected String getTokenHeader() { return ApiConstants.H_API_KEY; }

    protected RestResponse registerAdmin(String email) throws Exception {
        RestResponse response;
        final AdminRequest request = new AdminRequest();
        request.setEmail(email);
        request.setMobilePhone(RandomStringUtils.randomNumeric(10));
        request.setPassword(RandomStringUtils.randomAlphanumeric(10));

        apiDocs.addNote("register an admin");
        response = doPost(AdminsResource.ENDPOINT, toJson(request));
        assertEquals(HttpStatusCodes.OK, response.status);
        return response;
    }

}
