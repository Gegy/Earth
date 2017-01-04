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
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.io.WKTWriter;

import java.util.Collection;
import java.util.List;

/**
 * Validates that a collection of {@link SegmentString}s is correctly noded.
 * Indexing is used to improve performance.
 * In the most common use case, validation stops after a single
 * non-noded intersection is detected,
 * but the class can be requested to detect all intersections
 * by using the {@link #setFindAllIntersections(boolean)} method.
 * <p>
 * The validator does not check for a-b-a topology collapse situations.
 * <p>
 * The validator does not check for endpoint-interior vertex intersections.
 * This should not be a problem, since the JTS noders should be
 * able to compute intersections between vertices correctly.
 * <p>
 * The client may either test the {@link #isValid()} condition,
 * or request that a suitable {@link TopologyException} be thrown.
 *
 * @version 1.7
 */
public class FastNodingValidator {
    private LineIntersector li = new RobustLineIntersector();

    private Collection segStrings;
    private boolean findAllIntersections = false;
    private InteriorIntersectionFinder segInt = null;
    private boolean isValid = true;

    /**
     * Creates a new noding validator for a given set of linework.
     *
     * @param segStrings a collection of {@link SegmentString}s
     */
    public FastNodingValidator(Collection segStrings) {
        this.segStrings = segStrings;
    }

    public void setFindAllIntersections(boolean findAllIntersections) {
        this.findAllIntersections = findAllIntersections;
    }

    public List getIntersections() {
        return this.segInt.getIntersections();
    }

    /**
     * Checks for an intersection and
     * reports if one is found.
     *
     * @return true if the arrangement contains an interior intersection
     */
    public boolean isValid() {
        this.execute();
        return this.isValid;
    }

    /**
     * Returns an error message indicating the segments containing
     * the intersection.
     *
     * @return an error message documenting the intersection location
     */
    public String getErrorMessage() {
        if (this.isValid) {
            return "no intersections found";
        }

        Coordinate[] intSegs = this.segInt.getIntersectionSegments();
        return "found non-noded intersection between "
                + WKTWriter.toLineString(intSegs[0], intSegs[1])
                + " and "
                + WKTWriter.toLineString(intSegs[2], intSegs[3]);
    }

    /**
     * Checks for an intersection and throws
     * a TopologyException if one is found.
     *
     * @throws TopologyException if an intersection is found
     */
    public void checkValid() {
        this.execute();
        if (!this.isValid) {
            throw new TopologyException(this.getErrorMessage(), this.segInt.getInteriorIntersection());
        }
    }

    private void execute() {
        if (this.segInt != null) {
            return;
        }
        this.checkInteriorIntersections();
    }

    private void checkInteriorIntersections() {
        /**
         * MD - It may even be reliable to simply check whether
         * end segments (of SegmentStrings) have an interior intersection,
         * since noding should have split any true interior intersections already.
         */
        this.isValid = true;
        this.segInt = new InteriorIntersectionFinder(this.li);
        this.segInt.setFindAllIntersections(this.findAllIntersections);
        MCIndexNoder noder = new MCIndexNoder();
        noder.setSegmentIntersector(this.segInt);
        noder.computeNodes(this.segStrings);
        if (this.segInt.hasIntersection()) {
            this.isValid = false;
        }
    }
}
