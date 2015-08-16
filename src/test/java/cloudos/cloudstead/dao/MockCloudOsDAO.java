package cloudos.cloudstead.dao;

import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.server.CloudConfiguration;

public class MockCloudOsDAO extends CloudOsDAO {

    @Override protected String setupAws(Admin admin, CloudOs cloudOs) {

        final CloudConfiguration cloudConfig = configuration.getCloudConfig();
        final String salt = salt(cloudConfig);
        final String hostname = cloudOs.getName();

        return cloudOs.getIAMuser(admin, hostname, salt);
    }
}
