package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CZShipment extends CZJob {
    @Getter @Setter @JsonProperty("pickup")
    private CZActivity pickup;

    @Getter @Setter @JsonProperty("dropoff")
    private CZActivity dropoff;

    @Override
    public boolean isValid() {
        if (!super.isValid()) return false;
        if (!getPickup().isValid()) return false;
        if (!getDropoff().isValid()) return false;
        return true;
    }

    public List<CZCoordinate> getCoordinates() {
        return Arrays.asList(getPickup().getCoordinate(), getDropoff().getCoordinate());
    }

    public Shipment toJSprit() {
        final Shipment.Builder shipmentBuilder =  Shipment.Builder.newInstance(getId())
            .setPickupLocation(getPickup().getCoordinate().toJSprit())
            .setDeliveryLocation(getDropoff().getCoordinate().toJSprit());
        if (getPickup().getTimeWindow() != null) {
            shipmentBuilder.setPickupTimeWindow(getPickup().getTimeWindow().toJSprit());
        }
        if (getDropoff().getTimeWindow() != null) {
            shipmentBuilder.setPickupTimeWindow(getDropoff().getTimeWindow().toJSprit());
        }
        return shipmentBuilder.build();
    }

    CZShipment(
        @JsonProperty(value = "id", required = true) String id,
        @JsonProperty(value = "pickup", required = true) CZActivity pickup,
        @JsonProperty(value = "dropoff", required = true) CZActivity dropoff
    ) {
        super(id);
        this.pickup = pickup;
        this.dropoff = dropoff;
    }
}
