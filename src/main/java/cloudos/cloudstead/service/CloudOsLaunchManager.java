package cloudos.cloudstead.service;

import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.deploy.LaunchManagerBase;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CloudOsLaunchManager extends LaunchManagerBase<Admin, CloudOs, CloudsteadTaskResult, CloudOsLaunchTask> {

    @Autowired @Getter private CloudsteadConfiguration configuration;
    @Autowired @Getter private CloudOsDAO cloudOsDAO;

}
