package cloudos.cloudstead.resources;

import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.auth.CloudsteadAuthResponse;
import cloudos.cloudstead.model.support.AdminRequest;
import cloudos.cloudstead.model.support.AdminResponse;
import cloudos.model.auth.AuthResponse;
import cloudos.model.auth.LoginRequest;
import org.cobbzilla.mail.sender.mock.MockTemplatedMailSender;
import org.cobbzilla.util.http.HttpStatusCodes;
import org.cobbzilla.wizard.util.RestResponse;
import org.junit.Before;
import org.junit.Test;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.cobbzilla.util.json.JsonUtil.fromJson;
import static org.cobbzilla.util.json.JsonUtil.toJson;
import static org.cobbzilla.util.string.StringUtil.urlEncode;
import static org.cobbzilla.wizardtest.RandomUtil.randomEmail;
import static org.junit.Assert.*;

public class AdminsResourceTest extends ApiResourceITBase {

    private static final String DOC_TARGET = "Account Management";

    @Before public void resetTokens() { flushTokens(); }

    public AuthResponse assertAccount(AdminRequest request) throws Exception {
        return assertAccount(request, null);
    }

    public AuthResponse assertAccount(AdminRequest request, String deviceId) throws Exception {

        final String accountName = request.getAccountName();

        apiDocs.appendNote("registering a cloudstead admin: " + accountName);
        RestResponse response = registerAdmin(request);
        final AdminResponse admin = fromJson(response.json, AdminResponse.class);
        assertNotNull(admin);
        assertEquals(request.getEmail(), admin.getEmail());

        // grab password from email
        final MockTemplatedMailSender sender = getTemplatedMailSender();
        assertEquals(1, sender.messageCount());
        sender.reset();

        // login with password
        response = login(accountName, request.getPassword(), deviceId);
        assertEquals(200, response.status);

        AuthResponse authResponse = fromJson(response.json, CloudsteadAuthResponse.class);
        assertNotNull(authResponse.getSessionId());

        if (authResponse.isTwoFactor()) {
            final RestResponse secondFactorResponse = secondFactor(accountName, "0000000", deviceId);
            assertEquals(200, secondFactorResponse.status);
            authResponse = fromJson(secondFactorResponse.json, CloudsteadAuthResponse.class);
            assertNotNull(authResponse.getSessionId());
        }

        return authResponse;
    }

    public RestResponse login(String accountName, String password) throws Exception {
        final LoginRequest loginRequest = new LoginRequest().setName(accountName).setPassword(password);
        apiDocs.appendNote("login account " + accountName);
        return login(loginRequest);
    }

    public RestResponse login(String accountName, String password, String deviceId) throws Exception {
        final LoginRequest loginRequest = new LoginRequest()
                .setName(accountName)
                .setPassword(password)
                .setDeviceId(deviceId)
                .setDeviceName(deviceId);
        apiDocs.appendNote("login account " + accountName + " with device "+deviceId);
        return login(loginRequest);
    }

    public RestResponse secondFactor(String accountName, String secondFactor, String deviceId) throws Exception {

        apiDocs.appendNote("account requires 2-factor auth. verify that a request to view profile should fail, since login has not been completed");
        assertEquals(404, doGet(AdminsResource.ENDPOINT + "/" + accountName).status);

        final LoginRequest loginRequest = new LoginRequest()
                .setName(accountName)
                .setSecondFactor(secondFactor)
                .setDeviceId(deviceId)
                .setDeviceName(deviceId);
        apiDocs.appendNote("send 2-factor verification token for account " + accountName);
        return login(loginRequest);
    }

    @Test
    public void testCreateAccountWith2FactorAuth () throws Exception {
        apiDocs.startRecording(DOC_TARGET, "create a account with 2-factor authentication and login");
        final String accountName = randomEmail();
        final String password = randomAlphanumeric(10);
        final String device1 = randomAlphanumeric(10);
        final AdminRequest request = newAdminRequest(accountName, password);
        request.setTwoFactor(true);
        final AuthResponse authResponse = assertAccount(request, device1);
        assertNotEquals(AuthResponse.TWO_FACTOR_SID, authResponse.getSessionId());

        apiDocs.addNote("request to view profile should now succeed");
        assertEquals(200, get(AdminsResource.ENDPOINT + "/" + authResponse.getAccount().getUuid()).status);

        RestResponse login;

        flushTokens();
        apiDocs.addNote("login again, should not require 2-factor auth since we just supplied it");
        login = login(accountName, password, device1);
        assertEquals(200, login.status);
        assertNotEquals(AuthResponse.TWO_FACTOR_SID, fromJson(login.json, CloudsteadAuthResponse.class).getSessionId());

        flushTokens();
        apiDocs.addNote("login from a different device, should require 2-factor auth for new device");
        login = login(accountName, password, device1+"_different");
        assertEquals(200, login.status);
        assertEquals(AuthResponse.TWO_FACTOR_SID, fromJson(login.json, CloudsteadAuthResponse.class).getSessionId());
    }

    @Test
    public void testSuccessfulRegistration() throws Exception {

        RestResponse response;
        apiDocs.startRecording(DOC_TARGET, "register a new admin account");

        final String email = randomEmail();
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

        final String email = randomEmail();
        final AdminRequest request = newAdminRequest(email);

        apiDocs.addNote("register first admin, should succeed");
        response = registerAdmin(request);
        assertEquals(HttpStatusCodes.OK, response.status);

        apiDocs.addNote("register first admin a second time, should fail with uniqueness violations");
        response = registerAdmin(request);
        assertEquals(HttpStatusCodes.UNPROCESSABLE_ENTITY, response.status);
        assertExpectedViolations(response, new String[] { "{err.email.notUnique}", "{err.mobilePhone.notUnique}" } );
    }

    @Test
    public void testAdminCrud () throws Exception {

        RestResponse response;
        apiDocs.startRecording(DOC_TARGET, "register a new admin account, update it, then delete it");

        String email = randomEmail();
        final AdminRequest request = newAdminRequest(email);

        apiDocs.addNote("register admin");
        response = doPut(AdminsResource.ENDPOINT+"/"+urlEncode(request.getEmail()), toJson(request));
        assertEquals(HttpStatusCodes.OK, response.status);
        AdminResponse adminResponse = fromJson(response.json, AdminResponse.class);
        assertEquals(email, adminResponse.getEmail());

        email = randomEmail();
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

        apiDocs.addNote("lookup admin, should return 'not found' since our key is no longer valid");
        response = doGet(AdminsResource.ENDPOINT +"/"+admin.getUuid());
        assertEquals(HttpStatusCodes.NOT_FOUND, response.status);

        apiDocs.addNote("lookup session, should return 'not found'");
        response = doGet(SessionsResource.ENDPOINT +"/"+ adminResponse.getSession());
        assertEquals(HttpStatusCodes.NOT_FOUND, response.status);
    }

}