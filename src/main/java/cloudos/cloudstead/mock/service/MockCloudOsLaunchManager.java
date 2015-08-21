package cloudos.cloudstead.mock.service;

import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.service.CloudOsLaunchManager;
import cloudos.cloudstead.service.CloudsteadTaskResult;
import cloudos.model.instance.CloudOsState;
import org.cobbzilla.wizard.task.TaskBase;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.system.Sleep.sleep;

public class MockCloudOsLaunchManager extends CloudOsLaunchManager {

    @Override public CloudsteadTaskResult launch(Admin admin, CloudOs cloudOs) {
        final MockCloudOsLauncher launcher = new MockCloudOsLauncher();
        taskService.execute(launcher);
        return launcher.getResult();
    }

    @Override public void teardown(Admin admin, CloudOs cloudOs) {
        final MockCloudOsDestroyer destroyer = new MockCloudOsDestroyer();
        taskService.execute(destroyer);
    }

    protected void updateState(CloudOs cloudOs, CloudOsState state) {
        cloudOs.updateState(state);
        cloudOsDAO.update(cloudOs);
    }

    public class MockCloudOsLauncher extends TaskBase<CloudsteadTaskResult> {

        @Override public CloudsteadTaskResult call() {
            CloudOs cloudOs = cloudOsDAO.findByName(result.getCloudOs().getName());
            if (cloudOs == null) {
                cloudOs = new CloudOs();
                cloudOs.setAdminUuid(result.getAdmin().getUuid());
                cloudOs.setName(result.getCloudOs().getName());
                try { cloudOs = cloudOsDAO.create(cloudOs); } catch (Exception e) {
                    result.error("{setup.creatingCloudOs.error}", "Error saving new CloudOs to DB");
                    die("error saving new cloudos to DB: " + e, e);
                }
                result.setCloudOs(cloudOs);
            }

            result.update("{setup.creatingCloudAdminAccount}");
            sleep(); result.update("{setup.savingCloudAdminAccount}");
            updateState(cloudOs, CloudOsState.starting);
            sleep(); result.update("{setup.startingMasterInstance}");
            updateState(cloudOs, CloudOsState.started);
            sleep(); result.update("{setup.updatingCloudOsToMarkAsRunning}");
            sleep(); result.update("{setup.creatingDnsRecord}");
            sleep(); result.update("{setup.generatingSendgridCredentials}");
            sleep(); result.update("{setup.buildingInitializationFile}");
            updateState(cloudOs, CloudOsState.cheffing);
            sleep(); result.update("{setup.cheffing}");
            updateState(cloudOs, CloudOsState.cheffed);
            sleep(); result.success("{setup.success}");
            updateState(cloudOs, CloudOsState.live);
            return result;
        }
    }

    public class MockCloudOsDestroyer extends TaskBase<CloudsteadTaskResult> {

        @Override public CloudsteadTaskResult call() {
            updateState(result.getCloudOs(), CloudOsState.destroying);
            sleep(); result.update("{destroy.tearingDownInstance}");
            updateState(result.getCloudOs(), CloudOsState.destroyed);

            sleep(); result.update("{destroy.removingDnsRecords}");
            sleep(); result.update("{destroy.deletingFromDB}");

            updateState(result.getCloudOs(), CloudOsState.deleting);
            cloudOsDAO.delete(result.getCloudOs().getUuid());
            sleep(); result.success("{destroy.success}");
            return result;
        }
    }
}
