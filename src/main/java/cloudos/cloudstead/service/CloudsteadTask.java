package cloudos.cloudstead.service;

import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import org.cobbzilla.wizard.task.TaskBase;

public abstract class CloudsteadTask extends TaskBase<CloudsteadTaskResult> {

    protected CloudOsStatus status;
    protected CloudsteadConfiguration configuration;
    protected CloudOsDAO cloudOsDAO;

    public CloudsteadTask(CloudOsStatus status, CloudsteadConfiguration configuration, CloudOsDAO cloudOsDAO) {
        this.status = status;
        this.configuration = configuration;
        this.cloudOsDAO = cloudOsDAO;
    }

    @Override public void init() {
        super.init();
        result.setStatus(status);
    }
}
