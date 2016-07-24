package io.winebox.carrozza;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.Translation;
import com.graphhopper.util.shapes.GHPoint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AJ on 7/23/16.
 */
public class Test {
    public static void main( String[] args ) throws Exception {
        CmdArgs graphHopperArgs = CmdArgs.readFromConfig("config.properties", "graphhopper.config");
        graphHopperArgs.merge(CmdArgs.read(args));
        graphHopperArgs.put("osmreader.osm", graphHopperArgs.get("datasource", "input/NewYork.osm"));
        graphHopperArgs.put("graph.location", graphHopperArgs.get("graph.location", "output/graph-cache"));

        GraphHopper graphHopper = new GraphHopper()
                .forServer()
                .init(graphHopperArgs)
                .importOrLoad();

        Graph graph = graphHopper.getGraphHopperStorage().getBaseGraph();
        FlagEncoder carEncoder = graphHopper.getEncodingManager().getEncoder("car");
        LocationIndex locationIndex = graphHopper.getLocationIndex();

        long trafficValue = 0;
        GHPoint trafficPoint = new GHPoint(40.752603, -73.985755);
        QueryResult trafficQueryResult = locationIndex.findClosest(trafficPoint.getLat(), trafficPoint.getLon(), EdgeFilter.ALL_EDGES);

        int trafficEdgeId = trafficQueryResult.getClosestEdge().getEdge();
        EdgeIteratorState trafficEdge = graph.getEdgeIteratorState(trafficEdgeId, Integer.MIN_VALUE);
        trafficEdge.setFlags(carEncoder.setSpeed(trafficEdge.getFlags(), trafficValue));

        GHPoint blockPoint = new GHPoint(40.751201, -73.980434);
        QueryResult blockQueryResult = locationIndex.findClosest(blockPoint.getLat(), blockPoint.getLon(), EdgeFilter.ALL_EDGES);

        int blockEdgeId = blockQueryResult.getClosestEdge().getEdge();
        EdgeIteratorState blockEdge = graph.getEdgeIteratorState(blockEdgeId, Integer.MIN_VALUE);
        blockEdge.setFlags(carEncoder.setAccess(blockEdge.getFlags(), false, false));

        GHPoint fromPoint = new GHPoint(40.752279, -73.993505);
        GHPoint toPoint = new GHPoint(40.754092, -73.978377);
        List<GHPoint> points = new ArrayList();
        points.add(fromPoint);
        points.add(toPoint);
        GHRequest request = new GHRequest(points);
        GHResponse response = graphHopper.route(request);
        if (response.hasErrors()) {
            System.out.println(response.getErrors());
            return;
        }
        PathWrapper path = response.getBest();

        final Translation translation = graphHopper.getTranslationMap().getWithFallBack(request.getLocale());
        System.out.println(path.getTime() + " milliseconds");
        System.out.println(new BigDecimal(path.getTime() / 1000 / 60).setScale(0, RoundingMode.HALF_UP).intValue() + " minutes");

        int pointsCount = 0;
        for (Instruction instruction : path.getInstructions()) {
            System.out.println(instruction.getTurnDescription(translation));
            pointsCount += instruction.getPoints().size();
        }
        System.out.println("Number of steps: " + pointsCount);
    }
}
