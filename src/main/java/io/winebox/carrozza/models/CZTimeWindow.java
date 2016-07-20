package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.util.Coordinate;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by AJ on 7/19/16.
 */
public class CZTimeWindow {
    @Getter @Setter @JsonProperty("start")
    private double start;

    @Getter @Setter @JsonProperty("end")
    private double end;

    @JsonIgnore
    public boolean isValid() {
        return getStart() >= 0 && getEnd() >= 0 && getStart() <= getEnd();
    }

    @JsonIgnore
    public TimeWindow toJSprit() {
        return TimeWindow.newInstance(getStart(), getEnd());
    }

    CZTimeWindow(
            @JsonProperty(value = "start", required = true) double start,
            @JsonProperty(value = "end", required = true) double end
    ) {
        this.start = start;
        this.end = end;
    }
}