package io.winebox.carrozza.services.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;

public final class RoutingEngine {

    private static GraphHopper hopper;

    static {
        hopper = new GraphHopper().forServer();
        hopper.setOSMFile("input/planet.osm.pbf");
        hopper.setGraphHopperLocation("output/graphhopper");
        hopper.setEncodingManager(new EncodingManager(EncodingManager.CAR));
        hopper.importOrLoad();
    }

    public static GraphHopper getHopper() {
        return RoutingEngine.hopper;
    }
}
