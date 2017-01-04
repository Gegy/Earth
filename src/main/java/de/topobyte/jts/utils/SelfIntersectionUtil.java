// Copyright 2015 Sebastian Kuerten
//
// This file is part of jts-utils.
//
// jts-utils is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// jts-utils is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with jts-utils. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.jts.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import de.topobyte.jsi.GenericRTree;
import de.topobyte.jsi.GenericSpatialIndex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class provides methods for checking the integrity of LinearRings and for
 * solving integrity problems. Currently only self-intersections are considered
 * and may be repaired.
 *
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SelfIntersectionUtil {

    final static Logger logger = LogManager
            .getLogger(SelfIntersectionUtil.class);

    /**
     * Perform a test, whether the given string is sane. A check for
     * self-intersections will be performed. This method is for testing only and
     * does not return any value. Instead it will print on standard output.
     *
     * @param string the lineString to check.
     * @return
     */
    public static boolean hasSelfIntersections(LineString string) {
        List<LineSegment> segments = new ArrayList<>();
        CoordinateSequence seq = string.getCoordinateSequence();
        for (int i = 0; i < string.getNumPoints() - 1; i++) {
            Coordinate a = seq.getCoordinate(i);
            Coordinate b = seq.getCoordinate(i + 1);
            segments.add(new LineSegment(a.x, a.y, b.x, b.y));
        }

        GenericSpatialIndex<LineSegment> si = new GenericRTree<>(1, 10);
        for (LineSegment line : segments) {
            si.add(Segments.bbox(line), line);
        }

        for (int i = 0; i < segments.size() - 1; i++) {
            LineSegment a = segments.get(i);
            for (LineSegment b : si.intersects(Segments.bbox(a))) {
                if (a.equals(b) || Segments.connected(a, b)) {
                    continue;
                }
                Coordinate intersection = a.intersection(b);
                if (intersection == null) {
                    continue;
                }
                logger.debug("intersection!!!");
                logger.debug(intersection + ": " + a + " " + b);
                return true;
            }
        }

        return false;
    }

    /**
     * Given a LinearRing, possibly containing errors (self-intersections),
     * repair this ring by splitting it up into several rings.
     *
     * @param input the ring to repair.
     * @return a set of possibly more than one repaired rings.
     */
    public static Set<LinearRing> repair(LinearRing input) {
        Set<LinearRing> rings = new HashSet<>();

        // Build a list of line segments of the ring
        List<LineSegment> ringSegs = new ArrayList<>();
        CoordinateSequence seq = input.getCoordinateSequence();
        for (int i = 0; i < input.getNumPoints() - 1; i++) {
            Coordinate a = seq.getCoordinate(i);
            Coordinate b = seq.getCoordinate(i + 1);
            ringSegs.add(new LineSegment(a.x, a.y, b.x, b.y));
        }

        // Begin with repairing the initial ring and recursively repair the
        // results if necessary.
        List<List<LineSegment>> done = new ArrayList<>();
        List<List<LineSegment>> todo = new ArrayList<>();
        todo.add(ringSegs);
        boolean repairedSomething = false;
        while (!todo.isEmpty()) {
            List<LineSegment> segs = todo.remove(todo.size() - 1);
            RepairResult result = repairSegmentRing(segs);
            repairedSomething |= result.isIntersectionFound();
            Set<List<LineSegment>> results = result.getResults();
            if (!result.isIntersectionFound()) {
                done.addAll(results);
            } else {
                todo.addAll(results);
            }
        }

        if (!repairedSomething) {
            rings.add(input);
            return rings;
        }

        // Convert the lists of segments to LinearRing instances
        logger.debug("number of segment rings: " + done.size());
        for (int i = 0; i < done.size(); i++) {
            logger.debug("converting ring # " + i);
            List<LineSegment> segments = done.get(i);
            LinearRing ring = null;
            try {
                ring = ringFromSegments(segments);
                if (ring == null) {
                    logger.debug("no ring could be constructed for "
                            + segments.size() + " segments");
                    continue;
                }
            } catch (IllegalArgumentException e) {
                logger.debug("a broken ring has been constructed for "
                        + segments.size() + " segments");
                continue;
            }
            try {
                if (ring.isValid()) {
                    rings.add(ring);
                } else {
                    logger.debug("invalid ring with: " + ring.getNumPoints()
                            + " points");
                    IsValidOp isValidOp = new IsValidOp(ring);
                    logger.debug("validation error: "
                            + isValidOp.getValidationError());
                }
            } catch (Exception e) {
                logger.debug("catched an unchecked Exception");
                // if anything goes wrong, do not die...
            }
        }

        return rings;
    }

    private static RepairResult repairSegmentRing(List<LineSegment> ringSegs) {
        // Insert the segments into a spatial index
        GenericSpatialIndex<LineSegment> si = new GenericRTree<>(1, 10);
        for (LineSegment line : ringSegs) {
            si.add(Segments.bbox(line), line);
        }

        List<LineSegment> segments = new ArrayList<>(ringSegs.size());
        segments.addAll(ringSegs);
        for (int i = 0; i < segments.size() - 1; i++) {
            LineSegment a = segments.get(i);
            for (LineSegment b : si.intersects(Segments.bbox(a))) {
                if (a.equals(b) || Segments.connected(a, b)) {
                    continue;
                }
                Coordinate intersection = a.intersection(b);
                if (intersection == null) {
                    continue;
                }
                logger.debug("found intersection...");
                Set<List<LineSegment>> rings = ringify(ringSegs, i, a, b);
                return new RepairResult(true, rings);
            }
        }

        Set<List<LineSegment>> rings = new HashSet<>();
        rings.add(ringSegs);
        return new RepairResult(false, rings);
    }

    private static LinearRing ringFromSegments(List<LineSegment> segments) {
        GeometryFactory factory = new GeometryFactory();
        int nSegs = segments.size();
        if (nSegs < 3) {
            return null;
        }
        int len = segments.size() + 1;
        CoordinateSequence seq = factory.getCoordinateSequenceFactory().create(
                len, 2);
        int i = 0;
        for (LineSegment line : segments) {
            seq.setOrdinate(i, 0, line.p0.x);
            seq.setOrdinate(i, 1, line.p0.y);
            i++;
        }
        seq.setOrdinate(i, 0, segments.get(0).p0.x);
        seq.setOrdinate(i, 1, segments.get(0).p0.y);
        return factory.createLinearRing(seq);
    }

    // k is the index of a in 'segments'
    private static Set<List<LineSegment>> ringify(List<LineSegment> segments,
                                                  int k, LineSegment a, LineSegment b) {
        // Collect segments in these lists
        List<LineSegment> one = new ArrayList<>();
        List<LineSegment> two = new ArrayList<>();

        // Reorder segments, so that a is the first one
        List<LineSegment> lines = new ArrayList<>(segments.size());
        for (int i = k; i < segments.size(); i++) {
            lines.add(segments.get(i));
        }
        for (int i = 0; i < k; i++) {
            lines.add(segments.get(i));
        }

        // Compute the intersection of a and b
        Coordinate intersection = a.intersection(b);

        // Add relevant part of a to the first list
        LineSegment start = new LineSegment(intersection, a.p1);
        one.add(start);
        // Add all consecutive segments until we hit b
        int i;
        for (i = 1; i < lines.size(); i++) {
            LineSegment line = lines.get(i);
            if (line == b) {
                break;
            }
            one.add(line);
        }
        // Add the relevant part of b
        LineSegment end = new LineSegment(b.p0, intersection);
        one.add(end);

        // Add the remaining part of b to the second list
        LineSegment start2 = new LineSegment(intersection, b.p1);
        two.add(start2);
        // Add all remaining segments, too
        for (i = i + 1; i < lines.size(); i++) {
            LineSegment line = lines.get(i);
            two.add(line);
        }
        // Add the remaining part of a
        LineSegment end2 = new LineSegment(a.p0, intersection);
        two.add(end2);

        // Return both lists in a set
        Set<List<LineSegment>> rings = new HashSet<>();
        rings.add(one);
        rings.add(two);
        return rings;
    }
}
