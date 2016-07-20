package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CZActivity {
    @Getter @Setter @JsonProperty("coordinate")
    private CZCoordinate coordinate;

    @Getter @Setter @JsonProperty("time_window")
    private CZTimeWindow timeWindow;

    @JsonIgnore
    public boolean isValid() {
        if (!getCoordinate().isValid()) return false;
        if (getTimeWindow() != null && !getTimeWindow().isValid()) return false;
        return true;
    }

    CZActivity(
        @JsonProperty(value = "coordinate", required = true) CZCoordinate coordinate,
        @JsonProperty(value = "time_window") CZTimeWindow timeWindow
    ) {
        this.coordinate = coordinate;
        this.timeWindow = timeWindow;
    }
}
