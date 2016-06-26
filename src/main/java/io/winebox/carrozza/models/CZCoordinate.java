package io.winebox.carrozza.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CZCoordinate {
    private @Getter @Setter double latitude;
    private @Getter @Setter double longitude;

    @JsonIgnore
    public boolean isValid() {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    @JsonCreator
    CZCoordinate(@JsonProperty("latitude") Number latitude, @JsonProperty("longitude") Number longitude) {
        this.latitude = latitude.doubleValue();
        this.longitude = longitude.doubleValue();
    }
}