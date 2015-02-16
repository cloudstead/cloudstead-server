package cloudos.cloudstead.service.cloudos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.wizard.validation.SimpleViolationException;
import cloudos.cloudstead.dao.CloudOsEventDAO;
import cloudos.cloudstead.model.Admin;
import cloudos.cloudstead.model.CloudOs;
import cloudos.cloudstead.model.CloudOsEvent;
import cloudos.cloudstead.model.support.CloudOsRequest;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor @Slf4j
public class CloudOsStatus {

    @Getter private Admin admin;
    @Getter @Setter private CloudOs cloudOs;
    @Getter private CloudOsRequest request;

    private CloudOsEventDAO eventDAO;
    @Getter @Setter private List<CloudOsEvent> history = new ArrayList<>();

    @Getter private SimpleViolationException exception;

    public void initRetry() { exception = null; }

    public boolean hasError() {
        return exception != null || (!history.isEmpty() && getMostRecentEvent().getMessageKey().contains(".error."));
    }

    public String getErrorMessageKey () {
        return hasError() && !history.isEmpty() ? getMostRecentEvent().getMessageKey() : null;
    }
    public void setErrorMessageKey (String ignored) {}  // so json won't complain

    public CloudOsStatus(Admin admin, CloudOs cloudOs) {
        this.admin = admin;
        this.cloudOs = cloudOs;
    }

    public CloudOsStatus(Admin admin, CloudOs cloudOs, CloudOsEventDAO eventDAO) {
        this.admin = admin;
        this.cloudOs = cloudOs;
        this.eventDAO = eventDAO;
    }

    public CloudOsStatus(Admin admin, CloudOsRequest request, CloudOsEventDAO eventDAO) {
        this.admin = admin;
        this.request = request;
        this.eventDAO = eventDAO;
    }

    public void update (String statusMessageKey) {
        final CloudOsEvent event = new CloudOsEvent().setCloudOsUuid(cloudOs.getUuid()).setMessageKey(statusMessageKey);
        eventDAO.create(event);
        this.history.add(event);
    }

    public void success (String statusMessageKey) {
        update(statusMessageKey);
    }

    public void error(String messageKey, String message, String invalidValue) {
        exception = new SimpleViolationException(messageKey, message, invalidValue);
        update(messageKey);
    }

    public void error(String messageKey, String message) {
        log.error("ERROR: "+messageKey+" "+message);
        error(messageKey, message, null);
    }

    @JsonIgnore public boolean isCompleted() {
        final CloudOsEvent event = getMostRecentEvent();
        return event != null && (event.isCompleted() || event.isSuccess() || event.isError());
    }

    @JsonIgnore public boolean isSuccess() {
        final CloudOsEvent event = getMostRecentEvent();
        return event != null && event.isSuccess();
    }

    @JsonIgnore public boolean isError() {
        final CloudOsEvent event = getMostRecentEvent();
        return event != null && event.isError();
    }

    @JsonIgnore public CloudOsEvent getMostRecentEvent() {
        return history.isEmpty() ? null : history.get(history.size() - 1);
    }

    public void completed() { update("{setup.completed}"); }

}
