package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

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
        return !id.isEmpty();
    }

    CZJob( String id ) {
        this.id = id;
    }
}
