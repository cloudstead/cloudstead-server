package cloudos.cloudstead.resources;

import cloudos.cloudstead.dao.SessionDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.support.CloudOsAppBundle;
import cloudos.cloudstead.model.support.CloudOsEdition;
import cloudos.cloudstead.model.support.CloudOsGeoRegion;
import com.qmino.miredot.annotations.ReturnType;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.wizard.resources.ResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(ApiConstants.INSTANCE_OPTIONS_ENDPOINT)
@Service @Slf4j
public class InstanceOptionsResource {

    @Autowired private SessionDAO sessionDAO;

    /**
     * Get a list of all available app bundles
     * @return A List of available CloudOsAppBundles
     */
    @GET
    @Path("/bundles")
    @ReturnType("java.util.List<cloudos.cloudstead.model.support.CloudOsAppBundle>")
    public Response findAllAppBundles(@HeaderParam(ApiConstants.H_API_KEY) String apiKey) {
        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();
        return Response.ok(CloudOsAppBundle.values()).build();
    }

    /**
     * Get a list of all available cloudstead editions
     * @return A List of available CloudOsEditions
     */
    @GET
    @Path("/editions")
    @ReturnType("java.util.List<cloudos.cloudstead.model.support.CloudOsEdition>")
    public Response findAllEditions(@HeaderParam(ApiConstants.H_API_KEY) String apiKey) {
        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();
        return Response.ok(CloudOsEdition.values()).build();
    }

    /**
     * Get a list of all regions that a cloudstead can be launched in
     * @return A List of available CloudOsGeoRegions
     */
    @GET
    @Path("/regions")
    @ReturnType("java.util.List<cloudos.cloudstead.model.support.CloudOsEdition>")
    public Response findAllRegions(@HeaderParam(ApiConstants.H_API_KEY) String apiKey) {
        final Admin admin = sessionDAO.find(apiKey);
        if (admin == null) return ResourceUtil.forbidden();
        return Response.ok(CloudOsGeoRegion.values()).build();
    }

}