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

package com.vividsolutions.jts.operation.overlay;

import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.geomgraph.DirectedEdge;
import com.vividsolutions.jts.geomgraph.DirectedEdgeStar;
import com.vividsolutions.jts.geomgraph.GeometryGraph;
import com.vividsolutions.jts.geomgraph.Label;
import com.vividsolutions.jts.geomgraph.Node;
import com.vividsolutions.jts.geomgraph.PlanarGraph;
import com.vividsolutions.jts.geomgraph.Position;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tests whether the polygon rings in a {@link GeometryGraph}
 * are consistent.
 * Used for checking if Topology errors are present after noding.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class ConsistentPolygonRingChecker {
    private PlanarGraph graph;

    public ConsistentPolygonRingChecker(PlanarGraph graph) {
        this.graph = graph;
    }

    public void checkAll() {
        this.check(OverlayOp.INTERSECTION);
        this.check(OverlayOp.DIFFERENCE);
        this.check(OverlayOp.UNION);
        this.check(OverlayOp.SYMDIFFERENCE);
    }

    /**
     * Tests whether the result geometry is consistent
     *
     * @throws TopologyException if inconsistent topology is found
     */
    public void check(int opCode) {
        for (Iterator nodeit = this.graph.getNodeIterator(); nodeit.hasNext(); ) {
            Node node = (Node) nodeit.next();
            this.testLinkResultDirectedEdges((DirectedEdgeStar) node.getEdges(), opCode);
        }
    }

    private List getPotentialResultAreaEdges(DirectedEdgeStar deStar, int opCode) {
//print(System.out);
        List resultAreaEdgeList = new ArrayList();
        for (Iterator it = deStar.iterator(); it.hasNext(); ) {
            DirectedEdge de = (DirectedEdge) it.next();
            if (this.isPotentialResultAreaEdge(de, opCode) || this.isPotentialResultAreaEdge(de.getSym(), opCode)) {
                resultAreaEdgeList.add(de);
            }
        }
        return resultAreaEdgeList;
    }

    private boolean isPotentialResultAreaEdge(DirectedEdge de, int opCode) {
        // mark all dirEdges with the appropriate label
        Label label = de.getLabel();
        return label.isArea()
                && !de.isInteriorAreaEdge()
                && OverlayOp.isResultOfOp(
                label.getLocation(0, Position.RIGHT),
                label.getLocation(1, Position.RIGHT),
                opCode);
    }

    private final int SCANNING_FOR_INCOMING = 1;
    private final int LINKING_TO_OUTGOING = 2;

    private void testLinkResultDirectedEdges(DirectedEdgeStar deStar, int opCode) {
        // make sure edges are copied to resultAreaEdges list
        List ringEdges = this.getPotentialResultAreaEdges(deStar, opCode);
        // find first area edge (if any) to start linking at
        DirectedEdge firstOut = null;
        DirectedEdge incoming = null;
        int state = this.SCANNING_FOR_INCOMING;
        // link edges in CCW order
        for (Object ringEdge : ringEdges) {
            DirectedEdge nextOut = (DirectedEdge) ringEdge;
            DirectedEdge nextIn = nextOut.getSym();

            // skip de's that we're not interested in
            if (!nextOut.getLabel().isArea()) {
                continue;
            }

            // record first outgoing edge, in order to link the last incoming edge
            if (firstOut == null
                    && this.isPotentialResultAreaEdge(nextOut, opCode)) {
                firstOut = nextOut;
            }
            // assert: sym.isInResult() == false, since pairs of dirEdges should have been removed already

            switch (state) {
                case this.SCANNING_FOR_INCOMING:
                    if (!this.isPotentialResultAreaEdge(nextIn, opCode)) {
                        continue;
                    }
                    incoming = nextIn;
                    state = this.LINKING_TO_OUTGOING;
                    break;
                case this.LINKING_TO_OUTGOING:
                    if (!this.isPotentialResultAreaEdge(nextOut, opCode)) {
                        continue;
                    }
                    //incoming.setNext(nextOut);
                    state = this.SCANNING_FOR_INCOMING;
                    break;
            }
        }
//Debug.print(this);
        if (state == this.LINKING_TO_OUTGOING) {
//Debug.print(firstOut == null, this);
            if (firstOut == null) {
                throw new TopologyException("no outgoing dirEdge found", deStar.getCoordinate());
            }
        }
    }
}
