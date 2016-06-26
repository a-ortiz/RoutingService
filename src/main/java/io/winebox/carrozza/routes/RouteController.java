package io.winebox.carrozza.routes;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.shapes.GHPoint;
import io.winebox.carrozza.models.CZCoordinate;
import io.winebox.carrozza.services.routing.RoutingEngine;
import jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import lombok.Getter;
import lombok.Setter;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RouteController {

    private final static class Service {
        private @Getter @Setter String id;
        private @Getter @Setter CZCoordinate coordinate;

        @JsonIgnore
        public boolean isValid() {
            return !id.isEmpty() && coordinate.isValid();
        }

        @JsonCreator
        Service(@JsonProperty("id") String id, @JsonProperty("coordinate") CZCoordinate coordinate) {
            this.id = id;
            this.coordinate = coordinate;
        }
    }

    private final static class Shipment {
        private @Getter @Setter String id;
        private @Getter @Setter CZCoordinate pickupCoordinate;
        private @Getter @Setter CZCoordinate deliveryCoordinate;

        @JsonIgnore
        public boolean isValid() {
            return !id.isEmpty() && pickupCoordinate.isValid() && deliveryCoordinate.isValid();
        }

        @JsonCreator
        Shipment(@JsonProperty("id") String id, @JsonProperty("pickup_coordinate") CZCoordinate pickupCoordinate, @JsonProperty("delivery_coordinate") CZCoordinate deliveryCoordinate) {
            this.id = id;
            this.pickupCoordinate = pickupCoordinate;
            this.deliveryCoordinate = deliveryCoordinate;
        }
    }

    private final static class CreateRoutePayload {
        private @Getter @Setter List<Service> services;
        private @Getter @Setter List<Shipment> shipments;

        @JsonIgnore
        public boolean isValid() {
            if (services.isEmpty() && shipments.isEmpty()) return false;
            for (final Service service : services) if (!service.isValid()) return false;
            for (final Shipment shipment : shipments) if (!shipment.isValid()) return false;
            return true;
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
                response.status(400);
                return "{\"code\": \"BX\"}";
            }
            if (!payload.isValid()) {
                response.status(422);
                return "{\"code\": \"BM\"}";
            }
            final List<GHPoint> hopperPoints = Stream.concat(
                    payload.getServices().stream()
                            .map((service) -> service.getCoordinate()),
                    payload.getShipments().stream()
                            .map((shipment) -> Stream.of(shipment.getPickupCoordinate(), shipment.getDeliveryCoordinate()))
                            .flatMap(x -> x)
                    ).map((coordinate) -> new GHPoint(coordinate.getLatitude(), coordinate.getLongitude()))
                    .collect(Collectors.toList());

            final VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);


            return "";

//            final GHRequest hopperRequest = new GHRequest(hopperPoints);
//            final GHResponse hopperResponse = RoutingEngine.getHopper().route(hopperRequest);
//            if (hopperResponse.hasErrors()) {
//                response.status(500);
//                return "{\"code\": \"UE\"}";
//            }
//            final PathWrapper path = hopperResponse.getBest();
//            JsonObject json = new JsonObject();
//            JsonObject tourJSON = new JsonObject()
//                    .set("distance", path.getDistance())
//                    .set("time", path.getTime() / 1000.);
//
//
//            JsonArray instructions = new JsonArray();
//            for (Instruction instruction : path.getInstructions()) {
//                JsonObject instructionJSON = new JsonObject();
//                if (!instruction.getName().isEmpty()) {
//                    instructionJSON.set("text", instruction.getName());
//                }
//                instructionJSON.set("distance", instruction.getDistance());
//                instructionJSON.set("time", instruction.getTime() / 1000.);
//                JsonArray points = new JsonArray();
//                for (GHPoint point : instruction.getPoints()) {
//                    JsonObject pointJSON = new JsonObject();
//                    pointJSON.set("latitude", point.getLat());
//                    pointJSON.set("longitude", point.getLat());
//                    points.add(pointJSON);
//                }
//                instructionJSON.set("points", points);
//
//                JsonObject extra = new JsonObject();
//                for (Map.Entry<String, Object> entry : instruction.getExtraInfoJSON().entrySet()) {
//                    extra.set(entry.getKey(), entry.getValue().toString());
//                }
//                instructionJSON.set("extra", extra);
//                instructions.add(instructionJSON);
//            }
//            tourJSON.set("instructions", instructions);
//            json.set("tour", tourJSON);
//            response.status(200);
//            return json.toString();
        } catch (Exception e) {
            System.out.println(e);
            response.status(500);
            return "{\"code\": \"UE\"}";
        }
    }
}


//package io.winebox.carrozza.routes;
//
//import jsprit.analysis.toolbox.GraphStreamViewer;
//import jsprit.core.algorithm.box.SchrimpfFactory;
//import jsprit.core.problem.Location;
//import jsprit.core.problem.VehicleRoutingProblem;
//import jsprit.core.problem.job.Job;
//import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
//import jsprit.core.problem.solution.route.activity.TimeWindow;
//import jsprit.core.problem.vehicle.VehicleImpl;
//import jsprit.core.problem.vehicle.VehicleTypeImpl;
//import jsprit.core.reporting.SolutionPrinter;
//import jsprit.core.util.Solutions;
//
//import spark.Request;
//import spark.Response;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
///**
// * Created by AJ on 6/20/16.
// */
//public final class RouteController {
//
////    private final static class Coordinate {
////        private double latitude;
////        private double longitude;
////
////        public double getLatitude() {
////            return this.latitude;
////        }
////
////        public void setLatitude(double latitude) {
////            this.latitude = latitude;
////        }
////
////        public double getLongitude() {
////            return this.longitude;
////        }
////
////        public void setLongitude(double longitude) {
////            this.longitude = longitude;
////        }
////
////        public boolean isValid() {
////            return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
////        }
////
////        Coordinate(double latitude, double longitude) {
////            this.latitude = latitude;
////            this.longitude = longitude;
////        }
////    }
////
////    private final static class TimeWindow {
////        private double start;
////        private double end;
////
////        public double getStart() {
////            return this.start;
////        }
////
////        public void setStart( double start ) {
////            this.start = start;
////        }
////
////        public double getEnd() {
////            return this.end;
////        }
////
////        public void setEnd( double end ) {
////            this.end = end;
////        }
////
////        public boolean isValid() {
////            return start < end;
////        }
////
////        TimeWindow(double start, double end) {
////            this.start = start;
////            this.end = end;
////        }
////    }
////
////    private final static class Activity {
////        private Coordinate coordinate;
////        private double serviceTime;
////        private io.winebox.carrozza.routes.RouteController.TimeWindow timeWindow;
////
////        public Coordinate getCoordinate() {
////            return this.coordinate;
////        }
////
////        public void setCoordinate( Coordinate coordinate ) {
////            this.coordinate = coordinate;
////        }
////
////        public double getServiceTime() {
////            return this.serviceTime;
////        }
////
////        public void setServiceTime( double serviceTime ) {
////            this.serviceTime = serviceTime;
////        }
////
////        public io.winebox.carrozza.routes.RouteController.TimeWindow getTimeWindow() {
////            return this.timeWindow;
////        }
////
////        public void setTimeWindow( io.winebox.carrozza.routes.RouteController.TimeWindow timeWindow ) {
////            this.timeWindow = timeWindow;
////        }
////
////        public boolean isValid() {
////            return coordinate.isValid() && timeWindow.isValid() && serviceTime >= 0;
////        }
////
////        Activity(Coordinate coordinate, io.winebox.carrozza.routes.RouteController.TimeWindow timeWindow, double serviceTime) {
////            this.coordinate = coordinate;
////            this.timeWindow = timeWindow;
////            this.serviceTime = serviceTime;
////        }
////    }
////
////    private final static class Service {
////        private String id;
////        private List<String> skills;
////        private int[] dimensions;
////        private Activity activity;
////
////        public boolean isValid() {
////            return !id.isEmpty() && activity.isValid();
////        }
////    }
////
////    private final static class Shipment {
////        private String id;
////        private List<String> skills;
////        private int[] dimensions;
////        private Activity pickupActivity;
////        private Activity deliveryActivity;
////
////        public boolean isValid() {
////            return !id.isEmpty() && pickupActivity.isValid() && deliveryActivity.isValid();
////        }
////    }
////
//////    private final static class Job
////
////    private final static class VehicleType {
////        private String name;
////        private List<Vehicle> vehicles;
////
////        public String getName() {
////            return this.name;
////        }
////
////        public void setName( String name ) {
////            this.name = name;
////        }
////
//////        public List<>
////
////        public boolean isValid() {
////            return !name.isEmpty() && !vehicles.isEmpty() && vehicles.stream().allMatch((vehicle) -> vehicle.isValid());
////        }
////    }
////
////    private final static class Vehicle implements Validable {
////        private @Getter @Setter String id;
////        private @Getter @Setter Coordinate coordinate;
////
////        @Override
////        public boolean isValid() {
////            return !id.isEmpty() && coordinate.isValid();
////        }
////    }
////
////    private final static class CreateRoutePayload implements Validable {
////        private @Getter @Setter List<io.winebox.carrozza.routes.RouteController.Service> services;
////        private @Getter @Setter List<io.winebox.carrozza.routes.RouteController.Shipment> shipments;
////        private @Getter @Setter List<VehicleType> vehicleTypes;
////
////        @Override
////        public boolean isValid() {
////            return !(services.isEmpty() && shipments.isEmpty()) &&
////                !vehicleTypes.isEmpty() &&
////                services.stream().allMatch((service) -> service.isValid()) &&
////                shipments.stream().allMatch((shipment) -> shipment.isValid()) &&
////                vehicleTypes.stream().allMatch((vehicle_type) -> vehicle_type.isValid());
////        }
////    }
//
//    public final static String createRoute( Request request, Response response ) {
//        try {
//            final ObjectMapper mapper = new ObjectMapper();
//            final CreateRoutePayload payload = mapper.readValue(request.body(), CreateRoutePayload.class);
//            System.out.println(mapper.writeValueAsString(payload));
//            if (!payload.isValid()) {
//                System.out.println("Invalid");
//                response.status(400);
//                return "";
//            }
//
//            Collection<VehicleImpl> vehicles = payload.getVehicleTypes()
//                    .stream()
//                    .map((vehicle_type) -> {
//                        final VehicleTypeImpl newVehicleType = VehicleTypeImpl.Builder.newInstance(vehicle_type.getName())
//                                .
//                                .build();
//                        return vehicle_type.getVehicles()
//                                .stream()
//                                .map((vehicle) -> {
//                                    final Coordinate coordinate = vehicle.getCoordinate();
//                                    final Location location = Location.Builder.newInstance()
//                                            .setCoordinate(jsprit.core.util.Coordinate.newInstance(coordinate.getLatitude(), coordinate.getLongitude()))
//                                            .build();
//                                    final VehicleImpl newVehicle = VehicleImpl.Builder.newInstance(vehicle.getId())
//                                            .setType(newVehicleType)
//                                            .setStartLocation(location)
//                                            .setReturnToDepot(false)
//                                            .build();
//                                    return newVehicle;
//                                });
//                    })
//                    .flatMap((vehiclesByType) -> vehiclesByType)
//                    .collect(Collectors.toList());
//
//            final Stream<Job> services = payload.getServices()
//                    .stream()
//                    .map((service) -> {
//                        final Coordinate coordinate = service.getCoordinate();
//                        final Location location = Location.Builder.newInstance()
//                                .setCoordinate(jsprit.core.util.Coordinate.newInstance(coordinate.getLatitude(), coordinate.getLongitude()))
//                                .build();
//                        final jsprit.core.problem.job.Service newService = jsprit.core.problem.job.Service.Builder.newInstance(service.getId())
//                                .setLocation(location)
//
//                                .build();
//                        return newService;
//                    });
//
//            final Stream<Job> shipments = payload.getShipments()
//                    .stream()
//                    .map((shipment) -> {
//                        final Coordinate pickupCoordinate = shipment.getPickupCoordinate();
//                        final Location pickupLocation = Location.Builder.newInstance()
//                                .setCoordinate(jsprit.core.util.Coordinate.newInstance(pickupCoordinate.getLatitude(), pickupCoordinate.getLongitude()))
//                                .build();
//                        final Coordinate deliveryCoordinate = shipment.getDeliveryCoordinate();
//                        final Location deliveryLocation = Location.Builder.newInstance()
//                                .setCoordinate(jsprit.core.util.Coordinate.newInstance(deliveryCoordinate.getLatitude(), deliveryCoordinate.getLongitude()))
//                                .build();
//                        final jsprit.core.problem.job.Shipment newShipment = jsprit.core.problem.job.Shipment.Builder.newInstance(shipment.getId())
//                                .setPickupLocation(pickupLocation)
//                                .setDeliveryLocation(deliveryLocation)
//
//                                .build();
//                        return newShipment;
//                    });
//
//            final Collection<Job> jobs = Stream.concat(services, shipments).collect(Collectors.toList());
//
////            final Stream<Coordinate> coordinates = Stream.of(vehicles.stream().map((vehicle) -> vehicle.gets));
//
//            final VehicleRoutingProblem vehicleRoutingProblem = VehicleRoutingProblem.Builder.newInstance()
//                    .addAllJobs(jobs)
//                    .addAllVehicles(vehicles)
//
//                    .build();
//
//            VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(new SchrimpfFactory().createAlgorithm(vehicleRoutingProblem)
//                    .searchSolutions());
//
//
//
//            SolutionPrinter.print(vehicleRoutingProblem, bestSolution, SolutionPrinter.Print.VERBOSE);
//            new GraphStreamViewer(vehicleRoutingProblem, bestSolution).labelWith(GraphStreamViewer.Label.ID).setRenderDelay(200).display();
//
//            response.status(200);
//            response.type("application/json");
//            System.out.println(bestSolution);
//            return "";
//        } catch (JsonParseException jpe) {
//            System.out.println(jpe);
//            response.status(400);
//            return "";
//        } catch (UnrecognizedPropertyException upe) {
//            System.out.println(upe);
//            response.status(400);
//            return "";
//        } catch (Exception e) {
//            System.out.println(e);
//            response.status(500);
//            return "";
//        }
//    }
//}
