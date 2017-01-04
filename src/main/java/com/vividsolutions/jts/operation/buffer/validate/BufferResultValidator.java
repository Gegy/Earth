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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Validates that the result of a buffer operation
 * is geometrically correct, within a computed tolerance.
 * <p>
 * This is a heuristic test, and may return false positive results
 * (I.e. it may fail to detect an invalid result.)
 * It should never return a false negative result, however
 * (I.e. it should never report a valid result as invalid.)
 * <p>
 * This test may be (much) more expensive than the original
 * buffer computation.
 *
 * @author Martin Davis
 */
public class BufferResultValidator {
    private static boolean VERBOSE = false;

    /**
     * Maximum allowable fraction of buffer distance the
     * actual distance can differ by.
     * 1% sometimes causes an error - 1.2% should be safe.
     */
    private static final double MAX_ENV_DIFF_FRAC = .012;

    public static boolean isValid(Geometry g, double distance, Geometry result) {
        BufferResultValidator validator = new BufferResultValidator(g, distance, result);
        return validator.isValid();
    }

    /**
     * Checks whether the geometry buffer is valid,
     * and returns an error message if not.
     *
     * @param g
     * @param distance
     * @param result
     * @return an appropriate error message
     * or null if the buffer is valid
     */
    public static String isValidMsg(Geometry g, double distance, Geometry result) {
        BufferResultValidator validator = new BufferResultValidator(g, distance, result);
        if (!validator.isValid()) {
            return validator.getErrorMessage();
        }
        return null;
    }

    private Geometry input;
    private double distance;
    private Geometry result;
    private boolean isValid = true;
    private String errorMsg = null;
    private Coordinate errorLocation = null;
    private Geometry errorIndicator = null;

    public BufferResultValidator(Geometry input, double distance, Geometry result) {
        this.input = input;
        this.distance = distance;
        this.result = result;
    }

    public boolean isValid() {
        this.checkPolygonal();
        if (!this.isValid) {
            return this.isValid;
        }
        this.checkExpectedEmpty();
        if (!this.isValid) {
            return this.isValid;
        }
        this.checkEnvelope();
        if (!this.isValid) {
            return this.isValid;
        }
        this.checkArea();
        if (!this.isValid) {
            return this.isValid;
        }
        this.checkDistance();
        return this.isValid;
    }

    public String getErrorMessage() {
        return this.errorMsg;
    }

    public Coordinate getErrorLocation() {
        return this.errorLocation;
    }

    /**
     * Gets a geometry which indicates the location and nature of a validation failure.
     * <p>
     * If the failure is due to the buffer curve being too far or too close
     * to the input, the indicator is a line segment showing the location and size
     * of the discrepancy.
     *
     * @return a geometric error indicator
     * or null if no error was found
     */
    public Geometry getErrorIndicator() {
        return this.errorIndicator;
    }

    private void report(String checkName) {
        if (!VERBOSE) {
            return;
        }
        System.out.println("Check " + checkName + ": "
                + (this.isValid ? "passed" : "FAILED"));
    }

    private void checkPolygonal() {
        if (!(this.result instanceof Polygon
                || this.result instanceof MultiPolygon)) {
            this.isValid = false;
        }
        this.errorMsg = "Result is not polygonal";
        this.errorIndicator = this.result;
        this.report("Polygonal");
    }

    private void checkExpectedEmpty() {
        // can't check areal features
        if (this.input.getDimension() >= 2) {
            return;
        }
        // can't check positive distances
        if (this.distance > 0.0) {
            return;
        }

        // at this point can expect an empty result
        if (!this.result.isEmpty()) {
            this.isValid = false;
            this.errorMsg = "Result is non-empty";
            this.errorIndicator = this.result;
        }
        this.report("ExpectedEmpty");
    }

    private void checkEnvelope() {
        if (this.distance < 0.0) {
            return;
        }

        double padding = this.distance * MAX_ENV_DIFF_FRAC;
        if (padding == 0.0) {
            padding = 0.001;
        }

        Envelope expectedEnv = new Envelope(this.input.getEnvelopeInternal());
        expectedEnv.expandBy(this.distance);

        Envelope bufEnv = new Envelope(this.result.getEnvelopeInternal());
        bufEnv.expandBy(padding);

        if (!bufEnv.contains(expectedEnv)) {
            this.isValid = false;
            this.errorMsg = "Buffer envelope is incorrect";
            this.errorIndicator = this.input.getFactory().toGeometry(bufEnv);
        }
        this.report("Envelope");
    }

    private void checkArea() {
        double inputArea = this.input.getArea();
        double resultArea = this.result.getArea();

        if (this.distance > 0.0
                && inputArea > resultArea) {
            this.isValid = false;
            this.errorMsg = "Area of positive buffer is smaller than input";
            this.errorIndicator = this.result;
        }
        if (this.distance < 0.0
                && inputArea < resultArea) {
            this.isValid = false;
            this.errorMsg = "Area of negative buffer is larger than input";
            this.errorIndicator = this.result;
        }
        this.report("Area");
    }

    private void checkDistance() {
        BufferDistanceValidator distValid = new BufferDistanceValidator(this.input, this.distance, this.result);
        if (!distValid.isValid()) {
            this.isValid = false;
            this.errorMsg = distValid.getErrorMessage();
            this.errorLocation = distValid.getErrorLocation();
            this.errorIndicator = distValid.getErrorIndicator();
        }
        this.report("Distance");
    }
}
