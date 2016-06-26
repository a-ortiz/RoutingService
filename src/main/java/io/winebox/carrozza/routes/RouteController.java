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
