package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CZVehicleType {
    @Getter @Setter @JsonProperty("id")
    private String id;

    @JsonIgnore
    public boolean isValid() {
        if (getId().isEmpty()) return false;
        return true;
    }

    @JsonIgnore
    public VehicleTypeImpl toJSprit() {
        return VehicleTypeImpl.Builder.newInstance(getId()).build();
    }

    CZVehicleType(
        @JsonProperty(value = "id", required = true) String id
    ) {
        this.id = id;
    }
}