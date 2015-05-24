package cloudos.cloudstead.model.support;

import cloudos.cslib.compute.CsCloud;
import cloudos.cslib.compute.meta.CsCloudType;
import cloudos.model.CsInstanceType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

import static cloudos.cslib.compute.meta.CsCloudTypeFactory.*;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@AllArgsConstructor
public enum CloudOsEdition {

    starter   ((Map) ImmutableMap.of(
                    DO, DO.getInstanceType("2gb"),
                    RS, RS.getInstanceType("placeholder"))),

    master    ((Map) ImmutableMap.of(
                    EC2, EC2.getInstanceType("m3.medium"),
                    DO, DO.getInstanceType("4gb"),
                    RS, RS.getInstanceType("placeholder"))),

    business   ((Map) ImmutableMap.of(
                    EC2, EC2.getInstanceType("r3.large"),
                    DO, DO.getInstanceType("8gb"),
                    RS, RS.getInstanceType("placeholder"))),

    king       ((Map) ImmutableMap.of(
                    EC2, EC2.getInstanceType("r3.xlarge"),
                    DO, DO.getInstanceType("16gb"),
                    RS, RS.getInstanceType("placeholder"))),

    emperor    ((Map) ImmutableMap.of(
                    EC2, EC2.getInstanceType("r3.2xlarge"),
                    DO, DO.getInstanceType("32gb"),
                    RS, RS.getInstanceType("placeholder"))),

    maximus    ((Map) ImmutableMap.of(
                    EC2, EC2.getInstanceType("r3.4xlarge"),
                    DO, DO.getInstanceType("48gb"),
                    RS, RS.getInstanceType("placeholder"))),

    supermax   ((Map) ImmutableMap.of(
                    EC2, EC2.getInstanceType("r3.8xlarge"),
                    DO, DO.getInstanceType("64gb"),
                    RS, RS.getInstanceType("placeholder")));

    @Getter private final Map<CsCloudType<? extends CsCloud>, CsInstanceType> instanceTypes;

    @JsonCreator public static CloudOsEdition create (String val) { return empty(val) ? null : valueOf(val.toLowerCase()); }

    public String getInstanceType(CsCloudType cloudVendor) {
        final CsInstanceType type = instanceTypes.get(cloudVendor);
        if (type != null) return type.getName();
        return die("Vendor " + cloudVendor.getName() + " not supported on edition " + this);
    }
}
