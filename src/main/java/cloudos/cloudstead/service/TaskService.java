package cloudos.cloudstead.service;

import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.deploy.CloudOsTaskService;
import org.springframework.stereotype.Service;

@Service public class TaskService extends CloudOsTaskService<Admin, CloudOs, CloudOsLaunchTask, CloudsteadTaskResult> {}
