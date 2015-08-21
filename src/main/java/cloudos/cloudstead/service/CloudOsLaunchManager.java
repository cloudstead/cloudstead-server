package cloudos.cloudstead.service;

import cloudos.cloudstead.dao.AdminDAO;
import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.dao.CloudOsEventDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service @Slf4j
public class CloudOsLaunchManager {

    @Autowired protected CloudOsDAO cloudOsDAO;
    @Autowired protected AdminDAO adminDAO;
    @Autowired protected CloudOsEventDAO eventDAO;
    @Autowired protected CloudsteadConfiguration configuration;
    @Autowired protected TaskService taskService;

    public CloudsteadTaskResult launch(Admin admin, CloudOs cloudOs) {

        // relookup to ensure we get the full representation (password hash included)
        final Admin found = adminDAO.findByUuid(admin.getUuid());
        if (found == null) throw new IllegalArgumentException("Invalid admin: "+admin.getUuid());

        final CloudOsLaunchTask launcher = new CloudOsLaunchTask(found, cloudOs, configuration, cloudOsDAO, eventDAO);
        taskService.execute(launcher);
        return launcher.getResult();
    }

    public void teardown(Admin admin, CloudOs cloudOs) {
        taskService.execute(new CloudOsDestroyTask(admin, cloudOs, configuration, cloudOsDAO, eventDAO));
    }
}
