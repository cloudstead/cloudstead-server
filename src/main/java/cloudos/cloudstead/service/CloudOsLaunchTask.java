package cloudos.cloudstead.service;

import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.server.CloudsteadConfiguration;

public class CloudOsLaunchTask extends CloudsteadTask {

    public CloudOsLaunchTask(CloudOsStatus status, CloudsteadConfiguration configuration, CloudOsDAO cloudOsDAO) {
        super(status, configuration, cloudOsDAO);
    }

    @Override public CloudsteadTaskResult call() throws Exception {
        new CloudOsLauncher(status, configuration, cloudOsDAO).run();
        return result;
    }

}
