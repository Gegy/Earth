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
package com.vividsolutions.jts.operation.buffer.validate;

import com.vividsolutions.jts.algorithm.distance.DiscreteHausdorffDistance;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
import com.vividsolutions.jts.geom.util.PolygonExtracter;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that a given buffer curve lies an appropriate distance
 * from the input generating it.
 * Useful only for round buffers (cap and join).
 * Can be used for either positive or negative distances.
 * <p>
 * This is a heuristic test, and may return false positive results
 * (I.e. it may fail to detect an invalid result.)
 * It should never return a false negative result, however
 * (I.e. it should never report a valid result as invalid.)
 *
 * @author mbdavis
 */
public class BufferDistanceValidator {
    private static boolean VERBOSE = false;
    /**
     * Maximum allowable fraction of buffer distance the
     * actual distance can differ by.
     * 1% sometimes causes an error - 1.2% should be safe.
     */
    private static final double MAX_DISTANCE_DIFF_FRAC = .012;

    private Geometry input;
    private double bufDistance;
    private Geometry result;

    private double minValidDistance;
    private double maxValidDistance;

    private double minDistanceFound;
    private double maxDistanceFound;

    private boolean isValid = true;
    private String errMsg = null;
    private Coordinate errorLocation = null;
    private Geometry errorIndicator = null;

    public BufferDistanceValidator(Geometry input, double bufDistance, Geometry result) {
        this.input = input;
        this.bufDistance = bufDistance;
        this.result = result;
    }

    public boolean isValid() {
        double posDistance = Math.abs(this.bufDistance);
        double distDelta = MAX_DISTANCE_DIFF_FRAC * posDistance;
        this.minValidDistance = posDistance - distDelta;
        this.maxValidDistance = posDistance + distDelta;

        // can't use this test if either is empty
        if (this.input.isEmpty() || this.result.isEmpty()) {
            return true;
        }

        if (this.bufDistance > 0.0) {
            this.checkPositiveValid();
        } else {
            this.checkNegativeValid();
        }
        if (VERBOSE) {
            System.out.println("Min Dist= " + this.minDistanceFound + "  err= "
                    + (1.0 - this.minDistanceFound / this.bufDistance)
                    + "  Max Dist= " + this.maxDistanceFound + "  err= "
                    + (this.maxDistanceFound / this.bufDistance - 1.0)
            );
        }
        return this.isValid;
    }

    public String getErrorMessage() {
        return this.errMsg;
    }

    public Coordinate getErrorLocation() {
        return this.errorLocation;
    }

    /**
     * Gets a geometry which indicates the location and nature of a validation failure.
     * <p>
     * The indicator is a line segment showing the location and size
     * of the distance discrepancy.
     *
     * @return a geometric error indicator
     * or null if no error was found
     */
    public Geometry getErrorIndicator() {
        return this.errorIndicator;
    }

    private void checkPositiveValid() {
        Geometry bufCurve = this.result.getBoundary();
        this.checkMinimumDistance(this.input, bufCurve, this.minValidDistance);
        if (!this.isValid) {
            return;
        }

        this.checkMaximumDistance(this.input, bufCurve, this.maxValidDistance);
    }

    private void checkNegativeValid() {
        // Assert: only polygonal inputs can be checked for negative buffers

        // MD - could generalize this to handle GCs too
        if (!(this.input instanceof Polygon
                || this.input instanceof MultiPolygon
                || this.input instanceof GeometryCollection
        )) {
            return;
        }
        Geometry inputCurve = this.getPolygonLines(this.input);
        this.checkMinimumDistance(inputCurve, this.result, this.minValidDistance);
        if (!this.isValid) {
            return;
        }

        this.checkMaximumDistance(inputCurve, this.result, this.maxValidDistance);
    }

    private Geometry getPolygonLines(Geometry g) {
        List lines = new ArrayList();
        LinearComponentExtracter lineExtracter = new LinearComponentExtracter(lines);
        List polys = PolygonExtracter.getPolygons(g);
        for (Object poly1 : polys) {
            Polygon poly = (Polygon) poly1;
            poly.apply(lineExtracter);
        }
        return g.getFactory().buildGeometry(lines);
    }

    /**
     * Checks that two geometries are at least a minumum distance apart.
     *
     * @param g1 a geometry
     * @param g2 a geometry
     * @param minDist the minimum distance the geometries should be separated by
     */
    private void checkMinimumDistance(Geometry g1, Geometry g2, double minDist) {
        DistanceOp distOp = new DistanceOp(g1, g2, minDist);
        this.minDistanceFound = distOp.distance();

        if (this.minDistanceFound < minDist) {
            this.isValid = false;
            Coordinate[] pts = distOp.nearestPoints();
            this.errorLocation = distOp.nearestPoints()[1];
            this.errorIndicator = g1.getFactory().createLineString(pts);
            this.errMsg = "Distance between buffer curve and input is too small "
                    + "(" + this.minDistanceFound
                    + " at " + WKTWriter.toLineString(pts[0], pts[1]) + " )";
        }
    }

    /**
     * Checks that the furthest distance from the buffer curve to the input
     * is less than the given maximum distance.
     * This uses the Oriented Hausdorff distance metric.
     * It corresponds to finding
     * the point on the buffer curve which is furthest from <i>some</i> point on the input.
     *
     * @param input a geometry
     * @param bufCurve a geometry
     * @param maxDist the maximum distance that a buffer result can be from the input
     */
    private void checkMaximumDistance(Geometry input, Geometry bufCurve, double maxDist) {
//    BufferCurveMaximumDistanceFinder maxDistFinder = new BufferCurveMaximumDistanceFinder(input);
//    maxDistanceFound = maxDistFinder.findDistance(bufCurve);

        DiscreteHausdorffDistance haus = new DiscreteHausdorffDistance(bufCurve, input);
        haus.setDensifyFraction(0.25);
        this.maxDistanceFound = haus.orientedDistance();

        if (this.maxDistanceFound > maxDist) {
            this.isValid = false;
            Coordinate[] pts = haus.getCoordinates();
            this.errorLocation = pts[1];
            this.errorIndicator = input.getFactory().createLineString(pts);
            this.errMsg = "Distance between buffer curve and input is too large "
                    + "(" + this.maxDistanceFound
                    + " at " + WKTWriter.toLineString(pts[0], pts[1]) + ")";
        }
    }
  
  /*
  private void OLDcheckMaximumDistance(Geometry input, Geometry bufCurve, double maxDist)
  {
    BufferCurveMaximumDistanceFinder maxDistFinder = new BufferCurveMaximumDistanceFinder(input);
    maxDistanceFound = maxDistFinder.findDistance(bufCurve);
    
    
    if (maxDistanceFound > maxDist) {
      isValid = false;
      PointPairDistance ptPairDist = maxDistFinder.getDistancePoints();
      errorLocation = ptPairDist.getCoordinate(1);
      errMsg = "Distance between buffer curve and input is too large "
        + "(" + ptPairDist.getDistance()
        + " at " + ptPairDist.toString() +")";
    }
  }
  */
}
