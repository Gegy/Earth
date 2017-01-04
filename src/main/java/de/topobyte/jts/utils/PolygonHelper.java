// Copyright 2015 Sebastian Kuerten
//
// This file is part of jts-utils.
//
// jts-utils is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// jts-utils is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with jts-utils. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.jts.utils;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import de.topobyte.adt.graph.Graph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Various utility methods concerning polygons.
 *
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class PolygonHelper {

    final static Logger logger = LogManager.getLogger(PolygonHelper.class);

    /**
     * Convenience method for creating a simple polygon from a shell without
     * holes.
     *
     * @param shell the outline of the polygon.
     * @param factory the factory to use.
     * @return the constructed polygon.
     */
    public static Polygon polygonFromLinearRing(LinearRing shell,
                                                GeometryFactory factory) {
        return new Polygon(shell, null, factory);
    }

    /**
     * Given a set of LinearRings, create Multipolygon. Therefore figure out
     * which rings are nested in other rings to build up the topology of the
     * rings and correctly construct the multipolygon consisting of possibly
     * several polygons with holes.
     *
     * @param rings the set of linear rings.
     * @return the newly created JTS-instance.
     */
    public static MultiPolygon multipolygonFromRings(Set<LinearRing> rings,
                                                     boolean checkValid) {
        if (checkValid) {
            Iterator<LinearRing> iter = rings.iterator();
            while (iter.hasNext()) {
                LinearRing ring = iter.next();
                if (!ring.isValid()) {
                    iter.remove();
                    logger.debug("remove......");
                }
            }
        }

        // Build a list of polygons with holes.
        GeometryFactory factory = new GeometryFactory();
        List<Polygon> polygons = new ArrayList<>();

        // Setup a graph with rings as nodes and containment represented via
        // edges
        Graph<LinearRing> graph = new Graph<>();
        graph.addNodes(rings);

        // Build a polygon from each ring to determine containment relation
        Map<LinearRing, Polygon> ringToPolygon = new HashMap<>();
        for (LinearRing r : rings) {
            Polygon p = PolygonHelper.polygonFromLinearRing(r, factory);
            ringToPolygon.put(r, p);
        }

        // Evaluate containment for each pair of rings
        for (LinearRing r : rings) {
            Set<LinearRing> candidates = new HashSet<>();
            candidates.addAll(rings);
            candidates.remove(r);
            Polygon p1 = ringToPolygon.get(r);
            for (LinearRing c : candidates) {
                Polygon p2 = ringToPolygon.get(c);
                if (p1.contains(p2)) {
                    graph.addEdge(r, c);
                }
            }
        }

        // Assemble polygons with holes based on degree of the nodes
        for (LinearRing r : rings) {
            int d = graph.degreeIn(r);
            if (d % 2 == 0) {
                Set<LinearRing> possiblyInner = graph.getEdgesOut(r);
                Set<LinearRing> inner = new HashSet<>();
                for (LinearRing q : possiblyInner) {
                    if (graph.degreeIn(q) == d + 1) {
                        inner.add(q);
                    }
                }
                LinearRing[] holes = inner.toArray(new LinearRing[0]);
                polygons.add(new Polygon(r, holes, factory));
            }
        }

        Polygon[] ps = polygons.toArray(new Polygon[0]);
        return new MultiPolygon(ps, factory);
    }
}
