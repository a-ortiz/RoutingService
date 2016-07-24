package io.winebox.carrozza.services.routing;

import io.winebox.carrozza.models.CZCoordinate;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class CZRoutingEntry {
    @Getter @Setter
    private String id;

    @Getter @Setter
    private String valueType;

    @Getter @Setter
    private double value;

    @Getter @Setter
    private String mode;

    @Getter @Setter
    private List<CZCoordinate> points;

    public CZRoutingEntry(String id, double value, String valueType, String mode, List<CZCoordinate> points) {
        this.id = id;
        this.valueType = valueType;
        this.value = value;
        this.mode = mode;
        this.points = points;
    }
}
