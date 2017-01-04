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

package com.vividsolutions.jts.linearref;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * Represents a location along a {@link LineString} or {@link MultiLineString}.
 * The referenced geometry is not maintained within
 * this location, but must be provided for operations which require it.
 * Various methods are provided to manipulate the location value
 * and query the geometry it references.
 */
public class LinearLocation
        implements Comparable {
    /**
     * Gets a location which refers to the end of a linear {@link Geometry}.
     *
     * @param linear the linear geometry
     * @return a new <tt>LinearLocation</tt>
     */
    public static LinearLocation getEndLocation(Geometry linear) {
        // assert: linear is LineString or MultiLineString
        LinearLocation loc = new LinearLocation();
        loc.setToEnd(linear);
        return loc;
    }

    /**
     * Computes the {@link Coordinate} of a point a given fraction
     * along the line segment <tt>(p0, p1)</tt>.
     * If the fraction is greater than 1.0 the last
     * point of the segment is returned.
     * If the fraction is less than or equal to 0.0 the first point
     * of the segment is returned.
     * The Z ordinate is interpolated from the Z-ordinates of the given points,
     * if they are specified.
     *
     * @param p0 the first point of the line segment
     * @param p1 the last point of the line segment
     * @param frac the length to the desired point
     * @return the <tt>Coordinate</tt> of the desired point
     */
    public static Coordinate pointAlongSegmentByFraction(Coordinate p0, Coordinate p1, double frac) {
        if (frac <= 0.0) {
            return p0;
        }
        if (frac >= 1.0) {
            return p1;
        }

        double x = (p1.x - p0.x) * frac + p0.x;
        double y = (p1.y - p0.y) * frac + p0.y;
        // interpolate Z value. If either input Z is NaN, result z will be NaN as well.
        double z = (p1.z - p0.z) * frac + p0.z;
        return new Coordinate(x, y, z);
    }

    private int componentIndex = 0;
    private int segmentIndex = 0;
    private double segmentFraction = 0.0;

    /**
     * Creates a location referring to the start of a linear geometry
     */
    public LinearLocation() {
    }

    public LinearLocation(int segmentIndex, double segmentFraction) {
        this(0, segmentIndex, segmentFraction);
    }

    public LinearLocation(int componentIndex, int segmentIndex, double segmentFraction) {
        this.componentIndex = componentIndex;
        this.segmentIndex = segmentIndex;
        this.segmentFraction = segmentFraction;
        this.normalize();
    }

    /**
     * Creates a new location equal to a given one.
     *
     * @param loc a LinearLocation
     */
    public LinearLocation(LinearLocation loc) {
        this.componentIndex = loc.componentIndex;
        this.segmentIndex = loc.segmentIndex;
        this.segmentFraction = loc.segmentFraction;
    }

    /**
     * Ensures the individual values are locally valid.
     * Does <b>not</b> ensure that the indexes are valid for
     * a particular linear geometry.
     *
     * @see clamp
     */
    private void normalize() {
        if (this.segmentFraction < 0.0) {
            this.segmentFraction = 0.0;
        }
        if (this.segmentFraction > 1.0) {
            this.segmentFraction = 1.0;
        }

        if (this.componentIndex < 0) {
            this.componentIndex = 0;
            this.segmentIndex = 0;
            this.segmentFraction = 0.0;
        }
        if (this.segmentIndex < 0) {
            this.segmentIndex = 0;
            this.segmentFraction = 0.0;
        }
        if (this.segmentFraction == 1.0) {
            this.segmentFraction = 0.0;
            this.segmentIndex += 1;
        }
    }

    /**
     * Ensures the indexes are valid for a given linear {@link Geometry}.
     *
     * @param linear a linear geometry
     */
    public void clamp(Geometry linear) {
        if (this.componentIndex >= linear.getNumGeometries()) {
            this.setToEnd(linear);
            return;
        }
        if (this.segmentIndex >= linear.getNumPoints()) {
            LineString line = (LineString) linear.getGeometryN(this.componentIndex);
            this.segmentIndex = line.getNumPoints() - 1;
            this.segmentFraction = 1.0;
        }
    }

    /**
     * Snaps the value of this location to
     * the nearest vertex on the given linear {@link Geometry},
     * if the vertex is closer than <tt>minDistance</tt>.
     *
     * @param linearGeom a linear geometry
     * @param minDistance the minimum allowable distance to a vertex
     */
    public void snapToVertex(Geometry linearGeom, double minDistance) {
        if (this.segmentFraction <= 0.0 || this.segmentFraction >= 1.0) {
            return;
        }
        double segLen = this.getSegmentLength(linearGeom);
        double lenToStart = this.segmentFraction * segLen;
        double lenToEnd = segLen - lenToStart;
        if (lenToStart <= lenToEnd && lenToStart < minDistance) {
            this.segmentFraction = 0.0;
        } else if (lenToEnd <= lenToStart && lenToEnd < minDistance) {
            this.segmentFraction = 1.0;
        }
    }

    /**
     * Gets the length of the segment in the given
     * Geometry containing this location.
     *
     * @param linearGeom a linear geometry
     * @return the length of the segment
     */
    public double getSegmentLength(Geometry linearGeom) {
        LineString lineComp = (LineString) linearGeom.getGeometryN(this.componentIndex);

        // ensure segment index is valid
        int segIndex = this.segmentIndex;
        if (this.segmentIndex >= lineComp.getNumPoints() - 1) {
            segIndex = lineComp.getNumPoints() - 2;
        }

        Coordinate p0 = lineComp.getCoordinateN(segIndex);
        Coordinate p1 = lineComp.getCoordinateN(segIndex + 1);
        return p0.distance(p1);
    }

    /**
     * Sets the value of this location to
     * refer to the end of a linear geometry.
     *
     * @param linear the linear geometry to use to set the end
     */
    public void setToEnd(Geometry linear) {
        this.componentIndex = linear.getNumGeometries() - 1;
        LineString lastLine = (LineString) linear.getGeometryN(this.componentIndex);
        this.segmentIndex = lastLine.getNumPoints() - 1;
        this.segmentFraction = 1.0;
    }

    /**
     * Gets the component index for this location.
     *
     * @return the component index
     */
    public int getComponentIndex() {
        return this.componentIndex;
    }

    /**
     * Gets the segment index for this location
     *
     * @return the segment index
     */
    public int getSegmentIndex() {
        return this.segmentIndex;
    }

    /**
     * Gets the segment fraction for this location
     *
     * @return the segment fraction
     */
    public double getSegmentFraction() {
        return this.segmentFraction;
    }

    /**
     * Tests whether this location refers to a vertex
     *
     * @return true if the location is a vertex
     */
    public boolean isVertex() {
        return this.segmentFraction <= 0.0 || this.segmentFraction >= 1.0;
    }

    /**
     * Gets the {@link Coordinate} along the
     * given linear {@link Geometry} which is
     * referenced by this location.
     *
     * @param linearGeom the linear geometry referenced by this location
     * @return the <tt>Coordinate</tt> at the location
     */
    public Coordinate getCoordinate(Geometry linearGeom) {
        LineString lineComp = (LineString) linearGeom.getGeometryN(this.componentIndex);
        Coordinate p0 = lineComp.getCoordinateN(this.segmentIndex);
        if (this.segmentIndex >= lineComp.getNumPoints() - 1) {
            return p0;
        }
        Coordinate p1 = lineComp.getCoordinateN(this.segmentIndex + 1);
        return pointAlongSegmentByFraction(p0, p1, this.segmentFraction);
    }

    /**
     * Gets a {@link LineSegment} representing the segment of the
     * given linear {@link Geometry} which contains this location.
     *
     * @param linearGeom a linear geometry
     * @return the <tt>LineSegment</tt> containing the location
     */
    public LineSegment getSegment(Geometry linearGeom) {
        LineString lineComp = (LineString) linearGeom.getGeometryN(this.componentIndex);
        Coordinate p0 = lineComp.getCoordinateN(this.segmentIndex);
        // check for endpoint - return last segment of the line if so
        if (this.segmentIndex >= lineComp.getNumPoints() - 1) {
            Coordinate prev = lineComp.getCoordinateN(lineComp.getNumPoints() - 2);
            return new LineSegment(prev, p0);
        }
        Coordinate p1 = lineComp.getCoordinateN(this.segmentIndex + 1);
        return new LineSegment(p0, p1);
    }

    /**
     * Tests whether this location refers to a valid
     * location on the given linear {@link Geometry}.
     *
     * @param linearGeom a linear geometry
     * @return true if this location is valid
     */
    public boolean isValid(Geometry linearGeom) {
        if (this.componentIndex < 0 || this.componentIndex >= linearGeom.getNumGeometries()) {
            return false;
        }

        LineString lineComp = (LineString) linearGeom.getGeometryN(this.componentIndex);
        if (this.segmentIndex < 0 || this.segmentIndex > lineComp.getNumPoints()) {
            return false;
        }
        if (this.segmentIndex == lineComp.getNumPoints() && this.segmentFraction != 0.0) {
            return false;
        }

        return !(segmentFraction < 0.0 || segmentFraction > 1.0);
    }

    /**
     * Compares this object with the specified object for order.
     *
     * @param o the <code>LineStringLocation</code> with which this <code>Coordinate</code>
     * is being compared
     * @return a negative integer, zero, or a positive integer as this <code>LineStringLocation</code>
     * is less than, equal to, or greater than the specified <code>LineStringLocation</code>
     */
    @Override
    public int compareTo(Object o) {
        LinearLocation other = (LinearLocation) o;
        // compare component indices
        if (this.componentIndex < other.componentIndex) {
            return -1;
        }
        if (this.componentIndex > other.componentIndex) {
            return 1;
        }
        // compare segments
        if (this.segmentIndex < other.segmentIndex) {
            return -1;
        }
        if (this.segmentIndex > other.segmentIndex) {
            return 1;
        }
        // same segment, so compare segment fraction
        if (this.segmentFraction < other.segmentFraction) {
            return -1;
        }
        if (this.segmentFraction > other.segmentFraction) {
            return 1;
        }
        // same location
        return 0;
    }

    /**
     * Compares this object with the specified index values for order.
     *
     * @param componentIndex1 a component index
     * @param segmentIndex1 a segment index
     * @param segmentFraction1 a segment fraction
     * @return a negative integer, zero, or a positive integer as this <code>LineStringLocation</code>
     * is less than, equal to, or greater than the specified locationValues
     */
    public int compareLocationValues(int componentIndex1, int segmentIndex1, double segmentFraction1) {
        // compare component indices
        if (this.componentIndex < componentIndex1) {
            return -1;
        }
        if (this.componentIndex > componentIndex1) {
            return 1;
        }
        // compare segments
        if (this.segmentIndex < segmentIndex1) {
            return -1;
        }
        if (this.segmentIndex > segmentIndex1) {
            return 1;
        }
        // same segment, so compare segment fraction
        if (this.segmentFraction < segmentFraction1) {
            return -1;
        }
        if (this.segmentFraction > segmentFraction1) {
            return 1;
        }
        // same location
        return 0;
    }

    /**
     * Compares two sets of location values for order.
     *
     * @param componentIndex0 a component index
     * @param segmentIndex0 a segment index
     * @param segmentFraction0 a segment fraction
     * @param componentIndex1 another component index
     * @param segmentIndex1 another segment index
     * @param segmentFraction1 another segment fraction
     * @return a negative integer, zero, or a positive integer
     * as the first set of location values
     * is less than, equal to, or greater than the second set of locationValues
     */
    public static int compareLocationValues(
            int componentIndex0, int segmentIndex0, double segmentFraction0,
            int componentIndex1, int segmentIndex1, double segmentFraction1) {
        // compare component indices
        if (componentIndex0 < componentIndex1) {
            return -1;
        }
        if (componentIndex0 > componentIndex1) {
            return 1;
        }
        // compare segments
        if (segmentIndex0 < segmentIndex1) {
            return -1;
        }
        if (segmentIndex0 > segmentIndex1) {
            return 1;
        }
        // same segment, so compare segment fraction
        if (segmentFraction0 < segmentFraction1) {
            return -1;
        }
        if (segmentFraction0 > segmentFraction1) {
            return 1;
        }
        // same location
        return 0;
    }

    /**
     * Tests whether two locations
     * are on the same segment in the parent {@link Geometry}.
     *
     * @param loc a location on the same geometry
     * @return true if the locations are on the same segment of the parent geometry
     */
    public boolean isOnSameSegment(LinearLocation loc) {
        if (this.componentIndex != loc.componentIndex) {
            return false;
        }
        if (this.segmentIndex == loc.segmentIndex) {
            return true;
        }
        if (loc.segmentIndex - this.segmentIndex == 1
                && loc.segmentFraction == 0.0) {
            return true;
        }
        return segmentIndex - loc.segmentIndex == 1
                && segmentFraction == 0.0;
    }

    /**
     * Tests whether this location is an endpoint of
     * the linear component it refers to.
     *
     * @param linearGeom the linear geometry referenced by this location
     * @return true if the location is a component endpoint
     */
    public boolean isEndpoint(Geometry linearGeom) {
        LineString lineComp = (LineString) linearGeom.getGeometryN(this.componentIndex);
        // check for endpoint
        int nseg = lineComp.getNumPoints() - 1;
        return this.segmentIndex >= nseg
                || (this.segmentIndex == nseg && this.segmentFraction >= 1.0);
    }

    /**
     * Copies this location
     *
     * @return a copy of this location
     */
    @Override
    public Object clone() {
        return new LinearLocation(this.componentIndex, this.segmentIndex, this.segmentFraction);
    }

    public String toString() {
        return "LinearLoc["
                + this.componentIndex + ", "
                + this.segmentIndex + ", "
                + this.segmentFraction + "]";
    }
}
