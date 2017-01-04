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

import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.geom.TopologyException;

import java.io.PrintStream;

/**
 * @version 1.7
 */
public class DirectedEdge
        extends EdgeEnd {

    /**
     * Computes the factor for the change in depth when moving from one location to another.
     * E.g. if crossing from the INTERIOR to the EXTERIOR the depth decreases, so the factor is -1
     */
    public static int depthFactor(int currLocation, int nextLocation) {
        if (currLocation == Location.EXTERIOR && nextLocation == Location.INTERIOR) {
            return 1;
        } else if (currLocation == Location.INTERIOR && nextLocation == Location.EXTERIOR) {
            return -1;
        }
        return 0;
    }

    protected boolean isForward;
    private boolean isInResult = false;
    private boolean isVisited = false;

    private DirectedEdge sym; // the symmetric edge
    private DirectedEdge next;  // the next edge in the edge ring for the polygon containing this edge
    private DirectedEdge nextMin;  // the next edge in the MinimalEdgeRing that contains this edge
    private EdgeRing edgeRing;  // the EdgeRing that this edge is part of
    private EdgeRing minEdgeRing;  // the MinimalEdgeRing that this edge is part of
    /**
     * The depth of each side (position) of this edge.
     * The 0 element of the array is never used.
     */
    private int[] depth = { 0, -999, -999 };

    public DirectedEdge(Edge edge, boolean isForward) {
        super(edge);
        this.isForward = isForward;
        if (isForward) {
            this.init(edge.getCoordinate(0), edge.getCoordinate(1));
        } else {
            int n = edge.getNumPoints() - 1;
            this.init(edge.getCoordinate(n), edge.getCoordinate(n - 1));
        }
        this.computeDirectedLabel();
    }

    @Override
    public Edge getEdge() {
        return this.edge;
    }

    public void setInResult(boolean isInResult) {
        this.isInResult = isInResult;
    }

    public boolean isInResult() {
        return this.isInResult;
    }

    public boolean isVisited() {
        return this.isVisited;
    }

    public void setVisited(boolean isVisited) {
        this.isVisited = isVisited;
    }

    public void setEdgeRing(EdgeRing edgeRing) {
        this.edgeRing = edgeRing;
    }

    public EdgeRing getEdgeRing() {
        return this.edgeRing;
    }

    public void setMinEdgeRing(EdgeRing minEdgeRing) {
        this.minEdgeRing = minEdgeRing;
    }

    public EdgeRing getMinEdgeRing() {
        return this.minEdgeRing;
    }

    public int getDepth(int position) {
        return this.depth[position];
    }

    public void setDepth(int position, int depthVal) {
        if (this.depth[position] != -999) {
//      if (depth[position] != depthVal) {
//        Debug.print(this);
//      }
            if (this.depth[position] != depthVal) {
                throw new TopologyException("assigned depths do not match", this.getCoordinate());
            }
            //Assert.isTrue(depth[position] == depthVal, "assigned depths do not match at " + getCoordinate());
        }
        this.depth[position] = depthVal;
    }

    public int getDepthDelta() {
        int depthDelta = this.edge.getDepthDelta();
        if (!this.isForward) {
            depthDelta = -depthDelta;
        }
        return depthDelta;
    }

    /**
     * setVisitedEdge marks both DirectedEdges attached to a given Edge.
     * This is used for edges corresponding to lines, which will only
     * appear oriented in a single direction in the result.
     */
    public void setVisitedEdge(boolean isVisited) {
        this.setVisited(isVisited);
        this.sym.setVisited(isVisited);
    }

    /**
     * Each Edge gives rise to a pair of symmetric DirectedEdges, in opposite
     * directions.
     *
     * @return the DirectedEdge for the same Edge but in the opposite direction
     */
    public DirectedEdge getSym() {
        return this.sym;
    }

    public boolean isForward() {
        return this.isForward;
    }

    public void setSym(DirectedEdge de) {
        this.sym = de;
    }

    public DirectedEdge getNext() {
        return this.next;
    }

    public void setNext(DirectedEdge next) {
        this.next = next;
    }

    public DirectedEdge getNextMin() {
        return this.nextMin;
    }

    public void setNextMin(DirectedEdge nextMin) {
        this.nextMin = nextMin;
    }

    /**
     * This edge is a line edge if
     * <ul>
     * <li> at least one of the labels is a line label
     * <li> any labels which are not line labels have all Locations = EXTERIOR
     * </ul>
     */
    public boolean isLineEdge() {
        boolean isLine = this.label.isLine(0) || this.label.isLine(1);
        boolean isExteriorIfArea0 =
                !this.label.isArea(0) || this.label.allPositionsEqual(0, Location.EXTERIOR);
        boolean isExteriorIfArea1 =
                !this.label.isArea(1) || this.label.allPositionsEqual(1, Location.EXTERIOR);

        return isLine && isExteriorIfArea0 && isExteriorIfArea1;
    }

    /**
     * This is an interior Area edge if
     * <ul>
     * <li> its label is an Area label for both Geometries
     * <li> and for each Geometry both sides are in the interior.
     * </ul>
     *
     * @return true if this is an interior Area edge
     */
    public boolean isInteriorAreaEdge() {
        boolean isInteriorAreaEdge = true;
        for (int i = 0; i < 2; i++) {
            if (!(this.label.isArea(i)
                    && this.label.getLocation(i, Position.LEFT) == Location.INTERIOR
                    && this.label.getLocation(i, Position.RIGHT) == Location.INTERIOR)) {
                isInteriorAreaEdge = false;
            }
        }
        return isInteriorAreaEdge;
    }

    /**
     * Compute the label in the appropriate orientation for this DirEdge
     */
    private void computeDirectedLabel() {
        this.label = new Label(this.edge.getLabel());
        if (!this.isForward) {
            this.label.flip();
        }
    }

    /**
     * Set both edge depths.  One depth for a given side is provided.  The other is
     * computed depending on the Location transition and the depthDelta of the edge.
     */
    public void setEdgeDepths(int position, int depth) {
        // get the depth transition delta from R to L for this directed Edge
        int depthDelta = this.getEdge().getDepthDelta();
        if (!this.isForward) {
            depthDelta = -depthDelta;
        }

        // if moving from L to R instead of R to L must change sign of delta
        int directionFactor = 1;
        if (position == Position.LEFT) {
            directionFactor = -1;
        }

        int oppositePos = Position.opposite(position);
        int delta = depthDelta * directionFactor;
        //TESTINGint delta = depthDelta * DirectedEdge.depthFactor(loc, oppositeLoc);
        int oppositeDepth = depth + delta;
        this.setDepth(position, depth);
        this.setDepth(oppositePos, oppositeDepth);
    }

    @Override
    public void print(PrintStream out) {
        super.print(out);
        out.print(" " + this.depth[Position.LEFT] + "/" + this.depth[Position.RIGHT]);
        out.print(" (" + this.getDepthDelta() + ")");
        //out.print(" " + this.hashCode());
        //if (next != null) out.print(" next:" + next.hashCode());
        if (this.isInResult) {
            out.print(" inResult");
        }
    }

    public void printEdge(PrintStream out) {
        this.print(out);
        out.print(" ");
        if (this.isForward) {
            this.edge.print(out);
        } else {
            this.edge.printReverse(out);
        }
    }
}
