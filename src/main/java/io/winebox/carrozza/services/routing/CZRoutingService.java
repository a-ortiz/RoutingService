package io.winebox.carrozza.services.routing;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.CmdArgs;
import io.winebox.carrozza.models.CZCoordinate;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class CZRoutingService {

    private static CZRoutingService instance;

    @Getter
    private final GraphHopper hopper;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private CZRoutingDataUpdater dataUpdater;

    private CZRoutingService() {
        CmdArgs args;
        try {
            args = CmdArgs.readFromConfig("config.properties", "graphhopper.config");
        } catch (Exception e) {
            args = new CmdArgs();
        }
        args.put("osmreader.osm", "input/NewYork.osm");
        args.put("graph.location", "output/graph.location");

        this.hopper = new GraphHopper() {

            @Override
            public GHResponse route(GHRequest request) {
                lock.readLock().lock();
                try {
                    return super.route(request);
                } finally {
                    lock.readLock().unlock();
                }
            }

        }
                .forServer()
                .init(args)
//            .setOSMFile("input/planet.osm.pbf")
//            .setGraphHopperLocation("output/graph.location")
//            .setEncodingManager(new EncodingManager("car"))


            ;
        this.hopper.getCHFactoryDecorator().setWeightingsAsStrings("fastest");
        this.dataUpdater = new CZRoutingDataUpdater(this.hopper, lock.writeLock());
    }

    public void run() {
        this.hopper.importOrLoad();
        this.dataUpdater.start();
    }

    public static CZRoutingService getInstance() {
        if (instance == null) {
            synchronized (CZRoutingService.class) {
                if (instance == null) {
                    instance = new CZRoutingService();
                }
            }
        }
        return instance;
    }
}
