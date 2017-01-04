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
package com.vividsolutions.jts.precision;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;

/**
 * Computes the minimum clearance of a geometry or
 * set of geometries.
 * <p>
 * The <b>Minimum Clearance</b> is a measure of
 * what magnitude of perturbation of its vertices can be tolerated
 * by a geometry before it becomes topologically invalid.
 * <p>
 * This class uses an inefficient O(N^2) scan.
 * It is primarily for testing purposes.
 *
 * @author Martin Davis
 * @see MinimumClearance
 */
public class SimpleMinimumClearance {
    public static double getDistance(Geometry g) {
        SimpleMinimumClearance rp = new SimpleMinimumClearance(g);
        return rp.getDistance();
    }

    public static Geometry getLine(Geometry g) {
        SimpleMinimumClearance rp = new SimpleMinimumClearance(g);
        return rp.getLine();
    }

    private Geometry inputGeom;
    private double minClearance;
    private Coordinate[] minClearancePts;

    public SimpleMinimumClearance(Geometry geom) {
        this.inputGeom = geom;
    }

    public double getDistance() {
        this.compute();
        return this.minClearance;
    }

    public LineString getLine() {
        this.compute();
        return this.inputGeom.getFactory().createLineString(this.minClearancePts);
    }

    private void compute() {
        if (this.minClearancePts != null) {
            return;
        }
        this.minClearancePts = new Coordinate[2];
        this.minClearance = Double.MAX_VALUE;
        this.inputGeom.apply(new VertexCoordinateFilter());
    }

    private void updateClearance(double candidateValue, Coordinate p0, Coordinate p1) {
        if (candidateValue < this.minClearance) {
            this.minClearance = candidateValue;
            this.minClearancePts[0] = new Coordinate(p0);
            this.minClearancePts[1] = new Coordinate(p1);
        }
    }

    private void updateClearance(double candidateValue, Coordinate p,
                                 Coordinate seg0, Coordinate seg1) {
        if (candidateValue < this.minClearance) {
            this.minClearance = candidateValue;
            this.minClearancePts[0] = new Coordinate(p);
            LineSegment seg = new LineSegment(seg0, seg1);
            this.minClearancePts[1] = new Coordinate(seg.closestPoint(p));
        }
    }

    private class VertexCoordinateFilter
            implements CoordinateFilter {
        public VertexCoordinateFilter() {

        }

        @Override
        public void filter(Coordinate coord) {
            SimpleMinimumClearance.this.inputGeom.apply(new ComputeMCCoordinateSequenceFilter(coord));
        }
    }

    private class ComputeMCCoordinateSequenceFilter
            implements CoordinateSequenceFilter {
        private Coordinate queryPt;

        public ComputeMCCoordinateSequenceFilter(Coordinate queryPt) {
            this.queryPt = queryPt;
        }

        @Override
        public void filter(CoordinateSequence seq, int i) {
            // compare to vertex
            this.checkVertexDistance(seq.getCoordinate(i));

            // compare to segment, if this is one
            if (i > 0) {
                this.checkSegmentDistance(seq.getCoordinate(i - 1), seq.getCoordinate(i));
            }
        }

        private void checkVertexDistance(Coordinate vertex) {
            double vertexDist = vertex.distance(this.queryPt);
            if (vertexDist > 0) {
                SimpleMinimumClearance.this.updateClearance(vertexDist, this.queryPt, vertex);
            }
        }

        private void checkSegmentDistance(Coordinate seg0, Coordinate seg1) {
            if (this.queryPt.equals2D(seg0) || this.queryPt.equals2D(seg1)) {
                return;
            }
            double segDist = CGAlgorithms.distancePointLine(this.queryPt, seg1, seg0);
            if (segDist > 0) {
                SimpleMinimumClearance.this.updateClearance(segDist, this.queryPt, seg1, seg0);
            }
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public boolean isGeometryChanged() {
            return false;
        }
    }
}
