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

package com.vividsolutions.jts.shape;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;

public abstract class GeometricShapeBuilder {
    protected Envelope extent = new Envelope(0, 1, 0, 1);
    protected int numPts = 0;
    protected GeometryFactory geomFactory;

    public GeometricShapeBuilder(GeometryFactory geomFactory) {
        this.geomFactory = geomFactory;
    }

    public void setExtent(Envelope extent) {
        this.extent = extent;
    }

    public Envelope getExtent() {
        return this.extent;
    }

    public Coordinate getCentre() {
        return this.extent.centre();
    }

    public double getDiameter() {
        return Math.min(this.extent.getHeight(), this.extent.getWidth());
    }

    public double getRadius() {
        return this.getDiameter() / 2;
    }

    public LineSegment getSquareBaseLine() {
        double radius = this.getRadius();

        Coordinate centre = this.getCentre();
        Coordinate p0 = new Coordinate(centre.x - radius, centre.y - radius);
        Coordinate p1 = new Coordinate(centre.x + radius, centre.y - radius);
        return new LineSegment(p0, p1);
    }

    public Envelope getSquareExtent() {
        double radius = this.getRadius();

        Coordinate centre = this.getCentre();
        return new Envelope(centre.x - radius, centre.x + radius,
                centre.y - radius, centre.y + radius);
    }

    /**
     * Sets the total number of points in the created {@link Geometry}.
     * The created geometry will have no more than this number of points,
     * unless more are needed to create a valid geometry.
     */
    public void setNumPoints(int numPts) {
        this.numPts = numPts;
    }

    public abstract Geometry getGeometry();

    protected Coordinate createCoord(double x, double y) {
        Coordinate pt = new Coordinate(x, y);
        this.geomFactory.getPrecisionModel().makePrecise(pt);
        return pt;
    }
}
