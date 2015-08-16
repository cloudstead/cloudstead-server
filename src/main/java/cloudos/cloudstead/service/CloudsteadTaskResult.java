package cloudos.cloudstead.service;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.wizard.task.TaskResult;

public class CloudsteadTaskResult extends TaskResult {

    @Getter @Setter private CloudOsStatus status;

}
