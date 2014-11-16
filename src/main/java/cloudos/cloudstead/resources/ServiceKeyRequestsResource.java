package cloudos.cloudstead.resources;

import cloudos.cloudstead.dao.ServiceKeyDAO;
import cloudos.cloudstead.model.ServiceKey;
import com.qmino.miredot.annotations.ReturnType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rooty.toots.service.ServiceKeyVendorMessage;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(ApiConstants.SERVICEKEYS_ENDPOINT)
@Service @Slf4j
public class ServiceKeyRequestsResource {

    @Autowired private ServiceKeyDAO serviceKeyDAO;

    /**
     * Called by a CloudOs instance to request service assistance.
     * @param request The service key request. Tells us the hostname and the SSH key to use to connect.
     * @return Just an HTTP response
     * @statuscode 200 in all cases
     */
    @POST
    @ReturnType("java.lang.Void")
    public Response requestService (ServiceKeyVendorMessage request) {
        log.info("Received serviceKeyRequest: "+request.getHost());
        final ServiceKey key = new ServiceKey()
                .setHost(request.getHost())
                .setKey(request.getKey());
        serviceKeyDAO.create(key);
        return Response.ok().build();
    }

}
