package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CZService extends CZJob {
    @Getter @Setter @JsonProperty("coordinate")
    private CZCoordinate coordinate;

    @Override
    public boolean isValid() {
        if (!super.isValid()) return false;
        if (!coordinate.isValid()) return false;
        return true;
    }

    CZService(
            @JsonProperty(value = "id", required = true) String id,
            @JsonProperty(value = "coordiante", required = true) CZCoordinate coordinate
    ) {
        super(id);
        this.coordinate = coordinate;
    }
}
