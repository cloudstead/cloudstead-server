package cloudos.cloudstead.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.model.IdentifiableBase;

import javax.persistence.Entity;

@Entity @Accessors(chain=true)
public class CloudOsEvent extends IdentifiableBase {

    @Getter @Setter private String cloudOsUuid;
    @Getter @Setter private String messageKey;

    @JsonIgnore public boolean isCompleted() { return messageKey != null && messageKey.contains(".completed"); }
    @JsonIgnore public boolean isSuccess() { return messageKey != null && messageKey.contains(".success"); }
    @JsonIgnore public boolean isError () { return messageKey == null || messageKey.contains(".error."); }

    @JsonIgnore public long getTimestamp () { return getCtime(); }

}
