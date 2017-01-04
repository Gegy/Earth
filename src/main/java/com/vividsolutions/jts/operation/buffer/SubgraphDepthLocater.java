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
package com.vividsolutions.jts.operation.buffer;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geomgraph.DirectedEdge;
import com.vividsolutions.jts.geomgraph.Position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Locates a subgraph inside a set of subgraphs,
 * in order to determine the outside depth of the subgraph.
 * The input subgraphs are assumed to have had depths
 * already calculated for their edges.
 *
 * @version 1.7
 */
public class SubgraphDepthLocater {
    private Collection subgraphs;
    private LineSegment seg = new LineSegment();
    private CGAlgorithms cga = new CGAlgorithms();

    public SubgraphDepthLocater(List subgraphs) {
        this.subgraphs = subgraphs;
    }

    public int getDepth(Coordinate p) {
        List stabbedSegments = this.findStabbedSegments(p);
        // if no segments on stabbing line subgraph must be outside all others.
        if (stabbedSegments.size() == 0) {
            return 0;
        }
        Collections.sort(stabbedSegments);
        DepthSegment ds = (DepthSegment) stabbedSegments.get(0);
        return ds.leftDepth;
    }

    /**
     * Finds all non-horizontal segments intersecting the stabbing line.
     * The stabbing line is the ray to the right of stabbingRayLeftPt.
     *
     * @param stabbingRayLeftPt the left-hand origin of the stabbing line
     * @return a List of {@link DepthSegments} intersecting the stabbing line
     */
    private List findStabbedSegments(Coordinate stabbingRayLeftPt) {
        List stabbedSegments = new ArrayList();
        for (Object subgraph : subgraphs) {
            BufferSubgraph bsg = (BufferSubgraph) subgraph;

            // optimization - don't bother checking subgraphs which the ray does not intersect
            Envelope env = bsg.getEnvelope();
            if (stabbingRayLeftPt.y < env.getMinY()
                    || stabbingRayLeftPt.y > env.getMaxY()) {
                continue;
            }

            this.findStabbedSegments(stabbingRayLeftPt, bsg.getDirectedEdges(), stabbedSegments);
        }
        return stabbedSegments;
    }

    /**
     * Finds all non-horizontal segments intersecting the stabbing line
     * in the list of dirEdges.
     * The stabbing line is the ray to the right of stabbingRayLeftPt.
     *
     * @param stabbingRayLeftPt the left-hand origin of the stabbing line
     * @param stabbedSegments the current list of {@link DepthSegments} intersecting the stabbing line
     */
    private void findStabbedSegments(Coordinate stabbingRayLeftPt,
                                     List dirEdges,
                                     List stabbedSegments) {
        /**
         * Check all forward DirectedEdges only.  This is still general,
         * because each Edge has a forward DirectedEdge.
         */
        for (Object dirEdge : dirEdges) {
            DirectedEdge de = (DirectedEdge) dirEdge;
            if (!de.isForward()) {
                continue;
            }
            this.findStabbedSegments(stabbingRayLeftPt, de, stabbedSegments);
        }
    }

    /**
     * Finds all non-horizontal segments intersecting the stabbing line
     * in the input dirEdge.
     * The stabbing line is the ray to the right of stabbingRayLeftPt.
     *
     * @param stabbingRayLeftPt the left-hand origin of the stabbing line
     * @param stabbedSegments the current list of {@link DepthSegments} intersecting the stabbing line
     */
    private void findStabbedSegments(Coordinate stabbingRayLeftPt,
                                     DirectedEdge dirEdge,
                                     List stabbedSegments) {
        Coordinate[] pts = dirEdge.getEdge().getCoordinates();
        for (int i = 0; i < pts.length - 1; i++) {
            this.seg.p0 = pts[i];
            this.seg.p1 = pts[i + 1];
            // ensure segment always points upwards
            if (this.seg.p0.y > this.seg.p1.y) {
                this.seg.reverse();
            }

            // skip segment if it is left of the stabbing line
            double maxx = Math.max(this.seg.p0.x, this.seg.p1.x);
            if (maxx < stabbingRayLeftPt.x) {
                continue;
            }

            // skip horizontal segments (there will be a non-horizontal one carrying the same depth info
            if (this.seg.isHorizontal()) {
                continue;
            }

            // skip if segment is above or below stabbing line
            if (stabbingRayLeftPt.y < this.seg.p0.y || stabbingRayLeftPt.y > this.seg.p1.y) {
                continue;
            }

            // skip if stabbing ray is right of the segment
            if (CGAlgorithms.computeOrientation(this.seg.p0, this.seg.p1, stabbingRayLeftPt)
                    == CGAlgorithms.RIGHT) {
                continue;
            }

            // stabbing line cuts this segment, so record it
            int depth = dirEdge.getDepth(Position.LEFT);
            // if segment direction was flipped, use RHS depth instead
            if (!this.seg.p0.equals(pts[i])) {
                depth = dirEdge.getDepth(Position.RIGHT);
            }
            DepthSegment ds = new DepthSegment(this.seg, depth);
            stabbedSegments.add(ds);
        }
    }

    /**
     * A segment from a directed edge which has been assigned a depth value
     * for its sides.
     */
    private class DepthSegment
            implements Comparable {
        private LineSegment upwardSeg;
        private int leftDepth;

        public DepthSegment(LineSegment seg, int depth) {
            // input seg is assumed to be normalized
            this.upwardSeg = new LineSegment(seg);
            //upwardSeg.normalize();
            this.leftDepth = depth;
        }

        /**
         * Defines a comparision operation on DepthSegments
         * which orders them left to right
         * <p>
         * <pre>
         * DS1 < DS2   if   DS1.seg is left of DS2.seg
         * DS1 > DS2   if   DS1.seg is right of DS2.seg
         * </pre>
         *
         * @param obj
         * @return the comparison value
         */
        @Override
        public int compareTo(Object obj) {
            DepthSegment other = (DepthSegment) obj;
            /**
             * try and compute a determinate orientation for the segments.
             * Test returns 1 if other is left of this (i.e. this > other)
             */
            int orientIndex = this.upwardSeg.orientationIndex(other.upwardSeg);

            /**
             * If comparison between this and other is indeterminate,
             * try the opposite call order.
             * orientationIndex value is 1 if this is left of other,
             * so have to flip sign to get proper comparison value of
             * -1 if this is leftmost
             */
            if (orientIndex == 0) {
                orientIndex = -1 * other.upwardSeg.orientationIndex(this.upwardSeg);
            }

            // if orientation is determinate, return it
            if (orientIndex != 0) {
                return orientIndex;
            }

            // otherwise, segs must be collinear - sort based on minimum X value
            return this.compareX(this.upwardSeg, other.upwardSeg);
        }

        /**
         * Compare two collinear segments for left-most ordering.
         * If segs are vertical, use vertical ordering for comparison.
         * If segs are equal, return 0.
         * Segments are assumed to be directed so that the second coordinate is >= to the first
         * (e.g. up and to the right).
         *
         * @param seg0 a segment to compare
         * @param seg1 a segment to compare
         * @return
         */
        private int compareX(LineSegment seg0, LineSegment seg1) {
            int compare0 = seg0.p0.compareTo(seg1.p0);
            if (compare0 != 0) {
                return compare0;
            }
            return seg0.p1.compareTo(seg1.p1);
        }
    }
}
