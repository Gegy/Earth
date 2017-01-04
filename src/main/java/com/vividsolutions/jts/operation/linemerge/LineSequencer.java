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
package com.vividsolutions.jts.operation.linemerge;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.GraphComponent;
import com.vividsolutions.jts.planargraph.Node;
import com.vividsolutions.jts.planargraph.Subgraph;
import com.vividsolutions.jts.planargraph.algorithm.ConnectedSubgraphFinder;
import com.vividsolutions.jts.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Builds a sequence from a set of LineStrings so that
 * they are ordered end to end.
 * A sequence is a complete non-repeating list of the linear
 * components of the input.  Each linestring is oriented
 * so that identical endpoints are adjacent in the list.
 * <p>
 * A typical use case is to convert a set of
 * unoriented geometric links
 * from a linear network
 * (e.g. such as block faces on a bus route)
 * into a continuous oriented path through the network.
 * <p>
 * The input linestrings may form one or more connected sets.
 * The input linestrings should be correctly noded, or the results may
 * not be what is expected.
 * The computed output is a single {@link MultiLineString} containing the ordered
 * linestrings in the sequence.
 * <p>
 * The sequencing employs the classic <b>Eulerian path</b> graph algorithm.
 * Since Eulerian paths are not uniquely determined,
 * further rules are used to
 * make the computed sequence preserve as much as possible of the input
 * ordering.
 * Within a connected subset of lines, the ordering rules are:
 * <ul>
 * <li>If there is degree-1 node which is the start
 * node of an linestring, use that node as the start of the sequence
 * <li>If there is a degree-1 node which is the end
 * node of an linestring, use that node as the end of the sequence
 * <li>If the sequence has no degree-1 nodes, use any node as the start
 * </ul>
 * <p>
 * Note that not all arrangements of lines can be sequenced.
 * For a connected set of edges in a graph,
 * <i>Euler's Theorem</i> states that there is a sequence containing each edge once
 * <b>if and only if</b> there are no more than 2 nodes of odd degree.
 * If it is not possible to find a sequence, the {@link #isSequenceable()} method
 * will return <code>false</code>.
 *
 * @version 1.7
 */
public class LineSequencer {
    public static Geometry sequence(Geometry geom) {
        LineSequencer sequencer = new LineSequencer();
        sequencer.add(geom);
        return sequencer.getSequencedLineStrings();
    }

    /**
     * Tests whether a {@link Geometry} is sequenced correctly.
     * {@link LineString}s are trivially sequenced.
     * {@link MultiLineString}s are checked for correct sequencing.
     * Otherwise, <code>isSequenced</code> is defined
     * to be <code>true</code> for geometries that are not lineal.
     *
     * @param geom the geometry to test
     * @return <code>true</code> if the geometry is sequenced or is not lineal
     */
    public static boolean isSequenced(Geometry geom) {
        if (!(geom instanceof MultiLineString)) {
            return true;
        }

        MultiLineString mls = (MultiLineString) geom;
        // the nodes in all subgraphs which have been completely scanned
        Set prevSubgraphNodes = new TreeSet();

        Coordinate lastNode = null;
        List currNodes = new ArrayList();
        for (int i = 0; i < mls.getNumGeometries(); i++) {
            LineString line = (LineString) mls.getGeometryN(i);
            Coordinate startNode = line.getCoordinateN(0);
            Coordinate endNode = line.getCoordinateN(line.getNumPoints() - 1);

            /**
             * If this linestring is connected to a previous subgraph, geom is not sequenced
             */
            if (prevSubgraphNodes.contains(startNode)) {
                return false;
            }
            if (prevSubgraphNodes.contains(endNode)) {
                return false;
            }

            if (lastNode != null) {
                if (!startNode.equals(lastNode)) {
                    // start new connected sequence
                    prevSubgraphNodes.addAll(currNodes);
                    currNodes.clear();
                }
            }
            currNodes.add(startNode);
            currNodes.add(endNode);
            lastNode = endNode;
        }
        return true;
    }

    private LineMergeGraph graph = new LineMergeGraph();
    // initialize with default, in case no lines are input
    private GeometryFactory factory = new GeometryFactory();
    private int lineCount = 0;

    private boolean isRun = false;
    private Geometry sequencedGeometry = null;
    private boolean isSequenceable = false;

    /**
     * Adds a {@link Collection} of {@link Geometry}s to be sequenced.
     * May be called multiple times.
     * Any dimension of Geometry may be added; the constituent linework will be
     * extracted.
     *
     * @param geometries a Collection of geometries to add
     */
    public void add(Collection geometries) {
        for (Object geometry1 : geometries) {
            Geometry geometry = (Geometry) geometry1;
            this.add(geometry);
        }
    }

    /**
     * Adds a {@link Geometry} to be sequenced.
     * May be called multiple times.
     * Any dimension of Geometry may be added; the constituent linework will be
     * extracted.
     *
     * @param geometry the geometry to add
     */
    public void add(Geometry geometry) {
        geometry.apply((GeometryComponentFilter) component -> {
            if (component instanceof LineString) {
                addLine((LineString) component);
            }
        });
    }

    private void addLine(LineString lineString) {
        if (this.factory == null) {
            this.factory = lineString.getFactory();
        }
        this.graph.addEdge(lineString);
        this.lineCount++;
    }

    /**
     * Tests whether the arrangement of linestrings has a valid
     * sequence.
     *
     * @return <code>true</code> if a valid sequence exists.
     */
    public boolean isSequenceable() {
        this.computeSequence();
        return this.isSequenceable;
    }

    /**
     * Returns the {@link LineString} or {@link MultiLineString}
     * built by the sequencing process, if one exists.
     *
     * @return the sequenced linestrings,
     * or <code>null</code> if a valid sequence does not exist
     */
    public Geometry getSequencedLineStrings() {
        this.computeSequence();
        return this.sequencedGeometry;
    }

    private void computeSequence() {
        if (this.isRun) {
            return;
        }
        this.isRun = true;

        List sequences = this.findSequences();
        if (sequences == null) {
            return;
        }

        this.sequencedGeometry = this.buildSequencedGeometry(sequences);
        this.isSequenceable = true;

        int finalLineCount = this.sequencedGeometry.getNumGeometries();
        Assert.isTrue(this.lineCount == finalLineCount, "Lines were missing from result");
        Assert.isTrue(this.sequencedGeometry instanceof LineString
                        || this.sequencedGeometry instanceof MultiLineString,
                "Result is not lineal");
    }

    private List findSequences() {
        List sequences = new ArrayList();
        ConnectedSubgraphFinder csFinder = new ConnectedSubgraphFinder(this.graph);
        List subgraphs = csFinder.getConnectedSubgraphs();
        for (Object subgraph1 : subgraphs) {
            Subgraph subgraph = (Subgraph) subgraph1;
            if (this.hasSequence(subgraph)) {
                List seq = this.findSequence(subgraph);
                sequences.add(seq);
            } else {
                // if any subgraph cannot be sequenced, abort
                return null;
            }
        }
        return sequences;
    }

    /**
     * Tests whether a complete unique path exists in a graph
     * using Euler's Theorem.
     *
     * @param graph the subgraph containing the edges
     * @return <code>true</code> if a sequence exists
     */
    private boolean hasSequence(Subgraph graph) {
        int oddDegreeCount = 0;
        for (Iterator i = graph.nodeIterator(); i.hasNext(); ) {
            Node node = (Node) i.next();
            if (node.getDegree() % 2 == 1) {
                oddDegreeCount++;
            }
        }
        return oddDegreeCount <= 2;
    }

    private List findSequence(Subgraph graph) {
        GraphComponent.setVisited(graph.edgeIterator(), false);

        Node startNode = findLowestDegreeNode(graph);
        DirectedEdge startDE = (DirectedEdge) startNode.getOutEdges().iterator().next();
        DirectedEdge startDESym = startDE.getSym();

        List seq = new LinkedList();
        ListIterator lit = seq.listIterator();
        this.addReverseSubpath(startDESym, lit, false);
        while (lit.hasPrevious()) {
            DirectedEdge prev = (DirectedEdge) lit.previous();
            DirectedEdge unvisitedOutDE = findUnvisitedBestOrientedDE(prev.getFromNode());
            if (unvisitedOutDE != null) {
                this.addReverseSubpath(unvisitedOutDE.getSym(), lit, true);
            }
        }

        /**
         * At this point, we have a valid sequence of graph DirectedEdges, but it
         * is not necessarily appropriately oriented relative to the underlying
         * geometry.
         */
        List orientedSeq = this.orient(seq);
        return orientedSeq;
    }

    /**
     * Finds an {@link DirectedEdge} for an unvisited edge (if any),
     * choosing the dirEdge which preserves orientation, if possible.
     *
     * @param node the node to examine
     * @return the dirEdge found, or <code>null</code> if none were unvisited
     */
    private static DirectedEdge findUnvisitedBestOrientedDE(Node node) {
        DirectedEdge wellOrientedDE = null;
        DirectedEdge unvisitedDE = null;
        for (Iterator i = node.getOutEdges().iterator(); i.hasNext(); ) {
            DirectedEdge de = (DirectedEdge) i.next();
            if (!de.getEdge().isVisited()) {
                unvisitedDE = de;
                if (de.getEdgeDirection()) {
                    wellOrientedDE = de;
                }
            }
        }
        if (wellOrientedDE != null) {
            return wellOrientedDE;
        }
        return unvisitedDE;
    }

    private void addReverseSubpath(DirectedEdge de, ListIterator lit, boolean expectedClosed) {
        // trace an unvisited path *backwards* from this de
        Node endNode = de.getToNode();

        Node fromNode = null;
        while (true) {
            lit.add(de.getSym());
            de.getEdge().setVisited(true);
            fromNode = de.getFromNode();
            DirectedEdge unvisitedOutDE = findUnvisitedBestOrientedDE(fromNode);
            // this must terminate, since we are continually marking edges as visited
            if (unvisitedOutDE == null) {
                break;
            }
            de = unvisitedOutDE.getSym();
        }
        if (expectedClosed) {
            // the path should end at the toNode of this de, otherwise we have an error
            Assert.isTrue(fromNode == endNode, "path not contiguous");
        }
    }

    private static Node findLowestDegreeNode(Subgraph graph) {
        int minDegree = Integer.MAX_VALUE;
        Node minDegreeNode = null;
        for (Iterator i = graph.nodeIterator(); i.hasNext(); ) {
            Node node = (Node) i.next();
            if (minDegreeNode == null || node.getDegree() < minDegree) {
                minDegree = node.getDegree();
                minDegreeNode = node;
            }
        }
        return minDegreeNode;
    }

    /**
     * Computes a version of the sequence which is optimally
     * oriented relative to the underlying geometry.
     * <p>
     * Heuristics used are:
     * <ul>
     * <li>If the path has a degree-1 node which is the start
     * node of an linestring, use that node as the start of the sequence
     * <li>If the path has a degree-1 node which is the end
     * node of an linestring, use that node as the end of the sequence
     * <li>If the sequence has no degree-1 nodes, use any node as the start
     * (NOTE: in this case could orient the sequence according to the majority of the
     * linestring orientations)
     * </ul>
     *
     * @param seq a List of DirectedEdges
     * @return a List of DirectedEdges oriented appropriately
     */
    private List orient(List seq) {
        DirectedEdge startEdge = (DirectedEdge) seq.get(0);
        DirectedEdge endEdge = (DirectedEdge) seq.get(seq.size() - 1);
        Node startNode = startEdge.getFromNode();
        Node endNode = endEdge.getToNode();

        boolean flipSeq = false;
        boolean hasDegree1Node = startNode.getDegree() == 1
                || endNode.getDegree() == 1;

        if (hasDegree1Node) {
            boolean hasObviousStartNode = false;

            // test end edge before start edge, to make result stable
            // (ie. if both are good starts, pick the actual start
            if (endEdge.getToNode().getDegree() == 1 && !endEdge.getEdgeDirection()) {
                hasObviousStartNode = true;
                flipSeq = true;
            }
            if (startEdge.getFromNode().getDegree() == 1 && startEdge.getEdgeDirection()) {
                hasObviousStartNode = true;
                flipSeq = false;
            }

            // since there is no obvious start node, use any node of degree 1
            if (!hasObviousStartNode) {
                // check if the start node should actually be the end node
                if (startEdge.getFromNode().getDegree() == 1) {
                    flipSeq = true;
                }
                // if the end node is of degree 1, it is properly the end node
            }
        }

        // if there is no degree 1 node, just use the sequence as is
        // (Could insert heuristic of taking direction of majority of lines as overall direction)

        if (flipSeq) {
            return this.reverse(seq);
        }
        return seq;
    }

    /**
     * Reverse the sequence.
     * This requires reversing the order of the dirEdges, and flipping
     * each dirEdge as well
     *
     * @param seq a List of DirectedEdges, in sequential order
     * @return the reversed sequence
     */
    private List reverse(List seq) {
        LinkedList newSeq = new LinkedList();
        for (Object aSeq : seq) {
            DirectedEdge de = (DirectedEdge) aSeq;
            newSeq.addFirst(de.getSym());
        }
        return newSeq;
    }

    /**
     * Builds a geometry ({@link LineString} or {@link MultiLineString} )
     * representing the sequence.
     *
     * @param sequences a List of Lists of DirectedEdges with
     * LineMergeEdges as their parent edges.
     * @return the sequenced geometry, or <code>null</code> if no sequence exists
     */
    private Geometry buildSequencedGeometry(List sequences) {
        List lines = new ArrayList();

        for (Object sequence : sequences) {
            List seq = (List) sequence;
            for (Object aSeq : seq) {
                DirectedEdge de = (DirectedEdge) aSeq;
                LineMergeEdge e = (LineMergeEdge) de.getEdge();
                LineString line = e.getLine();

                LineString lineToAdd = line;
                if (!de.getEdgeDirection() && !line.isClosed()) {
                    lineToAdd = reverse(line);
                }

                lines.add(lineToAdd);
            }
        }
        if (lines.size() == 0) {
            return this.factory.createMultiLineString(new LineString[0]);
        }
        return this.factory.buildGeometry(lines);
    }

    private static LineString reverse(LineString line) {
        Coordinate[] pts = line.getCoordinates();
        Coordinate[] revPts = new Coordinate[pts.length];
        int len = pts.length;
        for (int i = 0; i < len; i++) {
            revPts[len - 1 - i] = new Coordinate(pts[i]);
        }
        return line.getFactory().createLineString(revPts);
    }
}
