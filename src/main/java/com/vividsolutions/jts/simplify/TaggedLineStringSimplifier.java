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

package com.vividsolutions.jts.simplify;

import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import java.util.List;

/**
 * Simplifies a TaggedLineString, preserving topology
 * (in the sense that no new intersections are introduced).
 * Uses the recursive Douglas-Peucker algorithm.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class TaggedLineStringSimplifier {
    private LineIntersector li = new RobustLineIntersector();
    private LineSegmentIndex inputIndex = new LineSegmentIndex();
    private LineSegmentIndex outputIndex = new LineSegmentIndex();
    private TaggedLineString line;
    private Coordinate[] linePts;
    private double distanceTolerance = 0.0;

    public TaggedLineStringSimplifier(LineSegmentIndex inputIndex,
                                      LineSegmentIndex outputIndex) {
        this.inputIndex = inputIndex;
        this.outputIndex = outputIndex;
    }

    /**
     * Sets the distance tolerance for the simplification.
     * All vertices in the simplified geometry will be within this
     * distance of the original geometry.
     *
     * @param distanceTolerance the approximation tolerance to use
     */
    public void setDistanceTolerance(double distanceTolerance) {
        this.distanceTolerance = distanceTolerance;
    }

    /**
     * Simplifies the given {@link TaggedLineString}
     * using the distance tolerance specified.
     *
     * @param line the linestring to simplify
     */
    void simplify(TaggedLineString line) {
        this.line = line;
        this.linePts = line.getParentCoordinates();
        this.simplifySection(0, this.linePts.length - 1, 0);
    }

    private void simplifySection(int i, int j, int depth) {
        depth += 1;
        int[] sectionIndex = new int[2];
        if ((i + 1) == j) {
            LineSegment newSeg = this.line.getSegment(i);
            this.line.addToResult(newSeg);
            // leave this segment in the input index, for efficiency
            return;
        }

        boolean isValidToSimplify = true;

        /**
         * Following logic ensures that there is enough points in the output line.
         * If there is already more points than the minimum, there's nothing to check.
         * Otherwise, if in the worst case there wouldn't be enough points,
         * don't flatten this segment (which avoids the worst case scenario)
         */
        if (this.line.getResultSize() < this.line.getMinimumSize()) {
            int worstCaseSize = depth + 1;
            if (worstCaseSize < this.line.getMinimumSize()) {
                isValidToSimplify = false;
            }
        }

        double[] distance = new double[1];
        int furthestPtIndex = this.findFurthestPoint(this.linePts, i, j, distance);
        // flattening must be less than distanceTolerance
        if (distance[0] > this.distanceTolerance) {
            isValidToSimplify = false;
        }
        // test if flattened section would cause intersection
        LineSegment candidateSeg = new LineSegment();
        candidateSeg.p0 = this.linePts[i];
        candidateSeg.p1 = this.linePts[j];
        sectionIndex[0] = i;
        sectionIndex[1] = j;
        if (this.hasBadIntersection(this.line, sectionIndex, candidateSeg)) {
            isValidToSimplify = false;
        }

        if (isValidToSimplify) {
            LineSegment newSeg = this.flatten(i, j);
            this.line.addToResult(newSeg);
            return;
        }
        this.simplifySection(i, furthestPtIndex, depth);
        this.simplifySection(furthestPtIndex, j, depth);
    }

    private int findFurthestPoint(Coordinate[] pts, int i, int j, double[] maxDistance) {
        LineSegment seg = new LineSegment();
        seg.p0 = pts[i];
        seg.p1 = pts[j];
        double maxDist = -1.0;
        int maxIndex = i;
        for (int k = i + 1; k < j; k++) {
            Coordinate midPt = pts[k];
            double distance = seg.distance(midPt);
            if (distance > maxDist) {
                maxDist = distance;
                maxIndex = k;
            }
        }
        maxDistance[0] = maxDist;
        return maxIndex;
    }

    private LineSegment flatten(int start, int end) {
        // make a new segment for the simplified geometry
        Coordinate p0 = this.linePts[start];
        Coordinate p1 = this.linePts[end];
        LineSegment newSeg = new LineSegment(p0, p1);
// update the indexes
        this.remove(this.line, start, end);
        this.outputIndex.add(newSeg);
        return newSeg;
    }

    /**
     * Index of section to be tested for flattening - reusable
     */
    private int[] validSectionIndex = new int[2];

    private boolean hasBadIntersection(TaggedLineString parentLine,
                                       int[] sectionIndex,
                                       LineSegment candidateSeg) {
        if (this.hasBadOutputIntersection(candidateSeg)) {
            return true;
        }
        return hasBadInputIntersection(parentLine, sectionIndex, candidateSeg);
    }

    private boolean hasBadOutputIntersection(LineSegment candidateSeg) {
        List querySegs = this.outputIndex.query(candidateSeg);
        for (Object querySeg1 : querySegs) {
            LineSegment querySeg = (LineSegment) querySeg1;
            if (this.hasInteriorIntersection(querySeg, candidateSeg)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBadInputIntersection(TaggedLineString parentLine,
                                            int[] sectionIndex,
                                            LineSegment candidateSeg) {
        List querySegs = this.inputIndex.query(candidateSeg);
        for (Object querySeg1 : querySegs) {
            TaggedLineSegment querySeg = (TaggedLineSegment) querySeg1;
            if (this.hasInteriorIntersection(querySeg, candidateSeg)) {
                if (isInLineSection(parentLine, sectionIndex, querySeg)) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Tests whether a segment is in a section of a TaggedLineString
     *
     * @param line
     * @param sectionIndex
     * @param seg
     * @return
     */
    private static boolean isInLineSection(
            TaggedLineString line,
            int[] sectionIndex,
            TaggedLineSegment seg) {
        // not in this line
        if (seg.getParent() != line.getParent()) {
            return false;
        }
        int segIndex = seg.getIndex();
        return segIndex >= sectionIndex[0] && segIndex < sectionIndex[1];
    }

    private boolean hasInteriorIntersection(LineSegment seg0, LineSegment seg1) {
        this.li.computeIntersection(seg0.p0, seg0.p1, seg1.p0, seg1.p1);
        return this.li.isInteriorIntersection();
    }

    /**
     * Remove the segs in the section of the line
     *
     * @param line
     * @param pts
     * @param sectionStartIndex
     * @param sectionEndIndex
     */
    private void remove(TaggedLineString line,
                        int start, int end) {
        for (int i = start; i < end; i++) {
            TaggedLineSegment seg = line.getSegment(i);
            this.inputIndex.remove(seg);
        }
    }
}
