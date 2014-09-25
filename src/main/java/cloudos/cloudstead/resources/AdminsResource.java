package cloudos.cloudstead.resources;

import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.wizard.model.HashedPassword;
import org.cobbzilla.wizard.resources.ResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cloudos.cloudstead.dao.AdminDAO;
import cloudos.cloudstead.dao.SessionDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.support.AdminRequest;
import cloudos.cloudstead.model.support.AdminResponse;
import cloudos.cloudstead.model.support.LoginRequest;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static cloudos.cloudstead.resources.ApiConstants.H_API_KEY;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(AdminsResource.ENDPOINT)
@Service @Slf4j
public class AdminsResource {

    public static final String ENDPOINT = "/admins";

    @Autowired private AdminDAO adminDAO;
    @Autowired private SessionDAO sessionDAO;

    @GET
    @Path("/{uuid}")
    public Response find (@HeaderParam(H_API_KEY) String apiKey,
                          @PathParam("uuid") String uuid) {

        Admin caller = sessionDAO.find(apiKey);
        if (caller == null || !caller.getUuid().equals(uuid)) return ResourceUtil.forbidden();

        final Admin admin = adminDAO.findByUuid(uuid);
        if (admin == null) return ResourceUtil.notFound();

        sessionDAO.update(apiKey, admin); // just in case it changed underneath us, update session cache

        return Response.ok(admin).build();
    }

    @POST
    public Response create(@Valid AdminRequest request) {

        Admin admin = populate(request, new Admin());

        admin = adminDAO.create(admin);

        final AdminResponse adminResponse = new AdminResponse(admin, sessionDAO.create(admin));
        return Response.ok(adminResponse).build();
    }

    private Admin populate(AdminRequest request, Admin admin) {
        admin.setEmail(request.getEmail());
        admin.setMobilePhone(request.getMobilePhone());
        admin.setPassword(new HashedPassword(request.getPassword()));
        return admin;
    }

    @PUT
    public Response login(@Valid LoginRequest request) {

        final Admin admin = adminDAO.findByEmail(request.getEmail());
        if (admin == null || !admin.getPassword().isCorrectPassword(request.getPassword())) {
            return ResourceUtil.notFound();
        }

        final AdminResponse adminResponse = new AdminResponse(admin, sessionDAO.create(admin));
        return Response.ok(adminResponse).build();
    }

    @POST
    @Path("/{uuid}")
    public Response update(@HeaderParam(H_API_KEY) String apiKey,
                           @PathParam("uuid") String uuid,
                           @Valid AdminRequest request) {

        if (!uuid.equals(request.getUuid())) return ResourceUtil.invalid();

        Admin caller = sessionDAO.find(apiKey);
        if (caller == null || !caller.getUuid().equals(uuid)) return ResourceUtil.forbidden();

        Admin admin = adminDAO.findByUuid(uuid);
        if (admin == null) return ResourceUtil.notFound();

        admin = populate(request, admin);
        admin = adminDAO.update(admin);
        sessionDAO.update(apiKey, admin);

        return Response.ok(admin).build();
    }

    @DELETE
    @Path("/{uuid}")
    public Response delete(@HeaderParam(H_API_KEY) String apiKey,
                           @PathParam("uuid") String uuid) {

        Admin caller = sessionDAO.find(apiKey);
        if (caller == null || !caller.getUuid().equals(uuid)) return ResourceUtil.forbidden();

        adminDAO.delete(uuid);
        sessionDAO.invalidate(apiKey);

        return Response.ok().build();
    }

}
