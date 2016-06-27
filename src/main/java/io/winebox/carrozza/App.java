package io.winebox.carrozza;

import io.winebox.carrozza.routes.RouteController;
import io.winebox.carrozza.routes.TourController;

import static spark.Spark.post;

public final class App {

    public final static void main( String[] args ) {
        System.out.println("Hello, world!");
        post("/tour", TourController::createTour);
        post("/route", RouteController::createRoute);
    }
}