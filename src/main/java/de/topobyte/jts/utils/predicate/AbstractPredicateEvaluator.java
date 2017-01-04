// Copyright 2016 Sebastian Kuerten
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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

public abstract class AbstractPredicateEvaluator implements PredicateEvaluator {

    @Override
    public boolean covers(Geometry geometry) {
        if (geometry instanceof GeometryCollection) {
            return this.coversCollection(geometry);
        }
        return this.coversNonCollection(geometry);
    }

    @Override
    public boolean contains(Geometry geometry) {
        if (geometry instanceof GeometryCollection) {
            return this.containsCollection(geometry);
        }
        return this.containsNonCollection(geometry);
    }

    @Override
    public boolean intersects(Geometry geometry) {
        if (geometry instanceof GeometryCollection) {
            return this.intersectsCollection(geometry);
        }
        return this.intersectsNonCollection(geometry);
    }

    public abstract boolean coversNonCollection(Geometry geometry);

    public boolean coversCollection(Geometry b) {
        for (int i = 0; i < b.getNumGeometries(); i++) {
            Geometry g = b.getGeometryN(i);
            if (!this.covers(g)) {
                return false;
            }
        }
        return true;
    }

    public abstract boolean containsNonCollection(Geometry geometry);

    public boolean containsCollection(Geometry b) {
        for (int i = 0; i < b.getNumGeometries(); i++) {
            Geometry g = b.getGeometryN(i);
            if (!this.contains(g)) {
                return false;
            }
        }
        return true;
    }

    public abstract boolean intersectsNonCollection(Geometry geometry);

    public boolean intersectsCollection(Geometry b) {
        for (int i = 0; i < b.getNumGeometries(); i++) {
            Geometry g = b.getGeometryN(i);
            if (this.intersects(g)) {
                return true;
            }
        }
        return false;
    }
}
