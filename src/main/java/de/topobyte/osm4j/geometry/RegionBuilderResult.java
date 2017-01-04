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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import de.topobyte.jts.utils.GeometryGroup;

import java.util.ArrayList;
import java.util.List;

public class RegionBuilderResult {

    private List<Coordinate> coordinates = new ArrayList<>();
    private List<LineString> lineStrings = new ArrayList<>();
    private MultiPolygon multiPolygon = null;

    public List<Coordinate> getCoordinates() {
        return this.coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public List<LineString> getLineStrings() {
        return this.lineStrings;
    }

    public void setLineStrings(List<LineString> lineStrings) {
        this.lineStrings = lineStrings;
    }

    public MultiPolygon getMultiPolygon() {
        return this.multiPolygon;
    }

    public void setMultiPolygon(MultiPolygon multiPolygon) {
        this.multiPolygon = multiPolygon;
    }

    public void clear() {
        this.coordinates.clear();
        this.lineStrings.clear();
        this.multiPolygon = null;
    }

    public Geometry toGeometry(GeometryFactory factory) {
        if (this.multiPolygon == null) {
            return GeometryUtil.createGeometry(this.coordinates, this.lineStrings,
                    factory);
        } else {
            return GeometryUtil.createGeometry(this.coordinates, this.lineStrings,
                    this.multiPolygon, factory);
        }
    }

    public GeometryGroup toGeometryGroup(GeometryFactory factory) {
        if (this.multiPolygon == null) {
            return GeometryUtil.createGeometryGroup(this.coordinates, this.lineStrings,
                    factory);
        } else {
            return GeometryUtil.createGeometryGroup(this.coordinates, this.lineStrings,
                    this.multiPolygon, factory);
        }
    }
}
