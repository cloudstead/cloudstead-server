package cloudos.cloudstead.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.model.IdentifiableBase;

import javax.persistence.Entity;

@Entity @Accessors(chain=true)
public class ServiceKey extends IdentifiableBase {

    @Getter @Setter private String host;
    @Getter @Setter private String key;

}
