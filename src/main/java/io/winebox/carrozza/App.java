package io.winebox.carrozza;

import io.winebox.carrozza.routes.RouteController;
import io.winebox.carrozza.routes.TourController;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static spark.Spark.post;

public final class App {

    public final static void main( String[] args ) {
        System.out.println("Hello, world!");
//        Runnable task = () -> {
//            String threadName = Thread.currentThread().getName();
//            System.out.println("Hello " + threadName);
//        };
//
//        task.run();
//
//        Thread thread = new Thread(task);
//        thread.start();

//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        executor.submit(() -> {
//            String threadName = Thread.currentThread().getName();
//            System.out.println("Hello " + threadName);
//        });
//        try {
//            System.out.println("attempt to shutdown executor");
//            executor.shutdown();
//            executor.awaitTermination(5, TimeUnit.SECONDS);
//        }
//        catch (InterruptedException e) {
//            System.err.println("tasks interrupted");
//        }
//        finally {
//            if (!executor.isTerminated()) {
//                System.err.println("cancel non-finished tasks");
//            }
//            executor.shutdownNow();
//            System.out.println("shutdown finished");
//        }
//        System.out.println("Done!");

//        ExecutorService executor = Executors.newWorkStealingPool();
//
//        List<Callable<String>> callables = Arrays.asList(
//            () -> {
//                TimeUnit.SECONDS.sleep(5);
//                return "task1";
//            },
//            () -> {
//                TimeUnit.SECONDS.sleep(5);
//                return "task2";
//            },
//            () -> {
//                TimeUnit.SECONDS.sleep(7);
//                return "task5";
//            },
//            () -> {
//                TimeUnit.SECONDS.sleep(7);
//                return "task6";
//            },
//            () -> {
//                TimeUnit.SECONDS.sleep(5);
//                return "task3";
//            },
//            () -> {
//                TimeUnit.SECONDS.sleep(5);
//                return "task4";
//            }
//        );
//
//        try {
//            System.out.println("hello");
//            List<Future<String>> futures = executor.invokeAll(callables);
//
//            System.out.println("done?");
//            futures
//                .stream()
//                .map(future -> {
//                    try {
//                        return future.get();
//                    }
//                    catch (Exception e) {
//                        throw new IllegalStateException(e);
//                    }
//                })
//                .forEach(System.out::println);
//            System.out.println("bye");
//        } catch (Exception e) {
//            System.out.println(e);
//        }

        post("/tour", TourController::createTour);
        post("/route", RouteController::createRoute);
    }
}