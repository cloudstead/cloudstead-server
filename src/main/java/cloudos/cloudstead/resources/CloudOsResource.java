package cloudos.cloudstead.resources;

import cloudos.cloudstead.dao.AdminDAO;
import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.dao.CloudOsEventDAO;
import cloudos.cloudstead.dao.SessionDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.CloudOsEvent;
import cloudos.cloudstead.model.support.CloudOsRequest;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cloudstead.service.cloudos.CloudOsLaunchManager;
import cloudos.cloudstead.service.cloudos.CloudOsStatus;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.wizard.resources.ResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(ApiConstants.CLOUDOS_ENDPOINT)
@Service @Slf4j
public class CloudOsResource {

    @Autowired private CloudOsDAO cloudOsDAO;
    @Autowired private SessionDAO sessionDAO;
    @Autowired private AdminDAO adminDAO;
    @Autowired private CloudOsLaunchManager launchManager;
    @Autowired private CloudOsEventDAO eventDAO;
    @Autowired private CloudsteadConfiguration configuration;

    @GET
    public Response findAll (@HeaderParam(ApiConstants.H_API_KEY) String apiKey) {

        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        final List<CloudOs> found = cloudOsDAO.findByAdmin(admin.getUuid());
        return Response.ok(found).build();
    }

    @GET
    @Path("/{name}")
    public Response find (@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                          @PathParam("name") String name) {

        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        final CloudOs cloudOs = cloudOsDAO.findByName(name);
        if (cloudOs == null) return ResourceUtil.notFound(name);
        if (!cloudOs.getAdminUuid().equals(admin.getUuid())) return ResourceUtil.forbidden();

        return Response.ok(cloudOs).build();
    }

    @PUT
    @Path("/{name}")
    public Response createOrRelaunch (@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                                      @PathParam("name") String name,
                                      CloudOsRequest request) {
        Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        // sanity check
        if (!name.equalsIgnoreCase(request.getName())) return ResourceUtil.invalid();

        // must be activated
        admin = adminDAO.findByUuid(admin.getUuid());
        if (!admin.isEmailVerified()) return ResourceUtil.invalid("setup.error.unverifiedEmail");

        // this should return quickly with a status of pending
        CloudOsStatus status = launchManager.launch(admin, request);

        return Response.ok(status).build();
    }

    @GET
    @Path("/{name}/status")
    public Response status(@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                           @PathParam("name") String name) {

        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        final CloudOs cloudOs = cloudOsDAO.findByName(name);
        if (cloudOs == null) return ResourceUtil.notFound();
        if (!cloudOs.getAdminUuid().equals(admin.getUuid())) return ResourceUtil.forbidden();

        final List<CloudOsEvent> events = eventDAO.findByCloudOs(cloudOs.getUuid());
        CloudOsStatus status = new CloudOsStatus(admin, cloudOs);
        status.setHistory(events);

        return Response.ok(status).build();
    }

    @DELETE
    @Path("/{name}")
    public Response delete (@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                            @PathParam("name") String name) {
        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();

        final CloudOs cloudOs = cloudOsDAO.findByName(name);
        if (cloudOs == null) return ResourceUtil.notFound();
        if (!cloudOs.getAdminUuid().equals(admin.getUuid())) return ResourceUtil.forbidden();

        launchManager.teardown(admin, cloudOs);

        return Response.ok(Boolean.TRUE).build();
    }

}
