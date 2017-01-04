/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.operation.valid;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geomgraph.DirectedEdge;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.EdgeRing;
import com.vividsolutions.jts.geomgraph.GeometryGraph;
import com.vividsolutions.jts.geomgraph.PlanarGraph;
import com.vividsolutions.jts.geomgraph.Position;
import com.vividsolutions.jts.operation.overlay.MaximalEdgeRing;
import com.vividsolutions.jts.operation.overlay.OverlayNodeFactory;
import com.vividsolutions.jts.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class tests that the interior of an area {@link Geometry}
 * ( {@link Polygon}  or {@link MultiPolygon} )
 * is connected.
 * This can happen if:
 * <ul>
 * <li>a shell self-intersects
 * <li>one or more holes form a connected chain touching a shell at two different points
 * <li>one or more holes form a ring around a subset of the interior
 * </ul>
 * If a disconnected situation is found the location of the problem is recorded.
 *
 * @version 1.7
 */
public class ConnectedInteriorTester {

    public static Coordinate findDifferentPoint(Coordinate[] coord, Coordinate pt) {
        for (Coordinate aCoord : coord) {
            if (!aCoord.equals(pt)) {
                return aCoord;
            }
        }
        return null;
    }

    private GeometryFactory geometryFactory = new GeometryFactory();

    private GeometryGraph geomGraph;
    // save a coordinate for any disconnected interior found
    // the coordinate will be somewhere on the ring surrounding the disconnected interior
    private Coordinate disconnectedRingcoord;

    public ConnectedInteriorTester(GeometryGraph geomGraph) {
        this.geomGraph = geomGraph;
    }

    public Coordinate getCoordinate() {
        return this.disconnectedRingcoord;
    }

    public boolean isInteriorsConnected() {
        // node the edges, in case holes touch the shell
        List splitEdges = new ArrayList();
        this.geomGraph.computeSplitEdges(splitEdges);

        // form the edges into rings
        PlanarGraph graph = new PlanarGraph(new OverlayNodeFactory());
        graph.addEdges(splitEdges);
        this.setInteriorEdgesInResult(graph);
        graph.linkResultDirectedEdges();
        List edgeRings = this.buildEdgeRings(graph.getEdgeEnds());

        /**
         * Mark all the edges for the edgeRings corresponding to the shells
         * of the input polygons.  Note only ONE ring gets marked for each shell.
         */
        this.visitShellInteriors(this.geomGraph.getGeometry(), graph);

        /**
         * If there are any unvisited shell edges
         * (i.e. a ring which is not a hole and which has the interior
         * of the parent area on the RHS)
         * this means that one or more holes must have split the interior of the
         * polygon into at least two pieces.  The polygon is thus invalid.
         */
        return !this.hasUnvisitedShellEdge(edgeRings);
    }

    private void setInteriorEdgesInResult(PlanarGraph graph) {
        for (Object o : graph.getEdgeEnds()) {
            DirectedEdge de = (DirectedEdge) o;
            if (de.getLabel().getLocation(0, Position.RIGHT) == Location.INTERIOR) {
                de.setInResult(true);
            }
        }
    }

    /**
     * Form DirectedEdges in graph into Minimal EdgeRings.
     * (Minimal Edgerings must be used, because only they are guaranteed to provide
     * a correct isHole computation)
     */
    private List buildEdgeRings(Collection dirEdges) {
        List edgeRings = new ArrayList();
        for (Object dirEdge : dirEdges) {
            DirectedEdge de = (DirectedEdge) dirEdge;
            // if this edge has not yet been processed
            if (de.isInResult()
                    && de.getEdgeRing() == null) {
                MaximalEdgeRing er = new MaximalEdgeRing(de, this.geometryFactory);

                er.linkDirectedEdgesForMinimalEdgeRings();
                List minEdgeRings = er.buildMinimalRings();
                edgeRings.addAll(minEdgeRings);
            }
        }
        return edgeRings;
    }

    /**
     * Mark all the edges for the edgeRings corresponding to the shells
     * of the input polygons.
     * Only ONE ring gets marked for each shell - if there are others which remain unmarked
     * this indicates a disconnected interior.
     */
    private void visitShellInteriors(Geometry g, PlanarGraph graph) {
        if (g instanceof Polygon) {
            Polygon p = (Polygon) g;
            this.visitInteriorRing(p.getExteriorRing(), graph);
        }
        if (g instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon) g;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Polygon p = (Polygon) mp.getGeometryN(i);
                this.visitInteriorRing(p.getExteriorRing(), graph);
            }
        }
    }

    private void visitInteriorRing(LineString ring, PlanarGraph graph) {
        Coordinate[] pts = ring.getCoordinates();
        Coordinate pt0 = pts[0];
        /**
         * Find first point in coord list different to initial point.
         * Need special check since the first point may be repeated.
         */
        Coordinate pt1 = findDifferentPoint(pts, pt0);
        Edge e = graph.findEdgeInSameDirection(pt0, pt1);
        DirectedEdge de = (DirectedEdge) graph.findEdgeEnd(e);
        DirectedEdge intDe = null;
        if (de.getLabel().getLocation(0, Position.RIGHT) == Location.INTERIOR) {
            intDe = de;
        } else if (de.getSym().getLabel().getLocation(0, Position.RIGHT) == Location.INTERIOR) {
            intDe = de.getSym();
        }
        Assert.isTrue(intDe != null, "unable to find dirEdge with Interior on RHS");

        this.visitLinkedDirectedEdges(intDe);
    }

    protected void visitLinkedDirectedEdges(DirectedEdge start) {
        DirectedEdge startDe = start;
        DirectedEdge de = start;
        do {
            Assert.isTrue(de != null, "found null Directed Edge");
            de.setVisited(true);
            de = de.getNext();
        } while (de != startDe);
    }

    /**
     * Check if any shell ring has an unvisited edge.
     * A shell ring is a ring which is not a hole and which has the interior
     * of the parent area on the RHS.
     * (Note that there may be non-hole rings with the interior on the LHS,
     * since the interior of holes will also be polygonized into CW rings
     * by the linkAllDirectedEdges() step)
     *
     * @return true if there is an unvisited edge in a non-hole ring
     */
    private boolean hasUnvisitedShellEdge(List edgeRings) {
        for (Object edgeRing : edgeRings) {
            EdgeRing er = (EdgeRing) edgeRing;
            // don't check hole rings
            if (er.isHole()) {
                continue;
            }
            List edges = er.getEdges();
            DirectedEdge de = (DirectedEdge) edges.get(0);
            // don't check CW rings which are holes
            // (MD - this check may now be irrelevant)
            if (de.getLabel().getLocation(0, Position.RIGHT) != Location.INTERIOR) {
                continue;
            }

            /**
             * the edgeRing is CW ring which surrounds the INT of the area, so check all
             * edges have been visited.  If any are unvisited, this is a disconnected part of the interior
             */
            for (Object edge : edges) {
                de = (DirectedEdge) edge;
//Debug.print("visted? "); Debug.println(de);
                if (!de.isVisited()) {
//Debug.print("not visited "); Debug.println(de);
                    this.disconnectedRingcoord = de.getCoordinate();
                    return true;
                }
            }
        }
        return false;
    }
}
