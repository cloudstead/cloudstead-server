package cloudos.cloudstead.service.cloudos;

import cloudos.cloudstead.dao.CloudOsDAO;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cslib.compute.CsCloud;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.dns.DnsRecordMatch;

@AllArgsConstructor @Slf4j
public class CloudOsDestroyer implements Runnable {

    private CloudOsStatus status;
    private CloudsteadConfiguration configuration;
    private CloudOsDAO cloudOsDAO;

    @Override
    public void run() {

        final CloudOs cloudOs = status.getCloudOs();
        final String name = cloudOs.getName();
        final CsCloud cloud = configuration.getCloudConfig().buildHostedCloud(status.getAdmin().getUuid(), name);

        status.update("{destroy.tearingDownInstance}");
        try {
            cloud.teardown(cloudOs.getInstance());
        } catch (Exception e) {
            status.error("{destroy.error.tearingDownInstance}", "An error occurred during teardown");
            throw new IllegalStateException("Error tearing down instance: "+e, e);
        }

        status.update("{destroy.removingDnsRecords}");
        try {
            final String fqdn = name + "." + configuration.getCloudConfig().getDomain();
            configuration.getDnsClient().remove(new DnsRecordMatch().setSubdomain(fqdn));

        } catch (Exception e) {
            status.error("{destroy.error.removingDnsRecords}", "An error occurred removing DNS records");
            log.error("Error removing DNS records: " + e, e);
        }

        status.update("{destroy.deletingFromDB}");
        try {
            cloudOsDAO.delete(cloudOs.getUuid());
        } catch (Exception e) {
            status.error("{destroy.error.deletingFromDB}", "An error occurred deleting from the DB");
            throw new IllegalStateException("Error deleting from DB: "+e, e);
        }

        status.success("{destroy.success}");
    }
}
