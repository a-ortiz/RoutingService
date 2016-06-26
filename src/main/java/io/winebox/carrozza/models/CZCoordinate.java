package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    CZCoordinate(
            @JsonProperty(value = "latitude", required = true) double latitude,
            @JsonProperty(value = "longitude", required = true) double longitude
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}