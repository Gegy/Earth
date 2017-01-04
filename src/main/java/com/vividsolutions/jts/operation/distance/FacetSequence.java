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

package com.vividsolutions.jts.operation.distance;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Represents a sequence of facets (points or line segments)
 * of a {@link Geometry}
 * specified by a subsequence of a {@link CoordinateSequence}.
 *
 * @author Martin Davis
 */
public class FacetSequence {
    private CoordinateSequence pts;
    private int start;
    private int end;

    // temporary Coordinates to materialize points from the CoordinateSequence
    private Coordinate pt = new Coordinate();
    private Coordinate seqPt = new Coordinate();

    /**
     * Creates a new section based on a CoordinateSequence.
     *
     * @param pts the sequence holding the points in the section
     * @param start the index of the start point
     * @param end the index of the end point + 1
     */
    public FacetSequence(CoordinateSequence pts, int start, int end) {
        this.pts = pts;
        this.start = start;
        this.end = end;
    }

    /**
     * Creates a new sequence for a single point from a CoordinateSequence.
     *
     * @param pts the sequence holding the points in the facet sequence
     * @param start the index of the point
     */
    public FacetSequence(CoordinateSequence pts, int start) {
        this.pts = pts;
        this.start = start;
        this.end = start + 1;
    }

    public Envelope getEnvelope() {
        Envelope env = new Envelope();
        for (int i = this.start; i < this.end; i++) {
            env.expandToInclude(this.pts.getX(i), this.pts.getY(i));
        }
        return env;
    }

    public int size() {
        return this.end - this.start;
    }

    public Coordinate getCoordinate(int index) {
        return this.pts.getCoordinate(this.start + index);
    }

    public boolean isPoint() {
        return this.end - this.start == 1;
    }

    public double distance(FacetSequence facetSeq) {
        boolean isPoint = this.isPoint();
        boolean isPointOther = facetSeq.isPoint();

        if (isPoint && isPointOther) {
            this.pts.getCoordinate(this.start, this.pt);
            facetSeq.pts.getCoordinate(facetSeq.start, this.seqPt);
            return this.pt.distance(this.seqPt);
        } else if (isPoint) {
            this.pts.getCoordinate(this.start, this.pt);
            return this.computePointLineDistance(this.pt, facetSeq);
        } else if (isPointOther) {
            facetSeq.pts.getCoordinate(facetSeq.start, this.seqPt);
            return this.computePointLineDistance(this.seqPt, this);
        }
        return this.computeLineLineDistance(facetSeq);
    }

    // temporary Coordinates to materialize points from the CoordinateSequence
    private Coordinate p0 = new Coordinate();
    private Coordinate p1 = new Coordinate();
    private Coordinate q0 = new Coordinate();
    private Coordinate q1 = new Coordinate();

    private double computeLineLineDistance(FacetSequence facetSeq) {
        // both linear - compute minimum segment-segment distance
        double minDistance = Double.MAX_VALUE;

        for (int i = this.start; i < this.end - 1; i++) {
            for (int j = facetSeq.start; j < facetSeq.end - 1; j++) {
                this.pts.getCoordinate(i, this.p0);
                this.pts.getCoordinate(i + 1, this.p1);
                facetSeq.pts.getCoordinate(j, this.q0);
                facetSeq.pts.getCoordinate(j + 1, this.q1);

                double dist = CGAlgorithms.distanceLineLine(this.p0, this.p1, this.q0, this.q1);
                if (dist == 0.0) {
                    return 0.0;
                }
                if (dist < minDistance) {
                    minDistance = dist;
                }
            }
        }
        return minDistance;
    }

    private double computePointLineDistance(Coordinate pt, FacetSequence facetSeq) {
        double minDistance = Double.MAX_VALUE;

        for (int i = facetSeq.start; i < facetSeq.end - 1; i++) {
            facetSeq.pts.getCoordinate(i, this.q0);
            facetSeq.pts.getCoordinate(i + 1, this.q1);
            double dist = CGAlgorithms.distancePointLine(pt, this.q0, this.q1);
            if (dist == 0.0) {
                return 0.0;
            }
            if (dist < minDistance) {
                minDistance = dist;
            }
        }
        return minDistance;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("LINESTRING ( ");
        Coordinate p = new Coordinate();
        for (int i = this.start; i < this.end; i++) {
            if (i > this.start) {
                buf.append(", ");
            }
            this.pts.getCoordinate(i, p);
            buf.append(p.x).append(" ").append(p.y);
        }
        buf.append(" )");
        return buf.toString();
    }
}
