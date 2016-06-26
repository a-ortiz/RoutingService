package io.winebox.carrozza;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winebox.carrozza.models.CZJob;

import static spark.Spark.post;

public final class App {

    public final static void main( String[] args ) {
        System.out.println("Hello, world!");
//        post("/tour", TourController::createTour);
//        post("/route", RouteController::createRoute);

        try {
            final ObjectMapper mapper = new ObjectMapper();
            CZJob job = mapper.readValue("{ \"id\": \"a\", \"type\": \"shipment\", \"pickup_coordinate\": { \"latitude\": -10, \"longitude\": 9.2 }, \"delivery_coordinate\": { \"latitude\": -10.0, \"longitude\": 9.2 } }", CZJob.class);
            System.out.println(mapper.writeValueAsString(job));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}