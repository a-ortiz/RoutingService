package io.winebox.carrozza.routes;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.SchrimpfFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.shapes.GHPoint;
import io.winebox.carrozza.models.*;
import io.winebox.carrozza.services.routing.RoutingEngine;
//import jsprit.analysis.toolbox.GraphStreamViewer;
//import jsprit.core.algorithm.VehicleRoutingAlgorithm;
//import jsprit.core.algorithm.box.SchrimpfFactory;
//import jsprit.core.problem.Location;
//import jsprit.core.problem.Skills;
//import jsprit.core.problem.VehicleRoutingProblem;
//import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
//import jsprit.core.problem.job.Break;
//import jsprit.core.problem.job.Job;
//import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
//import jsprit.core.problem.solution.route.VehicleRoute;
//import jsprit.core.problem.solution.route.activity.TourActivity;
//import jsprit.core.problem.vehicle.VehicleImpl;
//import jsprit.core.problem.vehicle.VehicleType;
//import jsprit.core.problem.vehicle.VehicleTypeImpl;
//import jsprit.core.reporting.SolutionPrinter;
//import jsprit.core.util.Solutions;
//import jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import lombok.Getter;
import lombok.Setter;
import spark.Request;
import spark.Response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public final class RouteController {

    private final static class CreateRoutePayload {
        @Getter @Setter @JsonProperty("vehicle_types")
        private List<CZVehicleType> vehicleTypes;

        @Getter @Setter @JsonProperty("vehicles")
        private List<CZVehicle> vehicles;

        @Getter @Setter @JsonProperty("jobs")
        private List<CZJob> jobs;

        @JsonIgnore
        private boolean isValid() {
            if (getVehicleTypes().isEmpty()) return false;
            for (final CZVehicleType vehicleType : getVehicleTypes()) {
                if (!vehicleType.isValid()) {
                    return false;
                }
            }
            if (getVehicles().isEmpty()) return false;
            for (final CZVehicle vehicle : getVehicles()) {
                if (!vehicle.isValid()) {
                    return false;
                }
            }
            if (getJobs().isEmpty()) return false;
            for (final CZJob job : getJobs()) {
                if (!job.isValid()) {
                    return false;
                }
            }
            return true;
        }

        CreateRoutePayload(
                @JsonProperty(value = "vehicle_types", required = true) List<CZVehicleType> vehicleTypes,
                @JsonProperty(value = "vehicles", required = true) List<CZVehicle> vehicles,
                @JsonProperty(value = "jobs", required = true) List<CZJob> jobs
        ) {
            this.vehicleTypes = vehicleTypes;
            this.vehicles = vehicles;
            this.jobs = jobs;
        }
    }

    public final static String createRoute(Request request, Response response ) {
        try {
            response.type("application/json");
            final ObjectMapper mapper = new ObjectMapper();
            final CreateRoutePayload payload;
            try {
                payload = mapper.readValue(request.body(), CreateRoutePayload.class);
            } catch (Exception e) {
                System.out.println(e);
                response.status(400);
                return "{\"code\": \"BX\"}";
            }
            if (!payload.isValid()) {
                response.status(422);
                return "{\"code\": \"BM\"}";
            }

            final Map<Location, GHPoint> points = new HashMap();
            final Map<String, VehicleTypeImpl> vehicleTypes = new HashMap();
            for (final CZVehicleType vehicleType : payload.getVehicleTypes())  {
                vehicleTypes.put(vehicleType.getId(), vehicleType.toJSprit());
            }
            final List<VehicleImpl> vehicles = new ArrayList();
            for (final CZVehicle vehicle : payload.getVehicles()) {
                for (CZCoordinate coordinate : vehicle.getCoordinates()) {
                    points.put(coordinate.toJSprit(), new GHPoint(coordinate.getLatitude(), coordinate.getLongitude()));
                }
                VehicleTypeImpl vehicleType = vehicleTypes.get(vehicle.getType());
                vehicles.add(vehicle.toJSprit(vehicleType));
            }
            final List<Job> jobs = new ArrayList();
            for (final CZJob job : payload.getJobs()) {
                for (CZCoordinate coordinate : job.getCoordinates()) {
                    points.put(coordinate.toJSprit(), new GHPoint(coordinate.getLatitude(), coordinate.getLongitude()));
                }
                jobs.add(job.toJSprit());
            }
            final VehicleRoutingTransportCostsMatrix.Builder costsMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
            for (final Map.Entry<Location, GHPoint> fromEntry : points.entrySet()) {
                for (final Map.Entry<Location, GHPoint> toEntry : points.entrySet()) {
                    if (fromEntry.getKey().equals(toEntry.getKey())) {
                        costsMatrixBuilder.addTransportDistance(fromEntry.getKey().getId(), toEntry.getKey().getId(), 0);
                        costsMatrixBuilder.addTransportDistance(fromEntry.getKey().getId(), toEntry.getKey().getId(), 0);
                        continue;
                    }
                    final GHRequest hopperRequest = new GHRequest(fromEntry.getValue(), toEntry.getValue());
                    final GHResponse hopperResponse = RoutingEngine.getHopper().route(hopperRequest);
                    if (hopperResponse.hasErrors()) {
                        response.status(500);
                        return "{\"code\": \"UE\"}";
                    }
                    final PathWrapper path = hopperResponse.getBest();
                    costsMatrixBuilder.addTransportDistance(fromEntry.getKey().getId(), toEntry.getKey().getId(), path.getDistance() / 1000);
                    costsMatrixBuilder.addTransportTime(fromEntry.getKey().getId(), toEntry.getKey().getId(), path.getTime() / 1000 / 60);
                }
            }

            final VehicleRoutingTransportCostsMatrix costsMatrix = costsMatrixBuilder.build();
            final VehicleRoutingProblem vehicleRoutingProblem = VehicleRoutingProblem.Builder.newInstance()
                .setRoutingCost(costsMatrix)
                .addAllJobs(jobs)
                .addAllVehicles(vehicles)
                .build();

            final VehicleRoutingAlgorithm algorithm = new SchrimpfFactory().createAlgorithm(vehicleRoutingProblem);
            final Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
            final VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

            final JsonArray routes = new JsonArray();
            for (final VehicleRoute route : solution.getRoutes()) {
                double costs = 0;
                TourActivity previousActivity = route.getStart();
                final JsonArray activities = new JsonArray();
                activities.add(new JsonObject()
                    .set("job_id", Json.NULL)
                    .set("coordinate", new JsonObject()
                        .set("latitude", route.getStart().getLocation().getCoordinate().getX())
                        .set("longitude", route.getStart().getLocation().getCoordinate().getY())
                    )
                    .set("activity", route.getStart().getName())
                    .set("time", new JsonObject()
                        .set("start", Json.NULL)
                        .set("end", Math.round(route.getStart().getEndTime()))
                    )
                    .set("cost", 0)
                );
                for (final TourActivity activity : route.getActivities()) {
                    final String jobId;
                    if (activity instanceof TourActivity.JobActivity) {
                        jobId = ((TourActivity.JobActivity) activity).getJob().getId();
                    } else {
                        jobId = null;
                    }
                    double cost = vehicleRoutingProblem.getTransportCosts().getTransportCost(previousActivity.getLocation(), activity.getLocation(), previousActivity.getEndTime(), route.getDriver(),
                            route.getVehicle());
                    cost += vehicleRoutingProblem.getActivityCosts().getActivityCost(activity, activity.getArrTime(), route.getDriver(), route.getVehicle());
                    costs += cost;
                    final String activityName;
                    switch (activity.getName()) {
                        case "pickupShipment": activityName = "pickup"; break;
                        case "deliverShipment": activityName = "dropoff"; break;
                        default: activityName = activity.getName(); break;
                    }
                    activities.add(new JsonObject()
                        .set("job_id", jobId)
                        .set("coordinate", new JsonObject()
                            .set("latitude", activity.getLocation().getCoordinate().getX())
                            .set("longitude", activity.getLocation().getCoordinate().getY())
                        )
                        .set("activity", activityName)
                        .set("time", new JsonObject()
                            .set("start", Math.round(activity.getArrTime()))
                            .set("end", Math.round(activity.getEndTime()))
                        )
                        .set("cost", new BigDecimal(cost).setScale(2, RoundingMode.HALF_UP).doubleValue())
                    );
                    previousActivity = activity;
                }
                double cost = vehicleRoutingProblem.getTransportCosts().getTransportCost(previousActivity.getLocation(), route.getEnd().getLocation(), previousActivity.getEndTime(),
                        route.getDriver(), route.getVehicle());
                cost += vehicleRoutingProblem.getActivityCosts().getActivityCost(route.getEnd(), route.getEnd().getArrTime(), route.getDriver(), route.getVehicle());
                costs += cost;
                activities.add(new JsonObject()
                    .set("job_id", Json.NULL)
                    .set("coordinate", new JsonObject()
                        .set("latitude", route.getEnd().getLocation().getCoordinate().getX())
                        .set("longitude", route.getEnd().getLocation().getCoordinate().getY())
                    )
                    .set("activity", route.getEnd().getName())
                    .set("time", new JsonObject()
                        .set("start", Math.round(route.getEnd().getArrTime()))
                        .set("end", Json.NULL)
                    )
                    .set("cost", new BigDecimal(cost).setScale(2, RoundingMode.HALF_UP).doubleValue())
                );
                routes.add(new JsonObject()
                    .set("vehicle_id", route.getVehicle().getId())
                    .set("cost", new BigDecimal(costs).setScale(2, RoundingMode.HALF_UP).doubleValue())
                    .set("activities", activities)
                );
            }
            final JsonArray unassignedJobIds = new JsonArray();
            for (final Job job : solution.getUnassignedJobs()) {
                unassignedJobIds.add(job.getId());
            }
            final JsonObject json = new JsonObject()
                .set("cost", new BigDecimal(solution.getCost()).setScale(2, RoundingMode.HALF_UP).doubleValue())
                .set("unassigned_job_ids", unassignedJobIds)
                .set("routes", routes);
            return json.toString();
        } catch (Exception e) {
            System.out.println(e);
            response.status(500);
            return "{\"code\": \"UE\"}";
        }
    }
}