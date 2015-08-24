package cloudos.cloudstead.mock.service;

import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.service.CloudOsLaunchManager;
import cloudos.cloudstead.service.CloudOsLaunchTask;
import cloudos.cloudstead.service.CloudsteadTaskResult;
import cloudos.model.instance.CloudOsState;
import org.cobbzilla.wizard.task.TaskId;

import static org.cobbzilla.util.system.Sleep.sleep;

public class MockCloudOsLaunchManager extends CloudOsLaunchManager {

    @Override public TaskId launch(Admin admin, CloudOs cloudOs) {
        final MockCloudOsLauncher launcher = new MockCloudOsLauncher(admin, cloudOs);
        return taskService.execute(launcher);
    }

    protected void updateState(CloudOs cloudOs, CloudOsState state) {
        cloudOs.updateState(state);
        cloudOsDAO.update(cloudOs);
    }

    public class MockCloudOsLauncher extends CloudOsLaunchTask {

        public MockCloudOsLauncher(Admin admin, CloudOs cloudOs) {
            super(admin, cloudOs, configuration, null, null);
        }

        @Override public CloudsteadTaskResult call() {
            result.update("{setup.creatingCloudAdminAccount}");
            sleep(); result.update("{setup.savingCloudAdminAccount}");
            updateState(cloudOs(), CloudOsState.starting);
            sleep(); result.update("{setup.startingMasterInstance}");
            updateState(cloudOs(), CloudOsState.started);
            sleep(); result.update("{setup.updatingCloudOsToMarkAsRunning}");
            sleep(); result.update("{setup.creatingDnsRecord}");
            sleep(); result.update("{setup.generatingSendgridCredentials}");
            sleep(); result.update("{setup.buildingInitializationFile}");
            updateState(cloudOs(), CloudOsState.cheffing);
            sleep(); result.update("{setup.cheffing}");
            updateState(cloudOs(), CloudOsState.cheffed);
            sleep(); result.success("{setup.success}");
            updateState(cloudOs(), CloudOsState.live);
            return result;
        }

        @Override public synchronized boolean teardown() {
            updateState(cloudOs(), CloudOsState.destroying);
            updateState(cloudOs(), CloudOsState.destroyed);
            return super.teardown();
        }
    }
}
