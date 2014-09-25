package cloudos.cloudstead.resources;

import lombok.Getter;
import org.cobbzilla.wizard.resources.AbstractSessionsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cloudos.cloudstead.dao.SessionDAO;
import cloudos.cloudstead.model.Admin;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(SessionsResource.ENDPOINT)
@Service
public class SessionsResource extends AbstractSessionsResource<Admin> {

    public static final String ENDPOINT = "/sessions";

    @Autowired @Getter private SessionDAO sessionDAO;

}
