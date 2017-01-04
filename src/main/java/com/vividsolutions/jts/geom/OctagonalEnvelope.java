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
package com.vividsolutions.jts.geom;

/**
 * A Bounding Container which is in the shape of an octagon.
 * The OctagonalEnvelope of a geometric object
 * is tight along the four extremal rectilineal parallels
 * and along the four extremal diagonal parallels.
 * Depending on the shape of the contained
 * geometry, the octagon may be degenerate to any extreme
 * (e.g. it may be a rectangle, a line, or a point).
 */
public class OctagonalEnvelope {
    private static double computeA(double x, double y) {
        return x + y;
    }

    private static double computeB(double x, double y) {
        return x - y;
    }

    private static double SQRT2 = Math.sqrt(2.0);

    // initialize in the null state
    private double minX = Double.NaN;
    private double maxX;
    private double minY;
    private double maxY;
    private double minA;
    private double maxA;
    private double minB;
    private double maxB;

    /**
     * Creates a new null bounding octagon
     */
    public OctagonalEnvelope() {
    }

    /**
     * Creates a new null bounding octagon bounding a {@link Coordinate}
     */
    public OctagonalEnvelope(Coordinate p) {
        this.expandToInclude(p);
    }

    /**
     * Creates a new null bounding octagon bounding a pair of {@link Coordinate}s
     */
    public OctagonalEnvelope(Coordinate p0, Coordinate p1) {
        this.expandToInclude(p0);
        this.expandToInclude(p1);
    }

    /**
     * Creates a new null bounding octagon bounding an {@link Envelope}
     */
    public OctagonalEnvelope(Envelope env) {
        this.expandToInclude(env);
    }

    /**
     * Creates a new null bounding octagon bounding an {@link OctagonalEnvelope}
     * (the copy constructor).
     */
    public OctagonalEnvelope(OctagonalEnvelope oct) {
        this.expandToInclude(oct);
    }

    /**
     * Creates a new null bounding octagon bounding a {@link Geometry}
     */
    public OctagonalEnvelope(Geometry geom) {
        this.expandToInclude(geom);
    }

    public double getMinX() {
        return this.minX;
    }

    public double getMaxX() {
        return this.maxX;
    }

    public double getMinY() {
        return this.minY;
    }

    public double getMaxY() {
        return this.maxY;
    }

    public double getMinA() {
        return this.minA;
    }

    public double getMaxA() {
        return this.maxA;
    }

    public double getMinB() {
        return this.minB;
    }

    public double getMaxB() {
        return this.maxB;
    }

    public boolean isNull() {
        return Double.isNaN(this.minX);
    }

    /**
     * Sets the value of this object to the null value
     */
    public void setToNull() {
        this.minX = Double.NaN;
    }

    public void expandToInclude(Geometry g) {
        g.apply(new BoundingOctagonComponentFilter());
    }

    public OctagonalEnvelope expandToInclude(CoordinateSequence seq) {
        for (int i = 0; i < seq.size(); i++) {
            double x = seq.getX(i);
            double y = seq.getY(i);
            this.expandToInclude(x, y);
        }
        return this;
    }

    public OctagonalEnvelope expandToInclude(OctagonalEnvelope oct) {
        if (oct.isNull()) {
            return this;
        }

        if (this.isNull()) {
            this.minX = oct.minX;
            this.maxX = oct.maxX;
            this.minY = oct.minY;
            this.maxY = oct.maxY;
            this.minA = oct.minA;
            this.maxA = oct.maxA;
            this.minB = oct.minB;
            this.maxB = oct.maxB;
            return this;
        }
        if (oct.minX < this.minX) {
            this.minX = oct.minX;
        }
        if (oct.maxX > this.maxX) {
            this.maxX = oct.maxX;
        }
        if (oct.minY < this.minY) {
            this.minY = oct.minY;
        }
        if (oct.maxY > this.maxY) {
            this.maxY = oct.maxY;
        }
        if (oct.minA < this.minA) {
            this.minA = oct.minA;
        }
        if (oct.maxA > this.maxA) {
            this.maxA = oct.maxA;
        }
        if (oct.minB < this.minB) {
            this.minB = oct.minB;
        }
        if (oct.maxB > this.maxB) {
            this.maxB = oct.maxB;
        }
        return this;
    }

    public OctagonalEnvelope expandToInclude(Coordinate p) {
        this.expandToInclude(p.x, p.y);
        return this;
    }

    public OctagonalEnvelope expandToInclude(Envelope env) {
        this.expandToInclude(env.getMinX(), env.getMinY());
        this.expandToInclude(env.getMinX(), env.getMaxY());
        this.expandToInclude(env.getMaxX(), env.getMinY());
        this.expandToInclude(env.getMaxX(), env.getMaxY());
        return this;
    }

    public OctagonalEnvelope expandToInclude(double x, double y) {
        double A = computeA(x, y);
        double B = computeB(x, y);

        if (this.isNull()) {
            this.minX = x;
            this.maxX = x;
            this.minY = y;
            this.maxY = y;
            this.minA = A;
            this.maxA = A;
            this.minB = B;
            this.maxB = B;
        } else {
            if (x < this.minX) {
                this.minX = x;
            }
            if (x > this.maxX) {
                this.maxX = x;
            }
            if (y < this.minY) {
                this.minY = y;
            }
            if (y > this.maxY) {
                this.maxY = y;
            }
            if (A < this.minA) {
                this.minA = A;
            }
            if (A > this.maxA) {
                this.maxA = A;
            }
            if (B < this.minB) {
                this.minB = B;
            }
            if (B > this.maxB) {
                this.maxB = B;
            }
        }
        return this;
    }

    public void expandBy(double distance) {
        if (this.isNull()) {
            return;
        }

        double diagonalDistance = SQRT2 * distance;

        this.minX -= distance;
        this.maxX += distance;
        this.minY -= distance;
        this.maxY += distance;
        this.minA -= diagonalDistance;
        this.maxA += diagonalDistance;
        this.minB -= diagonalDistance;
        this.maxB += diagonalDistance;

        if (!this.isValid()) {
            this.setToNull();
        }
    }

    /**
     * Tests if the extremal values for this octagon are valid.
     *
     * @return <code>true</code> if this object has valid values
     */
    private boolean isValid() {
        if (this.isNull()) {
            return true;
        }
        return this.minX <= this.maxX
                && this.minY <= this.maxY
                && this.minA <= this.maxA
                && this.minB <= this.maxB;
    }

    public boolean intersects(OctagonalEnvelope other) {
        if (this.isNull() || other.isNull()) {
            return false;
        }

        if (this.minX > other.maxX) {
            return false;
        }
        if (this.maxX < other.minX) {
            return false;
        }
        if (this.minY > other.maxY) {
            return false;
        }
        if (this.maxY < other.minY) {
            return false;
        }
        if (this.minA > other.maxA) {
            return false;
        }
        if (this.maxA < other.minA) {
            return false;
        }
        if (this.minB > other.maxB) {
            return false;
        }
        return !(maxB < other.minB);
    }

    public boolean intersects(Coordinate p) {
        if (this.minX > p.x) {
            return false;
        }
        if (this.maxX < p.x) {
            return false;
        }
        if (this.minY > p.y) {
            return false;
        }
        if (this.maxY < p.y) {
            return false;
        }

        double A = computeA(p.x, p.y);
        double B = computeB(p.x, p.y);
        if (this.minA > A) {
            return false;
        }
        if (this.maxA < A) {
            return false;
        }
        if (this.minB > B) {
            return false;
        }
        return !(maxB < B);
    }

    public boolean contains(OctagonalEnvelope other) {
        if (this.isNull() || other.isNull()) {
            return false;
        }

        return other.minX >= this.minX
                && other.maxX <= this.maxX
                && other.minY >= this.minY
                && other.maxY <= this.maxY
                && other.minA >= this.minA
                && other.maxA <= this.maxA
                && other.minB >= this.minB
                && other.maxB <= this.maxB;
    }

    public Geometry toGeometry(GeometryFactory geomFactory) {
        if (this.isNull()) {
            return geomFactory.createPoint((CoordinateSequence) null);
        }

        Coordinate px00 = new Coordinate(this.minX, this.minA - this.minX);
        Coordinate px01 = new Coordinate(this.minX, this.minX - this.minB);

        Coordinate px10 = new Coordinate(this.maxX, this.maxX - this.maxB);
        Coordinate px11 = new Coordinate(this.maxX, this.maxA - this.maxX);

        Coordinate py00 = new Coordinate(this.minA - this.minY, this.minY);
        Coordinate py01 = new Coordinate(this.minY + this.maxB, this.minY);

        Coordinate py10 = new Coordinate(this.maxY + this.minB, this.maxY);
        Coordinate py11 = new Coordinate(this.maxA - this.maxY, this.maxY);

        PrecisionModel pm = geomFactory.getPrecisionModel();
        pm.makePrecise(px00);
        pm.makePrecise(px01);
        pm.makePrecise(px10);
        pm.makePrecise(px11);
        pm.makePrecise(py00);
        pm.makePrecise(py01);
        pm.makePrecise(py10);
        pm.makePrecise(py11);

        CoordinateList coordList = new CoordinateList();
        coordList.add(px00, false);
        coordList.add(px01, false);
        coordList.add(py10, false);
        coordList.add(py11, false);
        coordList.add(px11, false);
        coordList.add(px10, false);
        coordList.add(py01, false);
        coordList.add(py00, false);

        if (coordList.size() == 1) {
            return geomFactory.createPoint(px00);
        }
        if (coordList.size() == 2) {
            Coordinate[] pts = coordList.toCoordinateArray();
            return geomFactory.createLineString(pts);
        }
        // must be a polygon, so add closing point
        coordList.add(px00, false);
        Coordinate[] pts = coordList.toCoordinateArray();
        return geomFactory.createPolygon(geomFactory.createLinearRing(pts), null);
    }

    private class BoundingOctagonComponentFilter
            implements GeometryComponentFilter {
        @Override
        public void filter(Geometry geom) {
            if (geom instanceof LineString) {
                OctagonalEnvelope.this.expandToInclude(((LineString) geom).getCoordinateSequence());
            } else if (geom instanceof Point) {
                OctagonalEnvelope.this.expandToInclude(((Point) geom).getCoordinateSequence());
            }
        }
    }
}
