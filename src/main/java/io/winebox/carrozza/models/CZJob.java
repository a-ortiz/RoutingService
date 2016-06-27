package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.*;
import jsprit.core.problem.job.Job;
import lombok.*;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CZService.class, name = "service"),
    @JsonSubTypes.Type(value = CZShipment.class, name = "shipment")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CZJob {
    @Getter @Setter
    private String id;

    @JsonIgnore
    public boolean isValid() {
        return !getId().isEmpty();
    }

    public abstract List<CZCoordinate> getCoordinates();

    public abstract Job toJSprit();

    CZJob( String id ) {
        this.id = id;
    }
}
