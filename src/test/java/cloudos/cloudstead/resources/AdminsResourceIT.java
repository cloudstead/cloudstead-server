package cloudos.cloudstead.resources;

import org.apache.commons.lang3.RandomStringUtils;
import org.cobbzilla.util.http.HttpStatusCodes;
import org.cobbzilla.wizard.util.RestResponse;
import org.cobbzilla.wizardtest.RandomUtil;
import org.junit.Test;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.support.AdminRequest;
import cloudos.cloudstead.model.support.AdminResponse;

import static org.cobbzilla.util.json.JsonUtil.fromJson;
import static org.cobbzilla.util.json.JsonUtil.toJson;
import static org.junit.Assert.*;

public class AdminsResourceIT extends ApiResourceITBase {

    public static final String DOC_TARGET = "admin CRUD";

    @Test
    public void testSuccessfulRegistration() throws Exception {

        RestResponse response;
        apiDocs.startRecording(DOC_TARGET, "register a new admin account");

        final String email = RandomUtil.randomEmail();
        response = registerAdmin(email);

        final AdminResponse adminResponse = fromJson(response.json, AdminResponse.class);
        assertEquals(email, adminResponse.getEmail());
        assertFalse(adminResponse.isEmailVerified());
        assertFalse(adminResponse.isMobilePhoneVerified());
        assertNotNull(adminResponse.getSession());

        apiDocs.addNote("lookup admin record in session");
        response = doGet(SessionsResource.ENDPOINT +"/"+ adminResponse.getSession());
        assertEquals(HttpStatusCodes.OK, response.status);
        Admin admin = fromJson(response.json, Admin.class);
        assertEquals(email, admin.getEmail());

        setToken(adminResponse.getSession());

        apiDocs.addNote("lookup admin resource by uuid");
        response = doGet(AdminsResource.ENDPOINT +"/"+admin.getUuid());
        assertEquals(HttpStatusCodes.OK, response.status);
        admin = fromJson(response.json, Admin.class);
        assertEquals(email, admin.getEmail());
    }

    @Test
    public void testDuplicateRegistration () throws Exception {

        RestResponse response;
        apiDocs.startRecording(DOC_TARGET, "register a new admin account, then try to register another account with the same info");

        final String email = RandomUtil.randomEmail();

        final AdminRequest request = new AdminRequest();
        request.setEmail(email);
        request.setMobilePhone(RandomStringUtils.randomNumeric(10));
        request.setPassword(RandomStringUtils.randomAlphanumeric(10));

        apiDocs.addNote("register first admin, should succeed");
        response = doPost(AdminsResource.ENDPOINT, toJson(request));
        assertEquals(HttpStatusCodes.OK, response.status);

        apiDocs.addNote("register first admin a second time, should fail with uniqueness violations");
        response = doPost(AdminsResource.ENDPOINT, toJson(request));
        assertEquals(HttpStatusCodes.UNPROCESSABLE_ENTITY, response.status);
        assertExpectedViolations(response, new String[] { "{err.email.notUnique}", "{err.mobilePhone.notUnique}" } );
    }

    @Test
    public void testAdminCrud () throws Exception {

        RestResponse response;
        apiDocs.startRecording(DOC_TARGET, "register a new admin account, update it, then delete it");

        String email = RandomUtil.randomEmail();

        final AdminRequest request = new AdminRequest();
        request.setEmail(email);
        request.setMobilePhone(RandomStringUtils.randomNumeric(10));
        request.setPassword(RandomStringUtils.randomAlphanumeric(10));

        apiDocs.addNote("register admin");
        response = doPost(AdminsResource.ENDPOINT, toJson(request));
        assertEquals(HttpStatusCodes.OK, response.status);
        AdminResponse adminResponse = fromJson(response.json, AdminResponse.class);
        assertEquals(email, adminResponse.getEmail());

        email = RandomUtil.randomEmail();
        request.setEmail(email);
        request.setUuid(adminResponse.getUuid());

        setToken(adminResponse.getSession());

        apiDocs.addNote("update admin");
        response = doPost(AdminsResource.ENDPOINT +"/"+ adminResponse.getUuid(), toJson(request));
        assertEquals(HttpStatusCodes.OK, response.status);
        Admin admin = fromJson(response.json, Admin.class);
        assertEquals(email, admin.getEmail());

        apiDocs.addNote("delete admin");
        response = doDelete(AdminsResource.ENDPOINT +"/"+admin.getUuid());
        assertEquals(HttpStatusCodes.OK, response.status);

        apiDocs.addNote("lookup admin, should return 'forbidden' since our key is no longer valid");
        response = doGet(AdminsResource.ENDPOINT +"/"+admin.getUuid());
        assertEquals(HttpStatusCodes.FORBIDDEN, response.status);

        apiDocs.addNote("lookup session, should return 'not found'");
        response = doGet(SessionsResource.ENDPOINT +"/"+ adminResponse.getSession());
        assertEquals(HttpStatusCodes.NOT_FOUND, response.status);
    }

}
