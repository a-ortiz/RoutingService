package io.winebox.carrozza.routes;

import com.eclipsesource.json.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.Translation;
import com.graphhopper.util.shapes.GHPoint;

import io.winebox.carrozza.models.CZCoordinate;
import io.winebox.carrozza.services.routing.RoutingEngine;

import java.util.*;

import lombok.Getter;
import lombok.Setter;
import spark.Request;
import spark.Response;

public final class TourController {

    private final static class CreateTourPayload {
        @Getter @Setter @JsonProperty("points")
        private List<CZCoordinate> coordinates;

        @JsonIgnore
        private boolean isValid() {
            if (coordinates.size() <= 1) return false;
            for (final CZCoordinate coordinate : coordinates) {
                if (!coordinate.isValid()) {
                    return false;
                }
            }
            return true;
        }

        CreateTourPayload(
                @JsonProperty(value = "points", required = true) List<CZCoordinate> coordinates
        ) {
            this.coordinates = coordinates;
        }
    }

    public static String createTour(Request request, Response response ) {
        try {
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

            final List<GHPoint> hopperPoints = new ArrayList();
            for (CZCoordinate coordinate : payload.getCoordinates()) {
                hopperPoints.add(new GHPoint(coordinate.getLatitude(), coordinate.getLongitude()));
            }
            final GraphHopper hopper = RoutingEngine.getHopper();
            final GHRequest hopperRequest = new GHRequest(hopperPoints);
            final GHResponse hopperResponse = hopper.route(hopperRequest);
            if (hopperResponse.hasErrors()) {
                response.status(500);
                return "{\"code\": \"UE\"}";
            }

            final Translation translation = hopper.getTranslationMap().getWithFallBack(hopperRequest.getLocale());

            final PathWrapper path = hopperResponse.getBest();
            final JsonArray instructions = new JsonArray();
            for (Instruction instruction : path.getInstructions()) {
                final JsonArray points = new JsonArray();
                for (GHPoint point : instruction.getPoints()) {
                    points.add(new JsonObject()
                        .set("latitude", point.getLat())
                        .set("longitude", point.getLat()));
                }
                instructions.add(new JsonObject()
                    .set("text", Helper.firstBig(instruction.getTurnDescription(translation)))
                    .set("distance", instruction.getDistance())
                    .set("time", instruction.getTime() / 1000.)
                    .set("points", points));
            }
            final JsonObject json = new JsonObject()
                .set("tour", new JsonObject()
                    .set("distance", path.getDistance())
                    .set("time", path.getTime() / 1000.)
                    .set("instructions", instructions)
                );
            return json.toString();
        } catch (Exception e) {
            response.status(500);
            return "{\"code\": \"UE\"}";
        }
    }
}
