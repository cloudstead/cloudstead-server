package cloudos.cloudstead.service;

import cloudos.dao.CloudOsEventDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.model.instance.CloudOsStatusBase;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor @Slf4j
public class CloudOsStatus extends CloudOsStatusBase<Admin, CloudOs> {

    public CloudOsStatus(Admin admin, CloudOs cloudOs) { super(admin, cloudOs); }

    public CloudOsStatus(Admin admin, CloudOs cloudOs, CloudOsEventDAO eventDAO) { super(admin, cloudOs, eventDAO); }

}
