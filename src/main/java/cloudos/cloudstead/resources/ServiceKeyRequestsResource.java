package cloudos.cloudstead.resources;

import cloudos.cloudstead.dao.ServiceKeyDAO;
import cloudos.cloudstead.model.ServiceKey;
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
@Path(ServiceKeyRequestsResource.ENDPOINT)
@Service @Slf4j
public class ServiceKeyRequestsResource {

    public static final String ENDPOINT = "/service_request";

    @Autowired private ServiceKeyDAO serviceKeyDAO;

    @POST
    public Response requestService (ServiceKeyVendorMessage request) {
        log.info("Received serviceKeyRequest: "+request.getHost());
        final ServiceKey key = new ServiceKey()
                .setHost(request.getHost())
                .setKey(request.getKey());
        serviceKeyDAO.create(key);
        return Response.ok().build();
    }

}
