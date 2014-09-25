package cloudos.cloudstead.dao;

import org.cobbzilla.wizard.dao.AbstractSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.server.CloudsteadConfiguration;

@Repository
public class SessionDAO extends AbstractSessionDAO<Admin> {

    @Autowired private CloudsteadConfiguration configuration;

    @Override protected Class<Admin> getEntityClass() { return Admin.class; }

    @Override protected String getPassphrase() { return configuration.getCloudConfig().getDataKey(); }

}
