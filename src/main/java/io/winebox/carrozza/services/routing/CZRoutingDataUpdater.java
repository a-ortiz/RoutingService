package io.winebox.carrozza.services.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIteratorState;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gnu.trove.set.hash.TIntHashSet;
import io.winebox.carrozza.models.CZCoordinate;
import org.json.JSONArray;
import org.json.JSONObject;
import scala.util.parsing.json.JSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

public class CZRoutingDataUpdater {

    private final GraphHopper hopper;
    private final Lock writeLock;
    private final long seconds = 150;
    private CZRoutingData currentRoads;

    public CZRoutingDataUpdater( GraphHopper hopper, Lock writeLock ) {
        this.hopper = hopper;
        this.writeLock = writeLock;
    }

    public void feed( CZRoutingData data ) {
        writeLock.lock();
        try {
            lockedFeed(data);
        } finally {
            writeLock.unlock();
        }
    }

    private void lockedFeed( CZRoutingData data ) {
        currentRoads = data;
        Graph graph = hopper.getGraphHopperStorage().getBaseGraph();
        FlagEncoder carEncoder = hopper.getEncodingManager().getEncoder("car");
        LocationIndex locationIndex = hopper.getLocationIndex();

        int errors = 0;
        int updates = 0;
        TIntHashSet edgeIds = new TIntHashSet(data.size());
        System.out.println(data.size());
        for (CZRoutingEntry entry : data) {
            System.out.println(entry);

            // TODO get more than one point -> our map matching component
            CZCoordinate point = entry.getPoints().get(entry.getPoints().size() / 2);
            QueryResult qr = locationIndex.findClosest(point.getLatitude(), point.getLongitude(), EdgeFilter.ALL_EDGES);
            if (!qr.isValid()) {
                // logger.info("no matching road found for entry " + entry.getId() + " at " + point);
                errors++;
                continue;
            }

            int edgeId = qr.getClosestEdge().getEdge();
            if (edgeIds.contains(edgeId)) {
                // TODO this wouldn't happen with our map matching component
                errors++;
                continue;
            }

            edgeIds.add(edgeId);
            EdgeIteratorState edge = graph.getEdgeIteratorState(edgeId, Integer.MIN_VALUE);
            double value = entry.getValue();

            if ("block".equalsIgnoreCase(entry.getValueType())) {
                edge.setFlags(carEncoder.setAccess(edge.getFlags(), false, false));
                continue;
            }
            if ("replace".equalsIgnoreCase(entry.getMode())) {
                if ("speed".equalsIgnoreCase(entry.getValueType())) {
                    double oldSpeed = carEncoder.getSpeed(edge.getFlags());
                    if (oldSpeed != value) {
                        updates++;
                        // TODO use different speed for the different directions (see e.g. Bike2WeightFlagEncoder)
                        System.out.println("Speed change at " + entry.getId() + " (" + point + "). Old: " + oldSpeed + ", new:" + value);
                        edge.setFlags(carEncoder.setSpeed(edge.getFlags(), value));
                    }
                } else {
                    throw new IllegalStateException("currently no other value type than 'speed' is supported");
                }
            } else {
                throw new IllegalStateException("currently no other mode than 'replace' is supported");
            }
        }

        System.out.println("Updated " + updates + " street elements of " + data.size() + ". Unchanged:" + (data.size() - updates) + ", errors:" + errors);
    }

    protected String fetchJSONString(String url) throws UnirestException {
        String string = Unirest.post(url)
                .header("accept", "application/json").asString().getBody();
        return string;
    }

    public CZRoutingData fetch(String url) throws UnirestException {
        JSONArray arr = new JSONArray(fetchJSONString(url));
        CZRoutingData data = new CZRoutingData();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            double speed = obj.getDouble("speed");
            String idStr = obj.getString("road_entry_id");

            JSONArray paths = obj.getJSONArray("points");
            List<CZCoordinate> points = new ArrayList<CZCoordinate>();
            for (int pointIndex = 0; pointIndex < paths.length(); pointIndex++) {
                JSONObject point = paths.getJSONObject(pointIndex);
                points.add(new CZCoordinate(point.getDouble("latitude"), point.getDouble("longitude")));
            }

            if (!points.isEmpty()) {
                data.add(new CZRoutingEntry(idStr + "_", speed, "speed", "replace", points));
            }
        }
//        List<CZCoordinate> points = new ArrayList();
//        points.add(new CZCoordinate(40.752603, -73.985755));
//        CZRoutingEntry blockEntry = new CZRoutingEntry("block", 0, "block", "",points);
//        data.add(blockEntry);

        return data;
    }

    private final AtomicBoolean running = new AtomicBoolean(false);

    public void start() {
        if (running.get()) {
            return;
        }

        running.set(true);
        new Thread("DataUpdater" + seconds) {
            @Override
            public void run() {
                System.out.println("fetch new data every " + seconds + " seconds");
                while (running.get()) {
                    try {
                        System.out.println("fetch new data");
                        CZRoutingData data = fetch("http://localhost:3000/traffic");
                        feed(data);
                        try {
                            Thread.sleep(seconds * 1000);
                        } catch (InterruptedException ex) {
                            System.out.println("update thread stopped");
                            break;
                        }
                    } catch (Exception ex) {
                        System.out.println(ex);
                        System.out.println("Problem while fetching data");
                    }
                }
            }
        }.start();
    }

    public void stop() {
        running.set(false);
    }

    public CZRoutingData getAll() {
        if (currentRoads == null) {
            return new CZRoutingData();
        }

        return currentRoads;
    }
}
