package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
//import jsprit.core.problem.job.Job;
//import jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Service;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CZService extends CZJob {
    @Getter @Setter @JsonProperty("activity")
    private CZActivity activity;

    @Override
    public boolean isValid() {
        if (!super.isValid()) return false;
        if (!getActivity().isValid()) return false;
        return true;
    }

    public List<CZCoordinate> getCoordinates() {
        return Arrays.asList(getActivity().getCoordinate());
    }

    public Service toJSprit() {
        Service.Builder serviceBuilder =  Service.Builder.newInstance(getId())
            .setLocation(getActivity().getCoordinate().toJSprit());
        if (getActivity().getTimeWindow() != null) {
            serviceBuilder.setTimeWindow(getActivity().getTimeWindow().toJSprit());
        }
        return serviceBuilder.build();
    }

    CZService(
        @JsonProperty(value = "id", required = true) String id,
        @JsonProperty(value = "activity", required = true) CZActivity activity
    ) {
        super(id);
        this.activity = activity;
    }
}
