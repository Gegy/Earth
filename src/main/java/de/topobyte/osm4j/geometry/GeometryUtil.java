// Copyright 2015 Sebastian Kuerten
//
// This file is part of osm4j.
//
// osm4j is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// osm4j is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with osm4j. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.osm4j.geometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import de.topobyte.jts.utils.GeometryGroup;
import de.topobyte.osm4j.core.model.iface.OsmNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GeometryUtil {

    public static MultiPoint createMultiPoint(List<Coordinate> coordinates,
                                              GeometryFactory factory) {
        Coordinate[] coords = coordinates.toArray(new Coordinate[coordinates
                .size()]);
        return factory.createMultiPoint(coords);
    }

    public static MultiLineString createMultiLineString(
            List<LineString> lineStrings, GeometryFactory factory) {
        LineString[] strings = lineStrings.toArray(new LineString[lineStrings
                .size()]);
        return factory.createMultiLineString(strings);
    }

    public static Geometry createGeometry(List<Coordinate> coordinates,
                                          List<LineString> lineStrings, GeometryFactory factory) {
        int numPoints = coordinates.size();
        int numLines = lineStrings.size();

        if (numPoints == 0 && numLines == 0) {
            return new Point(null, factory);
        } else if (numPoints == 0) {
            return lines(lineStrings, factory);
        } else if (numLines == 0) {
            return points(coordinates, factory);
        } else {
            Geometry points = points(coordinates, factory);
            Geometry lines = lines(lineStrings, factory);
            return createGeometryCollection(factory, points, lines);
        }
    }

    public static Geometry createGeometry(Coordinate[] coordinates,
                                          LineString[] lineStrings, GeometryFactory factory) {
        int numPoints = coordinates.length;
        int numLines = lineStrings.length;

        if (numPoints == 0 && numLines == 0) {
            return new Point(null, factory);
        } else if (numPoints == 0) {
            return lines(lineStrings, factory);
        } else if (numLines == 0) {
            return points(coordinates, factory);
        } else {
            Geometry points = points(coordinates, factory);
            Geometry lines = lines(lineStrings, factory);
            return createGeometryCollection(factory, points, lines);
        }
    }

    public static <T extends Geometry> Geometry createGeometry(
            List<Coordinate> coordinates, List<LineString> lineStrings,
            T geometry, GeometryFactory factory) {
        int numPoints = coordinates.size();
        int numLines = lineStrings.size();

        if (numPoints == 0 && numLines == 0) {
            return geometry;
        } else if (numPoints == 0) {
            return createGeometryCollection(factory, geometry,
                    lines(lineStrings, factory));
        } else if (numLines == 0) {
            return createGeometryCollection(factory, geometry,
                    points(coordinates, factory));
        } else {
            Geometry points = points(coordinates, factory);
            Geometry lines = lines(lineStrings, factory);
            return createGeometryCollection(factory, geometry, lines, points);
        }
    }

    public static <T extends Geometry> Geometry createGeometry(
            Coordinate[] coordinates, LineString[] lineStrings, T geometry,
            GeometryFactory factory) {
        int numPoints = coordinates.length;
        int numLines = lineStrings.length;

        if (numPoints == 0 && numLines == 0) {
            return geometry;
        } else if (numPoints == 0) {
            return createGeometryCollection(factory, geometry,
                    lines(lineStrings, factory));
        } else if (numLines == 0) {
            return createGeometryCollection(factory, geometry,
                    points(coordinates, factory));
        } else {
            Geometry points = points(coordinates, factory);
            Geometry lines = lines(lineStrings, factory);
            return createGeometryCollection(factory, geometry, lines, points);
        }
    }

    public static GeometryGroup createGeometryGroup(
            List<Coordinate> coordinates, List<LineString> lineStrings,
            GeometryFactory factory) {
        int numPoints = coordinates.size();
        int numLines = lineStrings.size();

        if (numPoints == 0 && numLines == 0) {
            return new GeometryGroup(factory);
        } else if (numPoints == 0) {
            return new GeometryGroup(factory, lines(lineStrings, factory));
        } else if (numLines == 0) {
            return new GeometryGroup(factory, points(coordinates, factory));
        } else {
            Geometry points = points(coordinates, factory);
            Geometry lines = lines(lineStrings, factory);
            return new GeometryGroup(factory, points, lines);
        }
    }

    public static <T extends Geometry> GeometryGroup createGeometryGroup(
            List<Coordinate> coordinates, List<LineString> lineStrings,
            T geometry, GeometryFactory factory) {
        int numPoints = coordinates.size();
        int numLines = lineStrings.size();

        if (numPoints == 0 && numLines == 0) {
            return new GeometryGroup(factory, geometry);
        } else if (numPoints == 0) {
            return new GeometryGroup(factory, geometry, lines(lineStrings,
                    factory));
        } else if (numLines == 0) {
            return new GeometryGroup(factory, geometry, points(coordinates,
                    factory));
        } else {
            Geometry points = points(coordinates, factory);
            Geometry lines = lines(lineStrings, factory);
            return new GeometryGroup(factory, geometry, points, lines);
        }
    }

    public static GeometryCollection createGeometryCollection(
            GeometryFactory factory, Geometry... parts) {
        return factory.createGeometryCollection(parts);
    }

    public static Geometry points(List<Coordinate> coordinates,
                                  GeometryFactory factory) {
        if (coordinates.size() == 1) {
            return factory.createPoint(coordinates.get(0));
        }
        return factory.createMultiPoint(coordinates
                .toArray(new Coordinate[coordinates.size()]));
    }

    public static Geometry points(Coordinate[] coordinates,
                                  GeometryFactory factory) {
        if (coordinates.length == 1) {
            return factory.createPoint(coordinates[0]);
        }
        return factory.createMultiPoint(coordinates);
    }

    public static Geometry lines(List<LineString> lineStrings,
                                 GeometryFactory factory) {
        if (lineStrings.size() == 1) {
            return lineStrings.get(0);
        }
        return factory.createMultiLineString(lineStrings
                .toArray(new LineString[lineStrings.size()]));
    }

    public static Geometry lines(LineString[] lineStrings,
                                 GeometryFactory factory) {
        if (lineStrings.length == 1) {
            return lineStrings[0];
        }
        return factory.createMultiLineString(lineStrings);
    }

    public static List<Coordinate> buildNodes(NodeBuilder nodeBuilder,
                                              Collection<OsmNode> nodes) {
        List<Coordinate> coords = new ArrayList<>();
        buildNodes(nodeBuilder, nodes, coords);
        return coords;
    }

    public static void buildNodes(NodeBuilder nodeBuilder,
                                  Collection<OsmNode> nodes, List<Coordinate> output) {
        for (OsmNode node : nodes) {
            output.add(nodeBuilder.buildCoordinate(node));
        }
    }
}
