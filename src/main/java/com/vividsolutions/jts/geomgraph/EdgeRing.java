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
package com.vividsolutions.jts.geomgraph;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version 1.7
 */
public abstract class EdgeRing {

    protected DirectedEdge startDe; // the directed edge which starts the list of edges for this EdgeRing
    private int maxNodeDegree = -1;
    private List edges = new ArrayList(); // the DirectedEdges making up this EdgeRing
    private List pts = new ArrayList();
    private Label label = new Label(Location.NONE); // label stores the locations of each geometry on the face surrounded by this ring
    private LinearRing ring;  // the ring created for this EdgeRing
    private boolean isHole;
    private EdgeRing shell;   // if non-null, the ring is a hole and this EdgeRing is its containing shell
    private ArrayList holes = new ArrayList(); // a list of EdgeRings which are holes in this EdgeRing

    protected GeometryFactory geometryFactory;

    public EdgeRing(DirectedEdge start, GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        this.computePoints(start);
        this.computeRing();
    }

    public boolean isIsolated() {
        return (this.label.getGeometryCount() == 1);
    }

    public boolean isHole() {
        //computePoints();
        return this.isHole;
    }

    public Coordinate getCoordinate(int i) {
        return (Coordinate) this.pts.get(i);
    }

    public LinearRing getLinearRing() {
        return this.ring;
    }

    public Label getLabel() {
        return this.label;
    }

    public boolean isShell() {
        return this.shell == null;
    }

    public EdgeRing getShell() {
        return this.shell;
    }

    public void setShell(EdgeRing shell) {
        this.shell = shell;
        if (shell != null) {
            shell.addHole(this);
        }
    }

    public void addHole(EdgeRing ring) {
        this.holes.add(ring);
    }

    public Polygon toPolygon(GeometryFactory geometryFactory) {
        LinearRing[] holeLR = new LinearRing[this.holes.size()];
        for (int i = 0; i < this.holes.size(); i++) {
            holeLR[i] = ((EdgeRing) this.holes.get(i)).getLinearRing();
        }
        Polygon poly = geometryFactory.createPolygon(this.getLinearRing(), holeLR);
        return poly;
    }

    /**
     * Compute a LinearRing from the point list previously collected.
     * Test if the ring is a hole (i.e. if it is CCW) and set the hole flag
     * accordingly.
     */
    public void computeRing() {
        if (this.ring != null) {
            return;   // don't compute more than once
        }
        Coordinate[] coord = new Coordinate[this.pts.size()];
        for (int i = 0; i < this.pts.size(); i++) {
            coord[i] = (Coordinate) this.pts.get(i);
        }
        this.ring = this.geometryFactory.createLinearRing(coord);
        this.isHole = CGAlgorithms.isCCW(this.ring.getCoordinates());
//Debug.println( (isHole ? "hole - " : "shell - ") + WKTWriter.toLineString(new CoordinateArraySequence(ring.getCoordinates())));
    }

    abstract public DirectedEdge getNext(DirectedEdge de);

    abstract public void setEdgeRing(DirectedEdge de, EdgeRing er);

    /**
     * Returns the list of DirectedEdges that make up this EdgeRing
     */
    public List getEdges() {
        return this.edges;
    }

    /**
     * Collect all the points from the DirectedEdges of this ring into a contiguous list
     */
    protected void computePoints(DirectedEdge start) {
//System.out.println("buildRing");
        this.startDe = start;
        DirectedEdge de = start;
        boolean isFirstEdge = true;
        do {
//      Assert.isTrue(de != null, "found null Directed Edge");
            if (de == null) {
                throw new TopologyException("Found null DirectedEdge");
            }
            if (de.getEdgeRing() == this) {
                throw new TopologyException("Directed Edge visited twice during ring-building at " + de.getCoordinate());
            }

            this.edges.add(de);
//Debug.println(de);
//Debug.println(de.getEdge());
            Label label = de.getLabel();
            Assert.isTrue(label.isArea());
            this.mergeLabel(label);
            this.addPoints(de.getEdge(), de.isForward(), isFirstEdge);
            isFirstEdge = false;
            this.setEdgeRing(de, this);
            de = this.getNext(de);
        } while (de != this.startDe);
    }

    public int getMaxNodeDegree() {
        if (this.maxNodeDegree < 0) {
            this.computeMaxNodeDegree();
        }
        return this.maxNodeDegree;
    }

    private void computeMaxNodeDegree() {
        this.maxNodeDegree = 0;
        DirectedEdge de = this.startDe;
        do {
            Node node = de.getNode();
            int degree = ((DirectedEdgeStar) node.getEdges()).getOutgoingDegree(this);
            if (degree > this.maxNodeDegree) {
                this.maxNodeDegree = degree;
            }
            de = this.getNext(de);
        } while (de != this.startDe);
        this.maxNodeDegree *= 2;
    }

    public void setInResult() {
        DirectedEdge de = this.startDe;
        do {
            de.getEdge().setInResult(true);
            de = de.getNext();
        } while (de != this.startDe);
    }

    protected void mergeLabel(Label deLabel) {
        this.mergeLabel(deLabel, 0);
        this.mergeLabel(deLabel, 1);
    }

    /**
     * Merge the RHS label from a DirectedEdge into the label for this EdgeRing.
     * The DirectedEdge label may be null.  This is acceptable - it results
     * from a node which is NOT an intersection node between the Geometries
     * (e.g. the end node of a LinearRing).  In this case the DirectedEdge label
     * does not contribute any information to the overall labelling, and is simply skipped.
     */
    protected void mergeLabel(Label deLabel, int geomIndex) {
        int loc = deLabel.getLocation(geomIndex, Position.RIGHT);
        // no information to be had from this label
        if (loc == Location.NONE) {
            return;
        }
        // if there is no current RHS value, set it
        if (this.label.getLocation(geomIndex) == Location.NONE) {
            this.label.setLocation(geomIndex, loc);
        }
    }

    protected void addPoints(Edge edge, boolean isForward, boolean isFirstEdge) {
        Coordinate[] edgePts = edge.getCoordinates();
        if (isForward) {
            int startIndex = 1;
            if (isFirstEdge) {
                startIndex = 0;
            }
            pts.addAll(Arrays.asList(edgePts).subList(startIndex, edgePts.length));
        } else { // is backward
            int startIndex = edgePts.length - 2;
            if (isFirstEdge) {
                startIndex = edgePts.length - 1;
            }
            for (int i = startIndex; i >= 0; i--) {
                this.pts.add(edgePts[i]);
            }
        }
    }

    /**
     * This method will cause the ring to be computed.
     * It will also check any holes, if they have been assigned.
     */
    public boolean containsPoint(Coordinate p) {
        LinearRing shell = this.getLinearRing();
        Envelope env = shell.getEnvelopeInternal();
        if (!env.contains(p)) {
            return false;
        }
        if (!CGAlgorithms.isPointInRing(p, shell.getCoordinates())) {
            return false;
        }

        for (Object hole1 : holes) {
            EdgeRing hole = (EdgeRing) hole1;
            if (hole.containsPoint(p)) {
                return false;
            }
        }
        return true;
    }
}
