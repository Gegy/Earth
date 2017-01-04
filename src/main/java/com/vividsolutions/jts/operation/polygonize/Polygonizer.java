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
package com.vividsolutions.jts.operation.polygonize;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Polygonizes a set of {@link Geometry}s which contain linework that
 * represents the edges of a planar graph.
 * All types of Geometry are accepted as input;
 * the constituent linework is extracted as the edges to be polygonized.
 * The processed edges must be correctly noded; that is, they must only meet
 * at their endpoints.  The Polygonizer will run on incorrectly noded input
 * but will not form polygons from non-noded edges,
 * and will report them as errors.
 * <p>
 * The Polygonizer reports the follow kinds of errors:
 * <ul>
 * <li><b>Dangles</b> - edges which have one or both ends which are not incident on another edge endpoint
 * <li><b>Cut Edges</b> - edges which are connected at both ends but which do not form part of polygon
 * <li><b>Invalid Ring Lines</b> - edges which form rings which are invalid
 * (e.g. the component lines contain a self-intersection)
 * </ul>
 *
 * @version 1.7
 */
public class Polygonizer {

    /**
     * Adds every linear element in a {@link Geometry} into the polygonizer graph.
     */
    private class LineStringAdder
            implements GeometryComponentFilter {
        @Override
        public void filter(Geometry g) {
            if (g instanceof LineString) {
                Polygonizer.this.add((LineString) g);
            }
        }
    }

    // default factory
    private LineStringAdder lineStringAdder = new LineStringAdder();

    protected PolygonizeGraph graph;
    // initialize with empty collections, in case nothing is computed
    protected Collection dangles = new ArrayList();
    protected List cutEdges = new ArrayList();
    protected List invalidRingLines = new ArrayList();

    protected List holeList = null;
    protected List shellList = null;
    protected List polyList = null;

    /**
     * Create a polygonizer with the same {@link GeometryFactory}
     * as the input {@link Geometry}s
     */
    public Polygonizer() {
    }

    /**
     * Adds a collection of geometries to the edges to be polygonized.
     * May be called multiple times.
     * Any dimension of Geometry may be added;
     * the constituent linework will be extracted and used.
     *
     * @param geomList a list of {@link Geometry}s with linework to be polygonized
     */
    public void add(Collection geomList) {
        for (Object aGeomList : geomList) {
            Geometry geometry = (Geometry) aGeomList;
            this.add(geometry);
        }
    }

    /**
     * Add a {@link Geometry} to the edges to be polygonized.
     * May be called multiple times.
     * Any dimension of Geometry may be added;
     * the constituent linework will be extracted and used
     *
     * @param g a {@link Geometry} with linework to be polygonized
     */
    public void add(Geometry g) {
        g.apply(this.lineStringAdder);
    }

    /**
     * Adds a linestring to the graph of polygon edges.
     *
     * @param line the {@link LineString} to add
     */
    private void add(LineString line) {
        // create a new graph using the factory from the input Geometry
        if (this.graph == null) {
            this.graph = new PolygonizeGraph(line.getFactory());
        }
        this.graph.addEdge(line);
    }

    /**
     * Gets the list of polygons formed by the polygonization.
     *
     * @return a collection of {@link Polygon}s
     */
    public Collection getPolygons() {
        this.polygonize();
        return this.polyList;
    }

    /**
     * Gets the list of dangling lines found during polygonization.
     *
     * @return a collection of the input {@link LineString}s which are dangles
     */
    public Collection getDangles() {
        this.polygonize();
        return this.dangles;
    }

    /**
     * Gets the list of cut edges found during polygonization.
     *
     * @return a collection of the input {@link LineString}s which are cut edges
     */
    public Collection getCutEdges() {
        this.polygonize();
        return this.cutEdges;
    }

    /**
     * Gets the list of lines forming invalid rings found during polygonization.
     *
     * @return a collection of the input {@link LineString}s which form invalid rings
     */
    public Collection getInvalidRingLines() {
        this.polygonize();
        return this.invalidRingLines;
    }

    /**
     * Performs the polygonization, if it has not already been carried out.
     */
    private void polygonize() {
        // check if already computed
        if (this.polyList != null) {
            return;
        }
        this.polyList = new ArrayList();

        // if no geometries were supplied it's possible that graph is null
        if (this.graph == null) {
            return;
        }

        this.dangles = this.graph.deleteDangles();
        this.cutEdges = this.graph.deleteCutEdges();
        List edgeRingList = this.graph.getEdgeRings();

        List validEdgeRingList = new ArrayList();
        this.invalidRingLines = new ArrayList();
        this.findValidRings(edgeRingList, validEdgeRingList, this.invalidRingLines);

        this.findShellsAndHoles(validEdgeRingList);
        assignHolesToShells(this.holeList, this.shellList);

        this.polyList = new ArrayList();
        for (Object aShellList : shellList) {
            EdgeRing er = (EdgeRing) aShellList;
            this.polyList.add(er.getPolygon());
        }
    }

    private void findValidRings(List edgeRingList, List validEdgeRingList, List invalidRingList) {
        for (Object anEdgeRingList : edgeRingList) {
            EdgeRing er = (EdgeRing) anEdgeRingList;
            if (er.isValid()) {
                validEdgeRingList.add(er);
            } else {
                invalidRingList.add(er.getLineString());
            }
        }
    }

    private void findShellsAndHoles(List edgeRingList) {
        this.holeList = new ArrayList();
        this.shellList = new ArrayList();
        for (Object anEdgeRingList : edgeRingList) {
            EdgeRing er = (EdgeRing) anEdgeRingList;
            if (er.isHole()) {
                this.holeList.add(er);
            } else {
                this.shellList.add(er);
            }
        }
    }

    private static void assignHolesToShells(List holeList, List shellList) {
        for (Object aHoleList : holeList) {
            EdgeRing holeER = (EdgeRing) aHoleList;
            assignHoleToShell(holeER, shellList);
        }
    }

    private static void assignHoleToShell(EdgeRing holeER, List shellList) {
        EdgeRing shell = EdgeRing.findEdgeRingContaining(holeER, shellList);
        if (shell != null) {
            shell.addHole(holeER.getRing());
        }
    }
}
