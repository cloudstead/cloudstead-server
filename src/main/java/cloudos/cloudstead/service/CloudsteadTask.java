package cloudos.cloudstead.service;

import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.model.instance.CloudOsEvent;
import org.cobbzilla.wizard.task.TaskBase;

public abstract class CloudsteadTask extends TaskBase<CloudsteadTaskResult, CloudOsEvent> {

    protected CloudsteadConfiguration configuration;
    protected CloudOsDAO cloudOsDAO;

    public CloudsteadTask(CloudsteadConfiguration configuration, CloudOsDAO cloudOsDAO) {
        this.configuration = configuration;
        this.cloudOsDAO = cloudOsDAO;
    }

}
