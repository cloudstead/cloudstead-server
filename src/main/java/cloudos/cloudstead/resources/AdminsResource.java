package cloudos.cloudstead.resources;

import cloudos.cloudstead.dao.AdminDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.auth.CloudsteadAuthResponse;
import cloudos.cloudstead.model.support.AdminRequest;
import cloudos.cloudstead.model.support.AdminResponse;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.model.auth.ChangePasswordRequest;
import cloudos.model.auth.LoginRequest;
import cloudos.resources.AccountsResourceBase;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.mail.TemplatedMail;
import org.cobbzilla.mail.service.TemplatedMailService;
import org.cobbzilla.wizard.model.HashedPassword;
import org.cobbzilla.wizard.resources.ResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static cloudos.cloudstead.resources.ApiConstants.ADMINS_ENDPOINT;
import static cloudos.cloudstead.resources.ApiConstants.H_API_KEY;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(ApiConstants.ADMINS_ENDPOINT)
@Service @Slf4j
public class AdminsResource extends AccountsResourceBase<Admin, CloudsteadAuthResponse> {

    public static final String EP_CHANGE_PASSWORD = "/{uuid}/change_password";
    public static String getChangePasswordPath (String uuid) { return ADMINS_ENDPOINT + EP_CHANGE_PASSWORD.replace("{uuid}", uuid); }

    @Autowired private AdminDAO adminDAO;
    @Autowired private TemplatedMailService mailService;
    @Autowired private CloudsteadConfiguration configuration;

    @Override protected void afterSuccessfulLogin(LoginRequest login, Admin admin) throws Exception {}

    @Override protected CloudsteadAuthResponse buildAuthResponse(String sessionId, Admin account) {
        return new CloudsteadAuthResponse(sessionId, account);
    }

    @GET
    @Path("/{uuid}")
    public Response find (@HeaderParam(H_API_KEY) String apiKey,
                          @PathParam("uuid") String uuid) {

        final Admin caller = sessionDAO.find(apiKey);
        if (caller == null || !caller.getUuid().equals(uuid)) return ResourceUtil.notFound();

        final Admin admin = adminDAO.findByUuid(uuid);
        if (admin == null) return ResourceUtil.notFound();

        sessionDAO.update(apiKey, admin); // just in case it changed underneath us, update session cache

        return Response.ok(admin).build();
    }

    @PUT
    @Path("/{name}")
    public Response create(@PathParam("name") String name, @Valid AdminRequest request) {

        // sanity check
        if (!name.equalsIgnoreCase(request.getEmail())) return ResourceUtil.invalid();

        Admin admin = populate(request, new Admin());
        admin.setHashedPassword(new HashedPassword(request.getPassword()));
        admin.setTwoFactor(true); // everyone gets two-factor turned on by default
        admin.setAuthIdInt(set2factor(request));
        admin.initEmailVerificationCode();

        admin = adminDAO.create(admin);

        final AdminResponse adminResponse = new AdminResponse(admin, sessionDAO.create(admin));

        sendInvitation(admin);

        return Response.ok(adminResponse).build();
    }

    public void sendInvitation(Admin admin) {
        // todo: use the event bus for this?

        // Send welcome email with verification code
        final TemplatedMail mail = new TemplatedMail()
                .setTemplateName(TemplatedMailService.T_WELCOME)
                .setLocale("en_US") // todo: set this at first-time-setup
                .setToEmail(admin.getEmail())
                .setToName(admin.getFullName())
                .setParameter(TemplatedMailService.PARAM_ACCOUNT, admin)
                .setParameter("activationUrl", configuration.getEmailVerificationUrl(admin.getEmailVerificationCode()));
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

    private Admin populate(AdminRequest request, Admin admin) {
        admin.setEmail(request.getEmail());
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setMobilePhone(request.getMobilePhone());
        admin.setMobilePhoneCountryCode(request.getMobilePhoneCountryCode());
        admin.setTosVersion(request.isTos() ? 1 : null); // todo: get TOS version from TOS service/dao. for now default to version 1
        return admin;
    }

    @POST
    @Path("/{uuid}")
    public Response update(@HeaderParam(H_API_KEY) String apiKey,
                           @PathParam("uuid") String uuid,
                           @Valid AdminRequest request) {

        if (!uuid.equals(request.getUuid())) return ResourceUtil.invalid();

        final Admin caller = sessionDAO.find(apiKey);
        if (caller == null || !caller.getUuid().equals(uuid)) return ResourceUtil.forbidden();

        Admin admin = adminDAO.findByUuid(uuid);
        if (admin == null) return ResourceUtil.notFound();

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

        admin = populate(request, admin);
        if (authId != null) admin.setAuthIdInt(authId); // if the 2-factor token changed, update it now.

        admin = adminDAO.update(admin);
        sessionDAO.update(apiKey, admin);

        return Response.ok(admin).build();
    }

    @POST
    @Path(EP_CHANGE_PASSWORD)
    public Response changePassword(@HeaderParam(H_API_KEY) String apiKey,
                                   @PathParam("uuid") String uuid,
                                   @Valid ChangePasswordRequest request) {

        if (!uuid.equals(request.getUuid())) return ResourceUtil.invalid();

        final Admin caller = sessionDAO.find(apiKey);
        if (caller == null || !caller.getUuid().equals(uuid)) return ResourceUtil.forbidden();

        Admin admin = adminDAO.findByUuid(uuid);
        if (admin == null) return ResourceUtil.notFound();

        if (!admin.getHashedPassword().isCorrectPassword(request.getOldPassword())) {
            return ResourceUtil.invalid("err.password.invalid");
        }
        admin.getHashedPassword().setPassword(request.getNewPassword());

        admin = adminDAO.update(admin);
        sessionDAO.update(apiKey, admin);

        return Response.ok(admin).build();
    }

    @DELETE
    @Path("/{uuid}")
    public Response delete(@HeaderParam(H_API_KEY) String apiKey,
                           @PathParam("uuid") String uuid) {

        // you can only delete yourself (currently)
        final Admin caller = sessionDAO.find(apiKey);
        if (caller == null || !caller.getUuid().equals(uuid)) return ResourceUtil.forbidden();

        remove2factor(caller);
        adminDAO.delete(uuid);
        sessionDAO.invalidate(apiKey);

        return Response.ok().build();
    }

}
