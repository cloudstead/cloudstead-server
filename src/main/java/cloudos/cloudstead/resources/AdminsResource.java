package cloudos.cloudstead.resources;

import cloudos.cloudstead.dao.AdminDAO;
import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.auth.CloudsteadAuthResponse;
import cloudos.cloudstead.model.support.AdminRequest;
import cloudos.cloudstead.model.support.AdminResponse;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.model.auth.ChangePasswordRequest;
import cloudos.model.auth.LoginRequest;
import cloudos.resources.AccountsResourceBase;
import com.qmino.miredot.annotations.ReturnType;
import com.sun.jersey.api.core.HttpContext;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.mail.SimpleEmailMessage;
import org.cobbzilla.mail.TemplatedMail;
import org.cobbzilla.mail.service.TemplatedMailService;
import org.cobbzilla.wizard.cache.redis.ActivationCodeService;
import org.cobbzilla.wizard.model.HashedPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Locale;

import static cloudos.cloudstead.resources.ApiConstants.ADMINS_ENDPOINT;
import static cloudos.cloudstead.resources.ApiConstants.H_API_KEY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cobbzilla.mail.service.TemplatedMailService.T_WELCOME;
import static org.cobbzilla.wizard.resources.ResourceUtil.*;

@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Path(ADMINS_ENDPOINT)
@Service @Slf4j
public class AdminsResource extends AccountsResourceBase<Admin, CloudsteadAuthResponse> {

    public static final String EP_CHANGE_PASSWORD = "/{uuid}/change_password";
    @Autowired private ActivationCodeService acService;

    public static String getChangePasswordPath (String uuid) { return ADMINS_ENDPOINT + EP_CHANGE_PASSWORD.replace("{uuid}", uuid); }

    @Autowired private AdminDAO adminDAO;
    @Autowired private TemplatedMailService mailService;
    @Autowired private CloudsteadConfiguration configuration;

    @Autowired private CloudOsDAO cloudOsDAO;
    @Autowired private CloudOsResource cloudOsResource;

    @Override protected void afterSuccessfulLogin(LoginRequest login, Admin admin) throws Exception {}

    @Override protected CloudsteadAuthResponse buildAuthResponse(String sessionId, Admin account) {
        return new CloudsteadAuthResponse(sessionId, account);
    }

    /**
     * Lookup your account. Requires that you are already logged in, can only lookup yourself
     * @param apiKey Your API key (from login)
     * @param uuid The UUID to look up
     * @return The Admin
     * @statuscode 404 No Admin with that UUID
     */
    @GET
    @Path("/{uuid}")
    @ReturnType("cloudos.cloudstead.model.Admin")
    public Response find (@HeaderParam(H_API_KEY) String apiKey,
                          @PathParam("uuid") String uuid) {

        final Admin caller = sessionDAO.find(apiKey);
        if (caller == null || !caller.getUuid().equals(uuid)) return notFound();

        final Admin admin = adminDAO.findByUuid(uuid);
        if (admin == null) return notFound();

        sessionDAO.update(apiKey, admin); // just in case it changed underneath us, update session cache

        return ok(admin);
    }

    /**
     * Create a new Admin
     * @param name The email address of the admin to create
     * @param request The other Admin information
     * @return an AdminResponse, containing the Admin that was created and the session ID to use for subsequent requests
     */
    @PUT
    @Path("/{name}")
    @ReturnType("cloudos.cloudstead.model.support.AdminResponse")
    public Response create(@PathParam("name") String name,
                           @Context HttpContext context,
                           @Valid AdminRequest request) {

        // sanity check
        if (!name.equalsIgnoreCase(request.getEmail())) return invalid();

        Admin admin = new Admin().populate(request);

        final Locale locale = context.getRequest().getLanguage();
        admin.setLocale(locale != null ? locale.toString().replace("-", "_") : "en_US");

        admin.setHashedPassword(new HashedPassword(request.getPassword()));
        admin.setTwoFactor(true); // everyone gets two-factor turned on by default
        admin.setAuthIdInt(set2factor(request));
        admin.initEmailVerificationCode();
        admin.initUuid();

        // ensure activation key is valid
        if (!acService.attempt(request.getActivationCode(), admin.getUuid())) {
            return invalid("{err.activationCode.empty}");
        }

        admin = adminDAO.create(admin);

        final AdminResponse adminResponse = new AdminResponse(admin, sessionDAO.create(admin));

        sendInvitation(admin);

        return ok(adminResponse);
    }

    public void sendInvitation(Admin admin) {
        // todo: use the event bus for this?

        // Send welcome email with verification code
        SimpleEmailMessage welcomeSender = configuration.getEmailSenderNames().get(T_WELCOME);
        final String code = admin.getEmailVerificationCode();
        final TemplatedMail mail = new TemplatedMail()
                .setTemplateName(T_WELCOME)
                .setLocale(admin.getLocale()) // todo: collect this at registration or auto-detect from browser
                .setFromName(welcomeSender.getFromName())
                .setFromEmail(welcomeSender.getFromEmail())
                .setToEmail(admin.getEmail())
                .setToName(admin.getFullName())
                .setParameter(TemplatedMailService.PARAM_ACCOUNT, admin)
                .setParameter("activationApiUrl", configuration.getEmailVerificationUrl(code))
                .setParameter("activationUrl", configuration.getPublicUriBase() + "/#/activate/" + code);
        try {
            mailService.getMailSender().deliverMessage(mail);
        } catch (Exception e) {
            log.error("sendInvitation: error sending welcome email: "+e, e);
        }
    }

    private int set2factor(AdminRequest request) {
        return configuration.getTwoFactorAuthService()
                .addUser(request.getEmail(), request.getMobilePhone(), request.getMobilePhoneCountryCodeString());
    }

    private void remove2factor(Admin admin) {
        configuration.getTwoFactorAuthService().deleteUser(admin.getAuthIdInt());
    }

    /**
     * Update an admin
     * @param apiKey The session ID
     * @param uuid The UUID to update
     * @param request The updated information
     * @return The updated Admin object
     */
    @POST
    @Path("/{uuid}")
    @ReturnType("cloudos.cloudstead.model.Admin")
    public Response update(@HeaderParam(H_API_KEY) String apiKey,
                           @PathParam("uuid") String uuid,
                           @Valid AdminRequest request) {

        if (!uuid.equals(request.getUuid())) return invalid();

        final Admin caller = sessionDAO.find(apiKey);
        if (caller == null || !caller.getUuid().equals(uuid)) return forbidden();

        Admin admin = adminDAO.findByUuid(uuid);
        if (admin == null) return notFound();

        Integer authId = null;
        if (!request.isTwoFactor() && admin.isTwoFactor()) {
            // they are turning off two-factor auth
            remove2factor(admin);
            admin.setAuthId(null);

        } else if (request.isTwoFactor() && !admin.isTwoFactor()) {
            // they are turning on two-factor auth
            authId = set2factor(request);

        } else if (!request.getMobilePhone().equals(admin.getMobilePhone())) {
            // they changed their phone number, remove old auth id and add a new one
            remove2factor(admin);
            authId = set2factor(request);
        }

        admin.populate(request);
        if (authId != null) admin.setAuthIdInt(authId); // if the 2-factor token changed, update it now.

        admin = adminDAO.update(admin);
        sessionDAO.update(apiKey, admin);

        return ok(admin);
    }

    /**
     * Change your password
     * @param apiKey The session ID
     * @param uuid The UUID of the Admin
     * @param request The change password request object
     * @return The updated Admin object
     */
    @POST
    @Path(EP_CHANGE_PASSWORD)
    @ReturnType("cloudos.cloudstead.model.Admin")
    public Response changePassword(@HeaderParam(H_API_KEY) String apiKey,
                                   @PathParam("uuid") String uuid,
                                   @Valid ChangePasswordRequest request) {

        if (!uuid.equals(request.getUuid())) return invalid();

        final Admin caller = sessionDAO.find(apiKey);
        if (caller == null || !caller.getUuid().equals(uuid)) return forbidden();

        Admin admin = adminDAO.findByUuid(uuid);
        if (admin == null) return notFound();

        if (!admin.getHashedPassword().isCorrectPassword(request.getOldPassword())) {
            return invalid("err.password.invalid");
        }

        adminDAO.setPassword(admin, request.getNewPassword());
        sessionDAO.update(apiKey, admin);

        return ok(admin);
    }

    /**
     * Delete an Admin
     * @param apiKey The session ID
     * @param uuid The UUID to delete
     * @return No response body. 200 = success, anything else = fail
     */
    @DELETE
    @Path("/{uuid}")
    @ReturnType("java.lang.Void")
    public Response delete(@HeaderParam(H_API_KEY) String apiKey,
                           @PathParam("uuid") String uuid) {

        // you can only delete yourself (currently)
        final Admin caller = sessionDAO.find(apiKey);
        if (caller == null || !caller.getUuid().equals(uuid)) return forbidden();

        // nuke all cloudos instances
        final List<CloudOs> list = cloudOsDAO.findByAdmin(caller.getUuid());
        for (CloudOs cos : list) {
            final Response response = cloudOsResource.delete(apiKey, cos.getName());
            if (response.getStatus() % 100 != 2) log.warn("delete: error deleting cloudos: "+cos.getName());
        }

        remove2factor(caller);
        adminDAO.delete(uuid);
        sessionDAO.invalidate(apiKey);

        return ok();
    }

}
