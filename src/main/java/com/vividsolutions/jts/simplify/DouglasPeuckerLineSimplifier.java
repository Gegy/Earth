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

package com.vividsolutions.jts.simplify;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.LineSegment;

/**
 * Simplifies a linestring (sequence of points) using
 * the standard Douglas-Peucker algorithm.
 *
 * @version 1.7
 */
class DouglasPeuckerLineSimplifier {
    public static Coordinate[] simplify(Coordinate[] pts, double distanceTolerance) {
        DouglasPeuckerLineSimplifier simp = new DouglasPeuckerLineSimplifier(pts);
        simp.setDistanceTolerance(distanceTolerance);
        return simp.simplify();
    }

    private Coordinate[] pts;
    private boolean[] usePt;
    private double distanceTolerance;

    public DouglasPeuckerLineSimplifier(Coordinate[] pts) {
        this.pts = pts;
    }

    /**
     * Sets the distance tolerance for the simplification.
     * All vertices in the simplified linestring will be within this
     * distance of the original linestring.
     *
     * @param distanceTolerance the approximation tolerance to use
     */
    public void setDistanceTolerance(double distanceTolerance) {
        this.distanceTolerance = distanceTolerance;
    }

    public Coordinate[] simplify() {
        this.usePt = new boolean[this.pts.length];
        for (int i = 0; i < this.pts.length; i++) {
            this.usePt[i] = true;
        }
        this.simplifySection(0, this.pts.length - 1);
        CoordinateList coordList = new CoordinateList();
        for (int i = 0; i < this.pts.length; i++) {
            if (this.usePt[i]) {
                coordList.add(new Coordinate(this.pts[i]));
            }
        }
        return coordList.toCoordinateArray();
    }

    private LineSegment seg = new LineSegment();

    private void simplifySection(int i, int j) {
        if ((i + 1) == j) {
            return;
        }
        this.seg.p0 = this.pts[i];
        this.seg.p1 = this.pts[j];
        double maxDistance = -1.0;
        int maxIndex = i;
        for (int k = i + 1; k < j; k++) {
            double distance = this.seg.distance(this.pts[k]);
            if (distance > maxDistance) {
                maxDistance = distance;
                maxIndex = k;
            }
        }
        if (maxDistance <= this.distanceTolerance) {
            for (int k = i + 1; k < j; k++) {
                this.usePt[k] = false;
            }
        } else {
            this.simplifySection(i, maxIndex);
            this.simplifySection(maxIndex, j);
        }
    }
}
