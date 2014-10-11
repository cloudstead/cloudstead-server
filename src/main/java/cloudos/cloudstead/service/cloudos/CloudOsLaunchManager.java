package cloudos.cloudstead.service.cloudos;

import cloudos.cloudstead.dao.AdminDAO;
import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.dao.CloudOsEventDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.support.CloudOsRequest;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service @Slf4j
public class CloudOsLaunchManager {

    @Autowired protected CloudOsDAO cloudOsDAO;
    @Autowired protected AdminDAO adminDAO;
    @Autowired protected CloudOsEventDAO eventDAO;
    @Autowired protected CloudsteadConfiguration configuration;

    protected Executor executor = Executors.newFixedThreadPool(10);

    public CloudOsStatus launch(Admin admin, CloudOsRequest request) {

        // relookup to ensure we get the full representation (password hash included)
        final Admin found = adminDAO.findByUuid(admin.getUuid());
        if (found == null) throw new IllegalArgumentException("Invalid admin: "+admin.getUuid());

        final CloudOsStatus status = new CloudOsStatus(found, request, eventDAO);
        return launch(status);

    }

    protected CloudOsStatus launch(CloudOsStatus status) {
        final CloudOsLauncher launcher = new CloudOsLauncher(status, configuration, cloudOsDAO);
        executor.execute(launcher);
        return status;
    }

    public void teardown(Admin admin, CloudOs cloudOs) {
        final CloudOsStatus status = new CloudOsStatus(admin, cloudOs, eventDAO);
        teardown(status);
    }

    protected void teardown(CloudOsStatus status) {
        final CloudOsDestroyer destroyer = new CloudOsDestroyer(status, configuration, cloudOsDAO);
        executor.execute(destroyer);
    }
}
