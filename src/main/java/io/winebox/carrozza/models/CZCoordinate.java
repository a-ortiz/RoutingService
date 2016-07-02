package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsprit.core.problem.Location;
import jsprit.core.problem.job.Service;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CZCoordinate {
    @Getter @Setter @JsonProperty("latitude")
    private double latitude;

    @Getter @Setter @JsonProperty("longitude")
    private double longitude;

    @JsonIgnore
    public boolean isValid() {
        return getLatitude() >= -90 && getLatitude() <= 90 && getLongitude() >= -180 && getLongitude() <= 180;
    }

    public Location toJSprit() {
        return Location.newInstance(getLatitude(), getLongitude());
    }

    CZCoordinate(
        @JsonProperty(value = "latitude", required = true) double latitude,
        @JsonProperty(value = "longitude", required = true) double longitude
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}