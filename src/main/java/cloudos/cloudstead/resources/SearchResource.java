package cloudos.cloudstead.resources;

import cloudos.cloudstead.dao.AdminDAO;
import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.dao.ServiceKeyDAO;
import cloudos.cloudstead.model.support.CloudsteadEntityType;
import com.qmino.miredot.annotations.ReturnType;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.wizard.dao.DAO;
import org.cobbzilla.wizard.model.ResultPage;
import org.cobbzilla.wizard.resources.ResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(ApiConstants.SEARCH_ENDPOINT)
@Service @Slf4j
public class SearchResource {

    @Autowired private AdminDAO adminDAO;
    @Autowired private CloudOsDAO cloudOsDAO;
    @Autowired private ServiceKeyDAO serviceKeyDAO;

    /**
     * Search Cloudstead objects. Must be admin.
     *
     * @param apiKey The session ID
     * @param type   The type of report. One of 'admins', 'cloudos', or 'servicekeys'
     * @param page   The page of results to return
     * @return a SearchResults object containing the results
     */
    @POST
    @Path("/{type}")
    @ReturnType("org.cobbzilla.wizard.dao.SearchResults")
    public Response search(@HeaderParam(ApiConstants.H_API_KEY) String apiKey,
                           @PathParam("type") String type,
                           ResultPage page) {
        final DAO dao;
        switch (CloudsteadEntityType.valueOf(type)) {
            case admin: dao = adminDAO; break;
            case cloudos: dao = cloudOsDAO; break;
            case servicekey: dao = serviceKeyDAO; break;
            default: return ResourceUtil.invalid("err.search.type.invalid");
        }
        return Response.ok(dao.search(page)).build();
    }

}