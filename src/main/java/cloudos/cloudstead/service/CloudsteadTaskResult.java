package cloudos.cloudstead.service;

import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.model.instance.CloudOsEvent;
import cloudos.model.instance.CloudOsTaskResultBase;
import lombok.NoArgsConstructor;
import org.cobbzilla.wizard.dao.DAO;

@NoArgsConstructor
public class CloudsteadTaskResult extends CloudOsTaskResultBase<Admin, CloudOs> {

    public CloudsteadTaskResult(Admin admin, CloudOs cloudOs) {
        super(admin, cloudOs);
    }

    public CloudsteadTaskResult(Admin admin, CloudOs cloudOs, DAO<CloudOsEvent> eventDAO) {
        super(admin, cloudOs, eventDAO);
    }

}
