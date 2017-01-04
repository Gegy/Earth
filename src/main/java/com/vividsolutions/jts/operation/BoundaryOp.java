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
package com.vividsolutions.jts.operation;

import com.vividsolutions.jts.algorithm.BoundaryNodeRule;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Computes the boundary of a {@link Geometry}.
 * Allows specifying the {@link BoundaryNodeRule} to be used.
 * This operation will always return a {@link Geometry} of the appropriate
 * dimension for the boundary (even if the input geometry is empty).
 * The boundary of zero-dimensional geometries (Points) is
 * always the empty {@link GeometryCollection}.
 *
 * @author Martin Davis
 * @version 1.7
 */

public class BoundaryOp {
    private Geometry geom;
    private GeometryFactory geomFact;
    private BoundaryNodeRule bnRule;

    public BoundaryOp(Geometry geom) {
        this(geom, BoundaryNodeRule.MOD2_BOUNDARY_RULE);
    }

    public BoundaryOp(Geometry geom, BoundaryNodeRule bnRule) {
        this.geom = geom;
        this.geomFact = geom.getFactory();
        this.bnRule = bnRule;
    }

    public Geometry getBoundary() {
        if (this.geom instanceof LineString) {
            return this.boundaryLineString((LineString) this.geom);
        }
        if (this.geom instanceof MultiLineString) {
            return this.boundaryMultiLineString((MultiLineString) this.geom);
        }
        return this.geom.getBoundary();
    }

    private MultiPoint getEmptyMultiPoint() {
        return this.geomFact.createMultiPoint((CoordinateSequence) null);
    }

    private Geometry boundaryMultiLineString(MultiLineString mLine) {
        if (this.geom.isEmpty()) {
            return this.getEmptyMultiPoint();
        }

        Coordinate[] bdyPts = this.computeBoundaryCoordinates(mLine);

        // return Point or MultiPoint
        if (bdyPts.length == 1) {
            return this.geomFact.createPoint(bdyPts[0]);
        }
        // this handles 0 points case as well
        return this.geomFact.createMultiPoint(bdyPts);
    }

/*
// MD - superseded
  private Coordinate[] computeBoundaryFromGeometryGraph(MultiLineString mLine)
  {
    GeometryGraph g = new GeometryGraph(0, mLine, bnRule);
    Coordinate[] bdyPts = g.getBoundaryPoints();
    return bdyPts;
  }
*/

    private Map endpointMap;

    private Coordinate[] computeBoundaryCoordinates(MultiLineString mLine) {
        List bdyPts = new ArrayList();
        this.endpointMap = new TreeMap();
        for (int i = 0; i < mLine.getNumGeometries(); i++) {
            LineString line = (LineString) mLine.getGeometryN(i);
            if (line.getNumPoints() == 0) {
                continue;
            }
            this.addEndpoint(line.getCoordinateN(0));
            this.addEndpoint(line.getCoordinateN(line.getNumPoints() - 1));
        }

        for (Object o : endpointMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Counter counter = (Counter) entry.getValue();
            int valence = counter.count;
            if (this.bnRule.isInBoundary(valence)) {
                bdyPts.add(entry.getKey());
            }
        }

        return CoordinateArrays.toCoordinateArray(bdyPts);
    }

    private void addEndpoint(Coordinate pt) {
        Counter counter = (Counter) this.endpointMap.get(pt);
        if (counter == null) {
            counter = new Counter();
            this.endpointMap.put(pt, counter);
        }
        counter.count++;
    }

    private Geometry boundaryLineString(LineString line) {
        if (this.geom.isEmpty()) {
            return this.getEmptyMultiPoint();
        }

        if (line.isClosed()) {
            // check whether endpoints of valence 2 are on the boundary or not
            boolean closedEndpointOnBoundary = this.bnRule.isInBoundary(2);
            if (closedEndpointOnBoundary) {
                return line.getStartPoint();
            } else {
                return this.geomFact.createMultiPoint((Coordinate[]) null);
            }
        }
        return this.geomFact.createMultiPoint(new Point[] {
                line.getStartPoint(),
                line.getEndPoint()
        });
    }
}

/**
 * Stores an integer count, for use as a Map entry.
 *
 * @author Martin Davis
 * @version 1.7
 */
class Counter {
    /**
     * The value of the count
     */
    int count;
}
