package cloudos.cloudstead.service.cloudos;

import lombok.AllArgsConstructor;
import org.cobbzilla.util.system.CommandProgressCallback;
import org.cobbzilla.util.system.CommandProgressMarker;

@AllArgsConstructor
public class CloudOsLaunchProgressCallback implements CommandProgressCallback {

    private CloudOsStatus status;

    @Override public void updateProgress(CommandProgressMarker marker) {
        status.update("{setup.cheffing.percent_done_"+marker.getPercent()+"}");
    }

}
