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
package com.vividsolutions.jts.noding;

import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;
//import com.vividsolutions.jts.util.Debug;

/**
 * Finds an interior intersection in a set of {@link SegmentString}s,
 * if one exists.  Only the first intersection found is reported.
 *
 * @version 1.7
 */
public class InteriorIntersectionFinder
        implements SegmentIntersector {
    private boolean findAllIntersections = false;
    private boolean isCheckEndSegmentsOnly = false;
    private LineIntersector li;
    private Coordinate interiorIntersection = null;
    private Coordinate[] intSegments = null;
    private List intersections = new ArrayList();

    /**
     * Creates an intersection finder which finds an interior intersection
     * if one exists
     *
     * @param li the LineIntersector to use
     */
    public InteriorIntersectionFinder(LineIntersector li) {
        this.li = li;
        this.interiorIntersection = null;
    }

    public void setFindAllIntersections(boolean findAllIntersections) {
        this.findAllIntersections = findAllIntersections;
    }

    public List getIntersections() {
        return this.intersections;
    }

    /**
     * Sets whether only end segments should be tested for interior intersection.
     * This is a performance optimization that may be used if
     * the segments have been previously noded by an appropriate algorithm.
     * It may be known that any potential noding failures will occur only in
     * end segments.
     *
     * @param isCheckEndSegmentsOnly whether to test only end segments
     */
    public void setCheckEndSegmentsOnly(boolean isCheckEndSegmentsOnly) {
        this.isCheckEndSegmentsOnly = isCheckEndSegmentsOnly;
    }

    /**
     * Tests whether an intersection was found.
     *
     * @return true if an intersection was found
     */
    public boolean hasIntersection() {
        return this.interiorIntersection != null;
    }

    /**
     * Gets the computed location of the intersection.
     * Due to round-off, the location may not be exact.
     *
     * @return the coordinate for the intersection location
     */
    public Coordinate getInteriorIntersection() {
        return this.interiorIntersection;
    }

    /**
     * Gets the endpoints of the intersecting segments.
     *
     * @return an array of the segment endpoints (p00, p01, p10, p11)
     */
    public Coordinate[] getIntersectionSegments() {
        return this.intSegments;
    }

    /**
     * This method is called by clients
     * of the {@link SegmentIntersector} class to process
     * intersections for two segments of the {@link SegmentString}s being intersected.
     * Note that some clients (such as <code>MonotoneChain</code>s) may optimize away
     * this call for segment pairs which they have determined do not intersect
     * (e.g. by an disjoint envelope test).
     */
    @Override
    public void processIntersections(
            SegmentString e0, int segIndex0,
            SegmentString e1, int segIndex1
    ) {
        // short-circuit if intersection already found
        if (this.hasIntersection()) {
            return;
        }

        // don't bother intersecting a segment with itself
        if (e0 == e1 && segIndex0 == segIndex1) {
            return;
        }

        /**
         * If enabled, only test end segments (on either segString).
         *
         */
        if (this.isCheckEndSegmentsOnly) {
            boolean isEndSegPresent = this.isEndSegment(e0, segIndex0) || this.isEndSegment(e1, segIndex1);
            if (!isEndSegPresent) {
                return;
            }
        }

        Coordinate p00 = e0.getCoordinates()[segIndex0];
        Coordinate p01 = e0.getCoordinates()[segIndex0 + 1];
        Coordinate p10 = e1.getCoordinates()[segIndex1];
        Coordinate p11 = e1.getCoordinates()[segIndex1 + 1];

        this.li.computeIntersection(p00, p01, p10, p11);
//if (li.hasIntersection() && li.isProper()) Debug.println(li);

        if (this.li.hasIntersection()) {
            if (this.li.isInteriorIntersection()) {
                this.intSegments = new Coordinate[4];
                this.intSegments[0] = p00;
                this.intSegments[1] = p01;
                this.intSegments[2] = p10;
                this.intSegments[3] = p11;

                this.interiorIntersection = this.li.getIntersection(0);
                this.intersections.add(this.interiorIntersection);
            }
        }
    }

    /**
     * Tests whether a segment in a {@link SegmentString} is an end segment.
     * (either the first or last).
     *
     * @param segStr a segment string
     * @param index the index of a segment in the segment string
     * @return true if the segment is an end segment
     */
    private boolean isEndSegment(SegmentString segStr, int index) {
        if (index == 0) {
            return true;
        }
        return index >= segStr.size() - 2;
    }

    @Override
    public boolean isDone() {
        if (this.findAllIntersections) {
            return false;
        }
        return this.interiorIntersection != null;
    }
}