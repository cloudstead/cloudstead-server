package cloudos.cloudstead.service;

import cloudos.cloudstead.dao.AdminDAO;
import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.dao.CloudOsEventDAO;
import cloudos.deploy.LaunchManagerBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service @Slf4j
public class CloudOsLaunchManager extends LaunchManagerBase<Admin, CloudOs, CloudsteadTaskResult, CloudOsLaunchTask>{

    @Autowired protected CloudOsDAO cloudOsDAO;
    @Autowired protected AdminDAO adminDAO;
    @Autowired protected CloudOsEventDAO eventDAO;
    @Autowired protected CloudsteadConfiguration configuration;
    @Autowired protected TaskService taskService;

    @Override protected CloudOsLaunchTask launchTask(Admin account, CloudOs instance) {
        return new CloudOsLaunchTask(account, instance, configuration, cloudOsDAO, eventDAO);
    }
}
