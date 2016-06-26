package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CZShipment extends CZJob {
    @Getter @Setter @JsonProperty("pickup_coordinate")
    private CZCoordinate pickupCoordinate;

    @Getter @Setter @JsonProperty("delivery_coordinate")
    private CZCoordinate deliveryCoordinate;

    @Override
    public boolean isValid() {
        if (!super.isValid()) return false;
        if (!pickupCoordinate.isValid()) return false;
        if (!deliveryCoordinate.isValid()) return false;
        return true;
    }

    CZShipment(
            @JsonProperty(value = "id", required = true) String id,
            @JsonProperty(value = "pickup_coordinate", required = true) CZCoordinate pickupCoordinate,
            @JsonProperty(value = "delivery_coordinate", required = true)  CZCoordinate deliveryCoordinate
    ) {
        super(id);
        this.pickupCoordinate = pickupCoordinate;
        this.deliveryCoordinate = deliveryCoordinate;
    }
}
