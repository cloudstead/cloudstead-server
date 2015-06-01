package cloudos.cloudstead.resources;

import cloudos.cloudstead.model.support.AdminResponse;
import cloudos.cloudstead.model.support.CloudOsAppBundle;
import cloudos.cloudstead.model.support.CloudOsEdition;
import cloudos.model.CsGeoRegion;
import org.junit.Before;
import org.junit.Test;

import static cloudos.cloudstead.resources.ApiConstants.INSTANCE_OPTIONS_ENDPOINT;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.json.JsonUtil.fromJson;
import static org.cobbzilla.wizardtest.RandomUtil.randomEmail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InstanceOptionsResourceIT extends ApiResourceITBase {

    public static final String DOC_TARGET = "View instance options: editions, app bundles, and regions";

    @Before public void createAdmin () throws Exception {
        if (!hasToken()) {
            final AdminResponse adminResponse = registerAndActivateAdmin(randomEmail());
            setToken(adminResponse.getSession());
        }
    }

    @Test public void testGetEditions () throws Exception {
        apiDocs.startRecording(DOC_TARGET, "get available Editions");
        final CloudOsEdition[] editions = fromJson(get(INSTANCE_OPTIONS_ENDPOINT+"/editions").json, CloudOsEdition[].class);
        assertTrue(!empty(editions));
        assertEquals(CloudOsEdition.values().length, editions.length);
    }

    @Test public void testGetBundles () throws Exception {
        apiDocs.startRecording(DOC_TARGET, "get available App Bundles");
        final CloudOsAppBundle[] bundles = fromJson(get(INSTANCE_OPTIONS_ENDPOINT+"/bundles").json, CloudOsAppBundle[].class);
        assertTrue(!empty(bundles));
        assertEquals(CloudOsAppBundle.values().length, bundles.length);
    }

    @Test public void testGetRegions () throws Exception {
        apiDocs.startRecording(DOC_TARGET, "get available Regions");
        final CsGeoRegion[] regions = fromJson(get(INSTANCE_OPTIONS_ENDPOINT+"/regions").json, CsGeoRegion[].class);
        assertTrue(!empty(regions));
        assertEquals(getConfiguration().getCloudConfig().getAllRegions().size(), regions.length);
    }

}
