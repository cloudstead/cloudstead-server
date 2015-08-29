package cloudos.cloudstead.mock.service;

import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.server.CloudsteadConfiguration;
import cloudos.cloudstead.service.CloudOsLaunchManager;
import cloudos.cloudstead.service.CloudOsLaunchTask;
import cloudos.cloudstead.service.CloudsteadTaskResult;
import cloudos.model.instance.CloudOsState;
import org.cobbzilla.wizard.task.TaskId;
import org.springframework.beans.factory.annotation.Autowired;

import static org.cobbzilla.util.system.Sleep.sleep;

public class MockCloudOsLaunchManager extends CloudOsLaunchManager {

    @Autowired private CloudsteadConfiguration configuration;

    @Override public TaskId launch(Admin admin, CloudOs cloudOs) {
        final MockCloudOsLauncher launcher = configuration.autowire(new MockCloudOsLauncher());
        launcher.init(admin, cloudOs);
        return taskService.execute(launcher);
    }

    public class MockCloudOsLauncher extends CloudOsLaunchTask {

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
