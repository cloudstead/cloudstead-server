package cloudos.cloudstead.mock.service;

import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.service.cloudos.CloudOsLaunchManager;
import cloudos.cloudstead.service.cloudos.CloudOsStatus;

import java.util.concurrent.TimeUnit;

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

    protected static void sleep() {
        try { Thread.sleep(TimeUnit.SECONDS.toMillis(4)); } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while setting up: "+e, e);
        }
    }

    public class MockCloudOsLauncher implements Runnable {

        private CloudOsStatus status;
        public MockCloudOsLauncher(CloudOsStatus status) { this.status = status; }

        @Override public void run() {
            CloudOs cloudOs = cloudOsDAO.findByName(status.getRequest().getName());
            if (cloudOs == null) {
                cloudOs = new CloudOs();
                cloudOs.setAdminUuid(status.getAdmin().getUuid());
                cloudOs.setName(status.getRequest().getName());
                try { cloudOs = cloudOsDAO.create(cloudOs); } catch (Exception e) {
                    status.error("{setup.creatingCloudOs.error}", "Error saving new CloudOs to DB");
                    throw new IllegalStateException("error saving new cloudos to DB: "+e, e);
                }
                status.setCloudOs(cloudOs);
            }

            sleep(); status.update("{setup.creatingCloudAdminAccount}");
            sleep(); status.update("{setup.savingCloudAdminAccount}");
            sleep(); status.update("{setup.startingMasterInstance}");
            sleep(); status.update("{setup.updatingCloudOsToMarkAsRunning}");
            sleep(); status.update("{setup.creatingDnsRecord}");
            sleep(); status.update("{setup.generatingSendgridCredentials}");
            sleep(); status.update("{setup.buildingInitializationFile}");
            sleep(); status.update("{setup.cheffing}");
            sleep(); status.completed();
            sleep(); status.success("{setup.success}");
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
