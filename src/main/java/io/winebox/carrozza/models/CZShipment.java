package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CZShipment extends CZJob {
    @Getter @Setter @JsonProperty("pickup_coordinate")
    private CZCoordinate pickupCoordinate;

    @Getter @Setter @JsonProperty("delivery_coordinate")
    private CZCoordinate deliveryCoordinate;

    @Override
    public boolean isValid() {
        if (!super.isValid()) return false;
        if (!getPickupCoordinate().isValid()) return false;
        if (!getDeliveryCoordinate().isValid()) return false;
        return true;
    }

    public List<CZCoordinate> getCoordinates() {
        return Arrays.asList(getPickupCoordinate(), getDeliveryCoordinate());
    }

    public Shipment toJSprit() {
        return Shipment.Builder.newInstance(getId())
                .setPickupLocation(getPickupCoordinate().toJSprit())
                .setDeliveryLocation(getDeliveryCoordinate().toJSprit())
                .build();
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
