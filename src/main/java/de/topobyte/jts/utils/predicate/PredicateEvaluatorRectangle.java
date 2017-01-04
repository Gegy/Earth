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

package de.topobyte.jts.utils.predicate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class PredicateEvaluatorRectangle extends AbstractPredicateEvaluator {

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private Geometry box = null;

    public PredicateEvaluatorRectangle(double minX, double minY, double maxX,
                                       double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public PredicateEvaluatorRectangle(Envelope envelope) {
        this.minX = envelope.getMinX();
        this.maxX = envelope.getMaxX();
        this.minY = envelope.getMinY();
        this.maxY = envelope.getMaxY();
    }

    private void initBox() {
        if (this.box == null) {
            GeometryFactory factory = new GeometryFactory();
            this.box = factory.toGeometry(new Envelope(this.minX, this.maxX, this.minY, this.maxY));
        }
    }

    @Override
    public boolean covers(Coordinate coordinate) {
        double x = coordinate.x;
        double y = coordinate.y;
        return this.covers(x, y);
    }

    @Override
    public boolean contains(Coordinate coordinate) {
        double x = coordinate.x;
        double y = coordinate.y;
        return this.contains(x, y);
    }

    @Override
    public boolean covers(Point point) {
        double x = point.getX();
        double y = point.getY();
        return this.covers(x, y);
    }

    @Override
    public boolean contains(Point point) {
        double x = point.getX();
        double y = point.getY();
        return this.contains(x, y);
    }

    private boolean covers(double x, double y) {
        return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY;
    }

    private boolean contains(double x, double y) {
        return x > this.minX && x < this.maxX && y > this.minY && y < this.maxY;
    }

    @Override
    public boolean covers(Envelope env) {
        return env.getMinX() >= this.minX && env.getMaxX() <= this.maxX
                && env.getMinY() >= this.minY && env.getMaxY() <= this.maxY;
    }

    @Override
    public boolean contains(Envelope env) {
        return env.getMinX() > this.minX && env.getMaxX() < this.maxX
                && env.getMinY() > this.minY && env.getMaxY() < this.maxY;
    }

    @Override
    public boolean coversNonCollection(Geometry geometry) {
        this.initBox();
        return this.box.covers(geometry);
    }

    @Override
    public boolean containsNonCollection(Geometry geometry) {
        this.initBox();
        return this.box.contains(geometry);
    }

    @Override
    public boolean intersects(Envelope env) {
        return env.getMinX() <= this.maxX && env.getMaxX() >= this.minX
                && env.getMinY() <= this.maxY && env.getMaxY() >= this.minY;
    }

    @Override
    public boolean intersectsNonCollection(Geometry geometry) {
        this.initBox();
        return this.box.intersects(geometry);
    }
}
