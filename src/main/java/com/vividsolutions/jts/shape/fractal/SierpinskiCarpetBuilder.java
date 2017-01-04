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

package com.vividsolutions.jts.shape.fractal;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.shape.GeometricShapeBuilder;

import java.util.ArrayList;
import java.util.List;

public class SierpinskiCarpetBuilder
        extends GeometricShapeBuilder {
    private CoordinateList coordList = new CoordinateList();

    public SierpinskiCarpetBuilder(GeometryFactory geomFactory) {
        super(geomFactory);
    }

    public static int recursionLevelForSize(int numPts) {
        double pow4 = numPts / 3;
        double exp = Math.log(pow4) / Math.log(4);
        return (int) exp;
    }

    @Override
    public Geometry getGeometry() {
        int level = recursionLevelForSize(this.numPts);
        LineSegment baseLine = this.getSquareBaseLine();
        Coordinate origin = baseLine.getCoordinate(0);
        LinearRing[] holes = this.getHoles(level, origin.x, origin.y, this.getDiameter());
        LinearRing shell = (LinearRing) ((Polygon) this.geomFactory.toGeometry(this.getSquareExtent())).getExteriorRing();
        return this.geomFactory.createPolygon(
                shell, holes);
    }

    private LinearRing[] getHoles(int n, double originX, double originY, double width) {
        List holeList = new ArrayList();

        this.addHoles(n, originX, originY, width, holeList);

        return GeometryFactory.toLinearRingArray(holeList);
    }

    private void addHoles(int n, double originX, double originY, double width, List holeList) {
        if (n < 0) {
            return;
        }
        int n2 = n - 1;
        double widthThird = width / 3.0;
        double widthTwoThirds = width * 2.0 / 3.0;
        double widthNinth = width / 9.0;
        this.addHoles(n2, originX, originY, widthThird, holeList);
        this.addHoles(n2, originX + widthThird, originY, widthThird, holeList);
        this.addHoles(n2, originX + 2 * widthThird, originY, widthThird, holeList);

        this.addHoles(n2, originX, originY + widthThird, widthThird, holeList);
        this.addHoles(n2, originX + 2 * widthThird, originY + widthThird, widthThird, holeList);

        this.addHoles(n2, originX, originY + 2 * widthThird, widthThird, holeList);
        this.addHoles(n2, originX + widthThird, originY + 2 * widthThird, widthThird, holeList);
        this.addHoles(n2, originX + 2 * widthThird, originY + 2 * widthThird, widthThird, holeList);

        // add the centre hole
        holeList.add(this.createSquareHole(originX + widthThird, originY + widthThird, widthThird));
    }

    private LinearRing createSquareHole(double x, double y, double width) {
        Coordinate[] pts = new Coordinate[] {
                new Coordinate(x, y),
                new Coordinate(x + width, y),
                new Coordinate(x + width, y + width),
                new Coordinate(x, y + width),
                new Coordinate(x, y)
        };
        return this.geomFactory.createLinearRing(pts);
    }
}
