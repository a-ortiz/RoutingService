package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
//import jsprit.core.problem.vehicle.VehicleImpl;
//import jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CZVehicle {
    @Getter @Setter @JsonProperty("id")
    private String id;

    @Getter @Setter @JsonProperty("type")
    private String type;

    @Getter @Setter @JsonProperty("start_coordinate")
    private CZCoordinate startCoordinate;

    @JsonIgnore
    public boolean isValid() {
        return !getId().isEmpty() && !getType().isEmpty() && getStartCoordinate().isValid();
    }

    @JsonIgnore
    public List<CZCoordinate> getCoordinates() {
        return Arrays.asList(getStartCoordinate());
    }

    @JsonIgnore
    public VehicleImpl toJSprit(VehicleTypeImpl type ) {
        return VehicleImpl.Builder.newInstance(getId())
            .setType(type)
            .setStartLocation(getStartCoordinate().toJSprit())
            .setReturnToDepot(false)
            .build();
    }

    CZVehicle(
        @JsonProperty(value = "id", required = true) String id,
        @JsonProperty(value = "type", required = true) String type,
        @JsonProperty(value = "start_coordinate", required = true) CZCoordinate startCoordinate
    ) {
        this.id = id;
        this.type = type;
        this.startCoordinate = startCoordinate;
    }
}