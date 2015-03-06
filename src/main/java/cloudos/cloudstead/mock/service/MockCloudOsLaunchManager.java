package cloudos.cloudstead.mock.service;

import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.service.cloudos.CloudOsLaunchManager;
import cloudos.cloudstead.service.cloudos.CloudOsStatus;

import java.util.concurrent.TimeUnit;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

public class MockCloudOsLaunchManager extends CloudOsLaunchManager {

    @Override protected CloudOsStatus launch(CloudOsStatus status) {
        final MockCloudOsLauncher launcher = new MockCloudOsLauncher(status);
        executor.execute(launcher);
        return status;
    }

    @Override protected void teardown(CloudOsStatus status) {
        final MockCloudOsDestroyer destroyer = new MockCloudOsDestroyer(status);
        executor.execute(destroyer);
    }

    protected static void sleep() { sleep(1); }

    protected static void sleep(int seconds) {
        try { Thread.sleep(TimeUnit.SECONDS.toMillis(seconds)); } catch (InterruptedException e) {
            die("Interrupted while setting up: "+e, e);
        }
    }

    public class MockCloudOsLauncher implements Runnable {

        private CloudOsStatus status;
        public MockCloudOsLauncher(CloudOsStatus status) { this.status = status; }

        @Override public void run() {
            CloudOs cloudOs = cloudOsDAO.findByName(status.getCloudOs().getName());
            if (cloudOs == null) {
                cloudOs = new CloudOs();
                cloudOs.setAdminUuid(status.getAdmin().getUuid());
                cloudOs.setName(status.getCloudOs().getName());
                try { cloudOs = cloudOsDAO.create(cloudOs); } catch (Exception e) {
                    status.error("{setup.creatingCloudOs.error}", "Error saving new CloudOs to DB");
                    die("error saving new cloudos to DB: " + e, e);
                }
                status.setCloudOs(cloudOs);
            }

            status.update("{setup.creatingCloudAdminAccount}");
            sleep(10); status.update("{setup.savingCloudAdminAccount}");
            sleep(15); status.update("{setup.startingMasterInstance}");
            sleep(20); status.update("{setup.updatingCloudOsToMarkAsRunning}");
            sleep(10); status.update("{setup.creatingDnsRecord}");
            sleep(15); status.update("{setup.generatingSendgridCredentials}");
            sleep(15); status.update("{setup.buildingInitializationFile}");
            sleep(10); status.update("{setup.cheffing}");
            sleep(60); status.completed();
            status.success("{setup.success}");
        }
    }

    public static class MockCloudOsDestroyer implements Runnable {
        private CloudOsStatus status;
        public MockCloudOsDestroyer(CloudOsStatus status) { this.status = status; }

        @Override public void run() {
            sleep(); status.update("{destroy.tearingDownInstance}");
            sleep(); status.update("{destroy.removingDnsRecords}");
            sleep(); status.update("{destroy.deletingFromDB}");
            sleep(); status.success("{destroy.success}");
        }
    }
}
