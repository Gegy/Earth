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

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.algorithm.MCPointInRing;
import com.vividsolutions.jts.algorithm.PointInRing;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.EdgeIntersection;
import com.vividsolutions.jts.geomgraph.EdgeIntersectionList;
import com.vividsolutions.jts.geomgraph.GeometryGraph;
import com.vividsolutions.jts.util.Assert;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implements the algorithms required to compute the <code>isValid()</code> method
 * for {@link Geometry}s.
 * See the documentation for the various geometry types for a specification of validity.
 *
 * @version 1.7
 */
public class IsValidOp {
    /**
     * Tests whether a {@link Geometry} is valid.
     *
     * @param geom the Geometry to test
     * @return true if the geometry is valid
     */
    public static boolean isValid(Geometry geom) {
        IsValidOp isValidOp = new IsValidOp(geom);
        return isValidOp.isValid();
    }

    /**
     * Checks whether a coordinate is valid for processing.
     * Coordinates are valid iff their x and y ordinates are in the
     * range of the floating point representation.
     *
     * @param coord the coordinate to validate
     * @return <code>true</code> if the coordinate is valid
     */
    public static boolean isValid(Coordinate coord) {
        if (Double.isNaN(coord.x)) {
            return false;
        }
        if (Double.isInfinite(coord.x)) {
            return false;
        }
        if (Double.isNaN(coord.y)) {
            return false;
        }
        return !Double.isInfinite(coord.y);
    }

    /**
     * Find a point from the list of testCoords
     * that is NOT a node in the edge for the list of searchCoords
     *
     * @return the point found, or <code>null</code> if none found
     */
    public static Coordinate findPtNotNode(
            Coordinate[] testCoords,
            LinearRing searchRing,
            GeometryGraph graph) {
        // find edge corresponding to searchRing.
        Edge searchEdge = graph.findEdge(searchRing);
        // find a point in the testCoords which is not a node of the searchRing
        EdgeIntersectionList eiList = searchEdge.getEdgeIntersectionList();
        // somewhat inefficient - is there a better way? (Use a node map, for instance?)
        for (Coordinate pt : testCoords) {
            if (!eiList.isIntersection(pt)) {
                return pt;
            }
        }
        return null;
    }

    private Geometry parentGeometry;  // the base Geometry to be validated
    /**
     * If the following condition is TRUE JTS will validate inverted shells and exverted holes
     * (the ESRI SDE model)
     */
    private boolean isSelfTouchingRingFormingHoleValid = false;
    private TopologyValidationError validErr;

    public IsValidOp(Geometry parentGeometry) {
        this.parentGeometry = parentGeometry;
    }

    /**
     * Sets whether polygons using <b>Self-Touching Rings</b> to form
     * holes are reported as valid.
     * If this flag is set, the following Self-Touching conditions
     * are treated as being valid:
     * <ul>
     * <li>the shell ring self-touches to create a hole touching the shell
     * <li>a hole ring self-touches to create two holes touching at a point
     * </ul>
     * <p>
     * The default (following the OGC SFS standard)
     * is that this condition is <b>not</b> valid (<code>false</code>).
     * <p>
     * This does not affect whether Self-Touching Rings
     * disconnecting the polygon interior are considered valid
     * (these are considered to be <b>invalid</b> under the SFS, and many other
     * spatial models as well).
     * This includes "bow-tie" shells,
     * which self-touch at a single point causing the interior to
     * be disconnected,
     * and "C-shaped" holes which self-touch at a single point causing an island to be formed.
     *
     * @param isValid states whether geometry with this condition is valid
     */
    public void setSelfTouchingRingFormingHoleValid(boolean isValid) {
        this.isSelfTouchingRingFormingHoleValid = isValid;
    }

    /**
     * Computes the validity of the geometry,
     * and returns <tt>true</tt> if it is valid.
     *
     * @return true if the geometry is valid
     */
    public boolean isValid() {
        this.checkValid(this.parentGeometry);
        return this.validErr == null;
    }

    /**
     * Computes the validity of the geometry,
     * and if not valid returns the validation error for the geometry,
     * or null if the geometry is valid.
     *
     * @return the validation error, if the geometry is invalid
     * or null if the geometry is valid
     */
    public TopologyValidationError getValidationError() {
        this.checkValid(this.parentGeometry);
        return this.validErr;
    }

    private void checkValid(Geometry g) {
        this.validErr = null;

        // empty geometries are always valid!
        if (g.isEmpty()) {
            return;
        }

        if (g instanceof Point) {
            this.checkValid((Point) g);
        } else if (g instanceof MultiPoint) {
            this.checkValid((MultiPoint) g);
        }
        // LineString also handles LinearRings
        else if (g instanceof LinearRing) {
            this.checkValid((LinearRing) g);
        } else if (g instanceof LineString) {
            this.checkValid((LineString) g);
        } else if (g instanceof Polygon) {
            this.checkValid((Polygon) g);
        } else if (g instanceof MultiPolygon) {
            this.checkValid((MultiPolygon) g);
        } else if (g instanceof GeometryCollection) {
            this.checkValid((GeometryCollection) g);
        } else {
            throw new UnsupportedOperationException(g.getClass().getName());
        }
    }

    /**
     * Checks validity of a Point.
     */
    private void checkValid(Point g) {
        this.checkInvalidCoordinates(g.getCoordinates());
    }

    /**
     * Checks validity of a MultiPoint.
     */
    private void checkValid(MultiPoint g) {
        this.checkInvalidCoordinates(g.getCoordinates());
    }

    /**
     * Checks validity of a LineString.  Almost anything goes for linestrings!
     */
    private void checkValid(LineString g) {
        this.checkInvalidCoordinates(g.getCoordinates());
        if (this.validErr != null) {
            return;
        }
        GeometryGraph graph = new GeometryGraph(0, g);
        this.checkTooFewPoints(graph);
    }

    /**
     * Checks validity of a LinearRing.
     */
    private void checkValid(LinearRing g) {
        this.checkInvalidCoordinates(g.getCoordinates());
        if (this.validErr != null) {
            return;
        }
        this.checkClosedRing(g);
        if (this.validErr != null) {
            return;
        }

        GeometryGraph graph = new GeometryGraph(0, g);
        this.checkTooFewPoints(graph);
        if (this.validErr != null) {
            return;
        }
        LineIntersector li = new RobustLineIntersector();
        graph.computeSelfNodes(li, true);
        this.checkNoSelfIntersectingRings(graph);
    }

    /**
     * Checks the validity of a polygon.
     * Sets the validErr flag.
     */
    private void checkValid(Polygon g) {
        this.checkInvalidCoordinates(g);
        if (this.validErr != null) {
            return;
        }
        this.checkClosedRings(g);
        if (this.validErr != null) {
            return;
        }

        GeometryGraph graph = new GeometryGraph(0, g);

        this.checkTooFewPoints(graph);
        if (this.validErr != null) {
            return;
        }
        this.checkConsistentArea(graph);
        if (this.validErr != null) {
            return;
        }

        if (!this.isSelfTouchingRingFormingHoleValid) {
            this.checkNoSelfIntersectingRings(graph);
            if (this.validErr != null) {
                return;
            }
        }
        this.checkHolesInShell(g, graph);
        if (this.validErr != null) {
            return;
        }
        //SLOWcheckHolesNotNested(g);
        this.checkHolesNotNested(g, graph);
        if (this.validErr != null) {
            return;
        }
        this.checkConnectedInteriors(graph);
    }

    private void checkValid(MultiPolygon g) {
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Polygon p = (Polygon) g.getGeometryN(i);
            this.checkInvalidCoordinates(p);
            if (this.validErr != null) {
                return;
            }
            this.checkClosedRings(p);
            if (this.validErr != null) {
                return;
            }
        }

        GeometryGraph graph = new GeometryGraph(0, g);

        this.checkTooFewPoints(graph);
        if (this.validErr != null) {
            return;
        }
        this.checkConsistentArea(graph);
        if (this.validErr != null) {
            return;
        }
        if (!this.isSelfTouchingRingFormingHoleValid) {
            this.checkNoSelfIntersectingRings(graph);
            if (this.validErr != null) {
                return;
            }
        }
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Polygon p = (Polygon) g.getGeometryN(i);
            this.checkHolesInShell(p, graph);
            if (this.validErr != null) {
                return;
            }
        }
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Polygon p = (Polygon) g.getGeometryN(i);
            this.checkHolesNotNested(p, graph);
            if (this.validErr != null) {
                return;
            }
        }
        this.checkShellsNotNested(g, graph);
        if (this.validErr != null) {
            return;
        }
        this.checkConnectedInteriors(graph);
    }

    private void checkValid(GeometryCollection gc) {
        for (int i = 0; i < gc.getNumGeometries(); i++) {
            Geometry g = gc.getGeometryN(i);
            this.checkValid(g);
            if (this.validErr != null) {
                return;
            }
        }
    }

    private void checkInvalidCoordinates(Coordinate[] coords) {
        for (Coordinate coord : coords) {
            if (!isValid(coord)) {
                validErr = new TopologyValidationError(
                        TopologyValidationError.INVALID_COORDINATE,
                        coord);
                return;
            }
        }
    }

    private void checkInvalidCoordinates(Polygon poly) {
        this.checkInvalidCoordinates(poly.getExteriorRing().getCoordinates());
        if (this.validErr != null) {
            return;
        }
        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            this.checkInvalidCoordinates(poly.getInteriorRingN(i).getCoordinates());
            if (this.validErr != null) {
                return;
            }
        }
    }

    private void checkClosedRings(Polygon poly) {
        this.checkClosedRing((LinearRing) poly.getExteriorRing());
        if (this.validErr != null) {
            return;
        }
        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            this.checkClosedRing((LinearRing) poly.getInteriorRingN(i));
            if (this.validErr != null) {
                return;
            }
        }
    }

    private void checkClosedRing(LinearRing ring) {
        if (!ring.isClosed()) {
            Coordinate pt = null;
            if (ring.getNumPoints() >= 1) {
                pt = ring.getCoordinateN(0);
            }
            this.validErr = new TopologyValidationError(
                    TopologyValidationError.RING_NOT_CLOSED,
                    pt);
        }
    }

    private void checkTooFewPoints(GeometryGraph graph) {
        if (graph.hasTooFewPoints()) {
            this.validErr = new TopologyValidationError(
                    TopologyValidationError.TOO_FEW_POINTS,
                    graph.getInvalidPoint());
        }
    }

    /**
     * Checks that the arrangement of edges in a polygonal geometry graph
     * forms a consistent area.
     *
     * @param graph
     * @see ConsistentAreaTester
     */
    private void checkConsistentArea(GeometryGraph graph) {
        ConsistentAreaTester cat = new ConsistentAreaTester(graph);
        boolean isValidArea = cat.isNodeConsistentArea();
        if (!isValidArea) {
            this.validErr = new TopologyValidationError(
                    TopologyValidationError.SELF_INTERSECTION,
                    cat.getInvalidPoint());
            return;
        }
        if (cat.hasDuplicateRings()) {
            this.validErr = new TopologyValidationError(
                    TopologyValidationError.DUPLICATE_RINGS,
                    cat.getInvalidPoint());
        }
    }

    /**
     * Check that there is no ring which self-intersects (except of course at its endpoints).
     * This is required by OGC topology rules (but not by other models
     * such as ESRI SDE, which allow inverted shells and exverted holes).
     *
     * @param graph the topology graph of the geometry
     */
    private void checkNoSelfIntersectingRings(GeometryGraph graph) {
        for (Iterator i = graph.getEdgeIterator(); i.hasNext(); ) {
            Edge e = (Edge) i.next();
            this.checkNoSelfIntersectingRing(e.getEdgeIntersectionList());
            if (this.validErr != null) {
                return;
            }
        }
    }

    /**
     * Check that a ring does not self-intersect, except at its endpoints.
     * Algorithm is to count the number of times each node along edge occurs.
     * If any occur more than once, that must be a self-intersection.
     */
    private void checkNoSelfIntersectingRing(EdgeIntersectionList eiList) {
        Set nodeSet = new TreeSet();
        boolean isFirst = true;
        for (Iterator i = eiList.iterator(); i.hasNext(); ) {
            EdgeIntersection ei = (EdgeIntersection) i.next();
            if (isFirst) {
                isFirst = false;
                continue;
            }
            if (nodeSet.contains(ei.coord)) {
                this.validErr = new TopologyValidationError(
                        TopologyValidationError.RING_SELF_INTERSECTION,
                        ei.coord);
                return;
            } else {
                nodeSet.add(ei.coord);
            }
        }
    }

    /**
     * Tests that each hole is inside the polygon shell.
     * This routine assumes that the holes have previously been tested
     * to ensure that all vertices lie on the shell oon the same side of it
     * (i.e that the hole rings do not cross the shell ring).
     * In other words, this test is only correct if the ConsistentArea test is passed first.
     * Given this, a simple point-in-polygon test of a single point in the hole can be used,
     * provided the point is chosen such that it does not lie on the shell.
     *
     * @param p the polygon to be tested for hole inclusion
     * @param graph a GeometryGraph incorporating the polygon
     */
    private void checkHolesInShell(Polygon p, GeometryGraph graph) {
        LinearRing shell = (LinearRing) p.getExteriorRing();

        //PointInRing pir = new SimplePointInRing(shell);
        //PointInRing pir = new SIRtreePointInRing(shell);
        PointInRing pir = new MCPointInRing(shell);

        for (int i = 0; i < p.getNumInteriorRing(); i++) {

            LinearRing hole = (LinearRing) p.getInteriorRingN(i);
            Coordinate holePt = findPtNotNode(hole.getCoordinates(), shell, graph);
            /**
             * If no non-node hole vertex can be found, the hole must
             * split the polygon into disconnected interiors.
             * This will be caught by a subsequent check.
             */
            if (holePt == null) {
                return;
            }

            boolean outside = !pir.isInside(holePt);
            if (outside) {
                this.validErr = new TopologyValidationError(
                        TopologyValidationError.HOLE_OUTSIDE_SHELL,
                        holePt);
                return;
            }
        }
    }

    /**
     * Tests that no hole is nested inside another hole.
     * This routine assumes that the holes are disjoint.
     * To ensure this, holes have previously been tested
     * to ensure that:
     * <ul>
     * <li>they do not partially overlap
     * (checked by <code>checkRelateConsistency</code>)
     * <li>they are not identical
     * (checked by <code>checkRelateConsistency</code>)
     * </ul>
     */
    private void checkHolesNotNested(Polygon p, GeometryGraph graph) {
        IndexedNestedRingTester nestedTester = new IndexedNestedRingTester(graph);
        //SimpleNestedRingTester nestedTester = new SimpleNestedRingTester(arg[0]);
        //SweeplineNestedRingTester nestedTester = new SweeplineNestedRingTester(arg[0]);

        for (int i = 0; i < p.getNumInteriorRing(); i++) {
            LinearRing innerHole = (LinearRing) p.getInteriorRingN(i);
            nestedTester.add(innerHole);
        }
        boolean isNonNested = nestedTester.isNonNested();
        if (!isNonNested) {
            this.validErr = new TopologyValidationError(
                    TopologyValidationError.NESTED_HOLES,
                    nestedTester.getNestedPoint());
        }
    }

    /**
     * Tests that no element polygon is wholly in the interior of another element polygon.
     * <p>
     * Preconditions:
     * <ul>
     * <li>shells do not partially overlap
     * <li>shells do not touch along an edge
     * <li>no duplicate rings exist
     * </ul>
     * This routine relies on the fact that while polygon shells may touch at one or
     * more vertices, they cannot touch at ALL vertices.
     */
    private void checkShellsNotNested(MultiPolygon mp, GeometryGraph graph) {
        for (int i = 0; i < mp.getNumGeometries(); i++) {
            Polygon p = (Polygon) mp.getGeometryN(i);
            LinearRing shell = (LinearRing) p.getExteriorRing();
            for (int j = 0; j < mp.getNumGeometries(); j++) {
                if (i == j) {
                    continue;
                }
                Polygon p2 = (Polygon) mp.getGeometryN(j);
                this.checkShellNotNested(shell, p2, graph);
                if (this.validErr != null) {
                    return;
                }
            }
        }
    }

    /**
     * Check if a shell is incorrectly nested within a polygon.  This is the case
     * if the shell is inside the polygon shell, but not inside a polygon hole.
     * (If the shell is inside a polygon hole, the nesting is valid.)
     * <p>
     * The algorithm used relies on the fact that the rings must be properly contained.
     * E.g. they cannot partially overlap (this has been previously checked by
     * <code>checkRelateConsistency</code> )
     */
    private void checkShellNotNested(LinearRing shell, Polygon p, GeometryGraph graph) {
        Coordinate[] shellPts = shell.getCoordinates();
        // test if shell is inside polygon shell
        LinearRing polyShell = (LinearRing) p.getExteriorRing();
        Coordinate[] polyPts = polyShell.getCoordinates();
        Coordinate shellPt = findPtNotNode(shellPts, polyShell, graph);
        // if no point could be found, we can assume that the shell is outside the polygon
        if (shellPt == null) {
            return;
        }
        boolean insidePolyShell = CGAlgorithms.isPointInRing(shellPt, polyPts);
        if (!insidePolyShell) {
            return;
        }

        // if no holes, this is an error!
        if (p.getNumInteriorRing() <= 0) {
            this.validErr = new TopologyValidationError(
                    TopologyValidationError.NESTED_SHELLS,
                    shellPt);
            return;
        }

        /**
         * Check if the shell is inside one of the holes.
         * This is the case if one of the calls to checkShellInsideHole
         * returns a null coordinate.
         * Otherwise, the shell is not properly contained in a hole, which is an error.
         */
        Coordinate badNestedPt = null;
        for (int i = 0; i < p.getNumInteriorRing(); i++) {
            LinearRing hole = (LinearRing) p.getInteriorRingN(i);
            badNestedPt = this.checkShellInsideHole(shell, hole, graph);
            if (badNestedPt == null) {
                return;
            }
        }
        this.validErr = new TopologyValidationError(
                TopologyValidationError.NESTED_SHELLS,
                badNestedPt);
    }

    /**
     * This routine checks to see if a shell is properly contained in a hole.
     * It assumes that the edges of the shell and hole do not
     * properly intersect.
     *
     * @return <code>null</code> if the shell is properly contained, or
     * a Coordinate which is not inside the hole if it is not
     */
    private Coordinate checkShellInsideHole(LinearRing shell, LinearRing hole, GeometryGraph graph) {
        Coordinate[] shellPts = shell.getCoordinates();
        Coordinate[] holePts = hole.getCoordinates();
        // TODO: improve performance of this - by sorting pointlists for instance?
        Coordinate shellPt = findPtNotNode(shellPts, hole, graph);
        // if point is on shell but not hole, check that the shell is inside the hole
        if (shellPt != null) {
            boolean insideHole = CGAlgorithms.isPointInRing(shellPt, holePts);
            if (!insideHole) {
                return shellPt;
            }
        }
        Coordinate holePt = findPtNotNode(holePts, shell, graph);
        // if point is on hole but not shell, check that the hole is outside the shell
        if (holePt != null) {
            boolean insideShell = CGAlgorithms.isPointInRing(holePt, shellPts);
            if (insideShell) {
                return holePt;
            }
            return null;
        }
        Assert.shouldNeverReachHere("points in shell and hole appear to be equal");
        return null;
    }

    private void checkConnectedInteriors(GeometryGraph graph) {
        ConnectedInteriorTester cit = new ConnectedInteriorTester(graph);
        if (!cit.isInteriorsConnected()) {
            this.validErr = new TopologyValidationError(
                    TopologyValidationError.DISCONNECTED_INTERIOR,
                    cit.getCoordinate());
        }
    }
}
