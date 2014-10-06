package cloudos.cloudstead.resources;

import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.dao.AccountBaseDAO;
import cloudos.resources.AccountAuthResource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.mail.service.TemplatedMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(ApiConstants.AUTH_ENDPOINT)
@Service @Slf4j
public class AuthResource extends AccountAuthResource<Admin> {

    @Autowired @Getter(value=AccessLevel.PROTECTED) protected AccountBaseDAO<Admin> accountBaseDAO;
    @Autowired @Getter(value=AccessLevel.PROTECTED) protected TemplatedMailService templatedMailService;

    @Autowired private CloudsteadConfiguration configuration;

    @Override protected String getResetPasswordUrl(String token) { return configuration.getResetPasswordUrl(token); }

}
