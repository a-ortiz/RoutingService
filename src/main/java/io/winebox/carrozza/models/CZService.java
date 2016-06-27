package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CZService extends CZJob {
    @Getter @Setter @JsonProperty("coordinate")
    private CZCoordinate coordinate;

    @Override
    public boolean isValid() {
        if (!super.isValid()) return false;
        if (!getCoordinate().isValid()) return false;
        return true;
    }

    public List<CZCoordinate> getCoordinates() {
        return Arrays.asList(getCoordinate());
    }

    public Service toJSprit() {
        return Service.Builder.newInstance(getId())
            .setLocation(getCoordinate().toJSprit())
            .build();
    }

    CZService(
            @JsonProperty(value = "id", required = true) String id,
            @JsonProperty(value = "coordinate", required = true) CZCoordinate coordinate
    ) {
        super(id);
        this.coordinate = coordinate;
    }
}
