package cloudos.cloudstead.service;

import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cslib.compute.CsCloud;
import cloudos.dao.CloudOsEventDAO;
import cloudos.model.instance.CloudOsState;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.dns.DnsRecordMatch;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@Slf4j
public class CloudOsDestroyTask extends CloudsteadTask {

    public CloudOsDestroyTask(Admin admin,
                              CloudOs cloudOs,
                              CloudsteadConfiguration configuration,
                              CloudOsDAO cloudOsDAO, CloudOsEventDAO eventDAO) {
        super(configuration, cloudOsDAO);
        result.setAdmin(admin);
        result.setCloudOs(cloudOs);
        result.setEventDAO(eventDAO);
    }

    @Override public CloudsteadTaskResult call() throws Exception {

        final CloudOs cloudOs = result.getCloudOs();
        if (cloudOs == null) {
            result.error("{destroy.error.notFound}", "No CloudOs record found");
            die("No CloudOs record found");
        }
        if (cloudOs.getInstance() == null) {
            result.error("{destroy.error.noInstance}", "No instance found");
            die("No instance found");
        }

        final String name = cloudOs.getName();
        final CsCloud cloud = configuration.getCloudConfig().buildHostedCloud(result.getAdmin().getUuid(), cloudOs);

        result.update("{destroy.tearingDownInstance}");
        cloudOs.setState(CloudOsState.destroying);
        cloudOsDAO.update(cloudOs);
        try {
            cloud.teardown(cloudOs.getInstance());
        } catch (Exception e) {
            result.error("{destroy.error.tearingDownInstance}", "An error occurred during teardown");
            die("Error tearing down instance: "+e, e);
        }

        cloudOs.setState(CloudOsState.destroyed);
        cloudOsDAO.update(cloudOs);

        result.update("{destroy.removingDnsRecords}");
        try {
            final String fqdn = name + "." + configuration.getCloudConfig().getDomain();
            configuration.getDnsClient().remove(new DnsRecordMatch().setSubdomain(fqdn));

        } catch (Exception e) {
            result.error("{destroy.error.removingDnsRecords}", "An error occurred removing DNS records");
            log.error("Error removing DNS records: " + e, e);
        }

        result.update("{destroy.deletingFromDB}");
        try {
            cloudOsDAO.delete(cloudOs.getUuid());
        } catch (Exception e) {
            result.error("{destroy.error.deletingFromDB}", "An error occurred deleting from the DB");
            die("Error deleting from DB: "+e, e);
        }

        result.success("{destroy.success}");

        return result;
    }

}
