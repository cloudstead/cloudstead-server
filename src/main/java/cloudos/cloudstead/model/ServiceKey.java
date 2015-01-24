package cloudos.cloudstead.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Size;

@Entity @Accessors(chain=true)
public class ServiceKey extends IdentifiableBase {

    @HasValue(message="err.host.empty")
    @Column(length=1024, nullable=false, updatable=false)
    @Size(max=1024)
    @Getter @Setter private String host;

    @HasValue(message="err.key.empty")
    @Column(length=4096, unique=true, nullable=false, updatable=false)
    @Size(max=4096)
    @Getter @Setter private String key;

}
