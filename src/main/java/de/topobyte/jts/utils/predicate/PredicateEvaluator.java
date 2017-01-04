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
import com.vividsolutions.jts.geom.Point;

public interface PredicateEvaluator {

    /**
     * Test for coverage. Includes the boundary of geometric objects.
     */
    boolean covers(Coordinate coordinate);

    /**
     * Test for containment. Excludes the boundary of geometric objects.
     */
    boolean contains(Coordinate coordinate);

    /**
     * Test for coverage. Includes the boundary of geometric objects.
     */
    boolean covers(Point point);

    /**
     * Test for containment. Excludes the boundary of geometric objects.
     */
    boolean contains(Point point);

    /**
     * Test for coverage. Includes the boundary of geometric objects.
     */
    boolean covers(Envelope envelope);

    /**
     * Test for containment. Excludes the boundary of geometric objects.
     */
    boolean contains(Envelope envelope);

    /**
     * Test for coverage. Includes the boundary of geometric objects.
     */
    boolean covers(Geometry geometry);

    /**
     * Test for containment. Excludes the boundary of geometric objects.
     */
    boolean contains(Geometry geometry);

    /**
     * Test for intersection.
     */
    boolean intersects(Geometry geometry);

    /**
     * Test for intersection.
     */
    boolean intersects(Envelope envelope);
}
