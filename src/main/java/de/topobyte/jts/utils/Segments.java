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

package de.topobyte.jts.utils;

import com.infomatiq.jsi.Rectangle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class Segments {

    public static Rectangle bbox(LineSegment segment) {
        Coordinate p = segment.p0;
        Coordinate q = segment.p1;
        double minX = p.x < q.x ? p.x : q.x;
        double maxX = p.x > q.x ? p.x : q.x;
        double minY = p.y < q.y ? p.y : q.y;
        double maxY = p.y > q.y ? p.y : q.y;
        return new Rectangle((float) minX, (float) minY, (float) maxX,
                (float) maxY);
    }

    public static boolean connected(LineSegment a, LineSegment b) {
        return (a.p0.equals(b.p0) || a.p0.equals(b.p1) || a.p1.equals(b.p0) || a.p1
                .equals(b.p1));
    }
}
