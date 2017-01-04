// Copyright 2015 Sebastian Kuerten
//
// This file is part of jts2awt.
//
// jts2awt is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// jts2awt is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with jts2awt. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.jts2awt;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import de.topobyte.jgs.transform.CoordinateTransformer;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class Jts2Awt {

    /**
     * Convert a geometry to a shape using the coordinate transformation.
     *
     * @param g the geometry to transform.
     * @param ct the coordinate transformation to use.
     * @return the created shape.
     */
    public static Shape toShape(Geometry g, CoordinateTransformer ct) {
        if (g instanceof MultiPolygon) {
            return toShape((MultiPolygon) g, ct);
        }
        if (g instanceof Polygon) {
            return toShape((Polygon) g, ct);
        }
        return new Area();
    }

    /**
     * Convert a multipolygon to a shape using the coordinate transformation.
     *
     * @param mp the polygon to transform.
     * @param ct the coordinate transformation to use.
     * @return the created shape.
     */
    public static Area toShape(MultiPolygon mp, CoordinateTransformer ct) {
        Area area = new Area();
        for (int i = 0; i < mp.getNumGeometries(); i++) {
            Geometry geom = mp.getGeometryN(i);
            if (geom instanceof Polygon) {
                Area a = toShape((Polygon) geom, ct);
                area.add(a);
            }
        }
        return area;
    }

    /**
     * Convert a polygon to a shape using the coordinate transformation.
     *
     * @param p the polygon to transform.
     * @param ct the coordinate transformation to use.
     * @return the created shape.
     */
    public static Area toShape(Polygon p, CoordinateTransformer ct) {
        if (p.isEmpty()) {
            return new Area();
        }

        LineString ring = p.getExteriorRing();
        Area outer = getArea(ring, ct);

        for (int i = 0; i < p.getNumInteriorRing(); i++) {
            LineString interior = p.getInteriorRingN(i);
            Area inner = getArea(interior, ct);
            outer.subtract(inner);
        }

        return outer;
    }

    /**
     * Convert a linear ring to an area using the coordinate transformation.
     *
     * @param ring the ring to create an area from
     * @param ct the transformation to apply
     * @return the created area.
     */
    public static Area getArea(LineString ring, CoordinateTransformer ct) {
        Path2D.Double path = new Path2D.Double();
        Coordinate coord = ring.getCoordinateN(0);
        path.moveTo(ct.getX(coord.x), ct.getY(coord.y));
        for (int i = 1; i < ring.getNumPoints(); i++) {
            coord = ring.getCoordinateN(i);
            path.lineTo(ct.getX(coord.x), ct.getY(coord.y));
        }
        path.closePath();
        return new Area(path);
    }

    /**
     * Convert a linestring to a path using the coordinate transformation.
     *
     * @param string the string to create a path from.
     * @param ct the transformation to apply.
     * @return the path created.
     */
    public static Path2D getPath(LineString string, CoordinateTransformer ct) {
        Path2D.Double path = new Path2D.Double();
        Coordinate coord = string.getCoordinateN(0);
        path.moveTo(ct.getX(coord.x), ct.getY(coord.y));
        for (int i = 1; i < string.getNumPoints(); i++) {
            coord = string.getCoordinateN(i);
            path.lineTo(ct.getX(coord.x), ct.getY(coord.y));
        }
        return path;
    }
}
