package io.winebox.carrozza.routes;

import com.eclipsesource.json.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.shapes.GHPoint;

import io.winebox.carrozza.services.routing.RoutingEngine;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Setter;
import spark.Request;
import spark.Response;

public final class TourController {

    private final static class Coordinate {
        private @Getter @Setter double latitude;
        private @Getter @Setter double longitude;

        @JsonIgnore
        public boolean isValid() {
            return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
        }

        @JsonCreator
        Coordinate(@JsonProperty("latitude") Number latitude, @JsonProperty("longitude") Number longitude) {
            this.latitude = latitude.doubleValue();
            this.longitude = longitude.doubleValue();
        }
    }

    private final static class CreateTourPayload {
        private @Getter @Setter List<Coordinate> coordinates;

        @JsonIgnore
        public boolean isValid() {
            if (coordinates.size() > 1) return false;
            for (final Coordinate coordinate : coordinates) if (!coordinate.isValid()) return false;
            return true;
        }

        @JsonCreator
        CreateTourPayload(@JsonProperty("points") List<Coordinate> coordinates) {
            this.coordinates = coordinates;
        }
    }

    public final static String createTour( Request request, Response response ) {
        response.type("application/json");
        final ObjectMapper mapper = new ObjectMapper();
        final CreateTourPayload payload;
        try {
            payload = mapper.readValue(request.body(), CreateTourPayload.class);
        } catch (Exception e) {
            response.status(400);
            return "{\"code\": \"BX\"}";
        }
        if (!payload.isValid()) {
            response.status(422);
            return "{\"code\": \"BM\"}";
        }
        final List<GHPoint> hopperPoints = payload.getCoordinates()
                .stream()
                .map((coordinate) -> new GHPoint(coordinate.getLatitude(), coordinate.getLongitude()))
                .collect(Collectors.toList());
        final GHRequest hopperRequest = new GHRequest(hopperPoints);
        final GraphHopper hopper = RoutingEngine.getHopper();
        final GHResponse hopperResponse = hopper.route(hopperRequest);
        if (hopperResponse.hasErrors()) {
            response.status(500);
            return "{\"code\": \"UE\"}";
        }
        final PathWrapper path = hopperResponse.getBest();
        JsonObject json = new JsonObject();
        JsonObject tourJSON = new JsonObject()
                .set("distance", path.getDistance())
                .set("time", path.getTime() / 1000.);


        JsonArray instructions = new JsonArray();
        for (Instruction instruction : path.getInstructions()) {
            JsonObject instructionJSON = new JsonObject();
            if (!instruction.getName().isEmpty()) {
                instructionJSON.set("text", instruction.getName());
            }
            instructionJSON.set("distance", instruction.getDistance());
            instructionJSON.set("time", instruction.getTime() / 1000.);
            JsonArray points = new JsonArray();
            for (GHPoint point : instruction.getPoints()) {
                JsonObject pointJSON = new JsonObject();
                pointJSON.set("latitude", point.getLat());
                pointJSON.set("longitude", point.getLat());
                points.add(pointJSON);
            }
            instructionJSON.set("points", points);

            JsonObject extra = new JsonObject();
            for (Map.Entry<String, Object> entry : instruction.getExtraInfoJSON().entrySet()) {
                extra.set(entry.getKey(), entry.getValue().toString());
            }
            instructionJSON.set("extra", extra);
            instructions.add(instructionJSON);
        }
        tourJSON.set("instructions", instructions);
        json.set("tour", tourJSON);

        response.status(200);
        return json.toString();
    }
}
