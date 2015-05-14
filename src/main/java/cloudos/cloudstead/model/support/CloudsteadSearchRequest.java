package cloudos.cloudstead.model.support;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.wizard.model.ResultPage;

public class CloudsteadSearchRequest {

    @Getter @Setter private CloudsteadEntityType type;
    @Getter @Setter private ResultPage page = new ResultPage();

}
