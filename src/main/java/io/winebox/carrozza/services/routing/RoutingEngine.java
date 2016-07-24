package io.winebox.carrozza.services.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;

public final class RoutingEngine {

    private static GraphHopper hopper;

    static {
        hopper = new GraphHopper().forServer();
        hopper.setOSMFile("input/planet.osm.pbf");
        hopper.setGraphHopperLocation("output/graphhopper");
        hopper.setEncodingManager(new EncodingManager("car"));
        hopper.importOrLoad();
    }

    public static GraphHopper getHopper() {
        return RoutingEngine.hopper;
    }
}


//public static ThreadSafeSingleton getInstanceUsingDoubleLocking(){
//    if(instance == null){
//        synchronized (ThreadSafeSingleton.class) {
//            if(instance == null){
//                instance = new ThreadSafeSingleton();
//            }
//        }
//    }
//    return instance;
//}