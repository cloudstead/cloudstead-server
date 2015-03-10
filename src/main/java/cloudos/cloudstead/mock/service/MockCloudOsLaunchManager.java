package cloudos.cloudstead.mock.service;

import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.support.CloudOsState;
import cloudos.cloudstead.service.cloudos.CloudOsLaunchManager;
import cloudos.cloudstead.service.cloudos.CloudOsStatus;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.system.Sleep.sleep;

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

    protected void updateState(CloudOs cloudOs, CloudOsState state) {
        cloudOs.updateState(state);
        cloudOsDAO.update(cloudOs);
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
            sleep(); status.update("{setup.savingCloudAdminAccount}");
            updateState(cloudOs, CloudOsState.starting);
            sleep(); status.update("{setup.startingMasterInstance}");
            updateState(cloudOs, CloudOsState.started);
            sleep(); status.update("{setup.updatingCloudOsToMarkAsRunning}");
            sleep(); status.update("{setup.creatingDnsRecord}");
            sleep(); status.update("{setup.generatingSendgridCredentials}");
            sleep(); status.update("{setup.buildingInitializationFile}");
            updateState(cloudOs, CloudOsState.cheffing);
            sleep(); status.update("{setup.cheffing}");
            updateState(cloudOs, CloudOsState.cheffed);
            sleep(); status.completed();
            status.success("{setup.success}");
            updateState(cloudOs, CloudOsState.live);
        }
    }

    public class MockCloudOsDestroyer implements Runnable {
        private CloudOsStatus status;
        public MockCloudOsDestroyer(CloudOsStatus status) { this.status = status; }

        @Override public void run() {
            updateState(status.getCloudOs(), CloudOsState.destroying);
            sleep(); status.update("{destroy.tearingDownInstance}");
            updateState(status.getCloudOs(), CloudOsState.destroyed);

            sleep(); status.update("{destroy.removingDnsRecords}");
            sleep(); status.update("{destroy.deletingFromDB}");

            updateState(status.getCloudOs(), CloudOsState.deleting);
            cloudOsDAO.delete(status.getCloudOs().getUuid());
            sleep(); status.success("{destroy.success}");
        }
    }
}
