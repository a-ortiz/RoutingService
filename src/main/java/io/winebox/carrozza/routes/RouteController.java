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
import io.winebox.carrozza.models.CZJob;
import io.winebox.carrozza.models.CZService;
import io.winebox.carrozza.models.CZShipment;
import io.winebox.carrozza.services.routing.RoutingEngine;
import jsprit.analysis.toolbox.GraphStreamViewer;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;
import jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import lombok.Getter;
import lombok.Setter;
import spark.Request;
import spark.Response;

import java.util.*;

public final class RouteController {

    private final static class CreateRoutePayload {
        @Getter @Setter @JsonProperty("jobs")
        private List<CZJob> jobs;

        @JsonIgnore
        private boolean isValid() {
            if (jobs.isEmpty()) return false;
            for (final CZJob job : jobs) {
                if (!job.isValid()) {
                    return false;
                }
            }
            return true;
        }

        CreateRoutePayload(
                @JsonProperty(value = "jobs", required = true) List<CZJob> jobs
        ) {
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

            VehicleTypeImpl vehicleType1 = VehicleTypeImpl.Builder.newInstance("car")
                    .build();

            VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("car_1")
                    .setType(vehicleType1)
                    .setStartLocation(Location.newInstance(-2.114648, -79.869998))
                    .build();

            final List<Job> jobs = new ArrayList();
            final Map<Location, GHPoint> points = new HashMap();
            for (CZJob job : payload.getJobs()) {
                for (CZCoordinate coordinate : job.getCoordinates()) {
                    points.put(coordinate.toJSprit(), new GHPoint(coordinate.getLatitude(), coordinate.getLongitude()));
                }
                jobs.add(job.toJSprit());
            }
            points.put(Location.newInstance(-2.114648, -79.869998), new GHPoint(-2.114648, -79.869998));
            final VehicleRoutingTransportCostsMatrix.Builder costsMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
            for (Map.Entry<Location, GHPoint> fromEntry : points.entrySet()) {
                for (Map.Entry<Location, GHPoint> toEntry : points.entrySet()) {
                    System.out.println(fromEntry.getKey());
                    System.out.println(toEntry.getKey());
                    System.out.println(fromEntry.getKey().equals(toEntry.getKey()));
                    if (fromEntry.getKey().equals(toEntry.getKey())) {
                        costsMatrixBuilder.addTransportDistance(fromEntry.getKey().getId(), toEntry.getKey().getId(), 0);
                        costsMatrixBuilder.addTransportDistance(fromEntry.getKey().getId(), toEntry.getKey().getId(), 0);
                        continue;
                    }
                    final GHRequest hopperRequest = new GHRequest(fromEntry.getValue(), toEntry.getValue());
                    final GHResponse hopperResponse = RoutingEngine.getHopper().route(hopperRequest);
                    if (hopperResponse.hasErrors()) {
                        System.out.println(hopperResponse.getErrors());
                        response.status(500);
                        return "{\"code\": \"UE\"}";
                    }
                    final PathWrapper path = hopperResponse.getBest();
                    System.out.println(path.getTime());
                    costsMatrixBuilder.addTransportDistance(fromEntry.getKey().getId(), toEntry.getKey().getId(), path.getDistance());
                    costsMatrixBuilder.addTransportTime(fromEntry.getKey().getId(), toEntry.getKey().getId(), path.getTime());
                }
            }
            final VehicleRoutingTransportCostsMatrix costsMatrix = costsMatrixBuilder.build();
            final VehicleRoutingProblem vehicleRoutingProblem = VehicleRoutingProblem.Builder.newInstance()
                .setRoutingCost(costsMatrix)
                .addAllJobs(jobs)
                .addVehicle(vehicle1)
                .build();

            VehicleRoutingAlgorithm algorithm = new SchrimpfFactory().createAlgorithm(vehicleRoutingProblem);
            Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
            VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
            System.out.println(bestSolution.getCost());
            System.out.println(bestSolution.getUnassignedJobs());
            for (VehicleRoute route : bestSolution.getRoutes()) {
//                route.
                for (TourActivity actitvity : route.getActivities()) {
                    System.out.println(actitvity.getArrTime());
                    System.out.println(actitvity.getEndTime());
                    System.out.println(actitvity.getLocation());
                    System.out.println(actitvity.getName());
                }
                for (Job job : route.getTourActivities().getJobs()) {
                    System.out.println(job.getId());
                }
                System.out.println(route.getTourActivities());
                System.out.println(route.getVehicle().getId());
                System.out.println(route.getDriver().getId());
            }
            SolutionPrinter.print(vehicleRoutingProblem, bestSolution, SolutionPrinter.Print.VERBOSE);
            return "";
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
