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
package com.vividsolutions.jts.algorithm;

import com.vividsolutions.jts.algorithm.locate.IndexedPointInAreaLocator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.index.bintree.Bintree;
import com.vividsolutions.jts.index.bintree.Interval;
import com.vividsolutions.jts.index.chain.MonotoneChain;
import com.vividsolutions.jts.index.chain.MonotoneChainBuilder;
import com.vividsolutions.jts.index.chain.MonotoneChainSelectAction;

import java.util.List;

/**
 * Implements {@link PointInRing}
 * using {@link MonotoneChain}s and a {@link Bintree} index to
 * increase performance.
 *
 * @version 1.7
 * @see IndexedPointInAreaLocator for more general functionality
 */
public class MCPointInRing implements PointInRing {

    class MCSelecter extends MonotoneChainSelectAction {
        Coordinate p;

        public MCSelecter(Coordinate p) {
            this.p = p;
        }

        @Override
        public void select(LineSegment ls) {
            MCPointInRing.this.testLineSegment(this.p, ls);
        }
    }

    private LinearRing ring;
    private Bintree tree;
    private int crossings = 0;  // number of segment/ray crossings

    public MCPointInRing(LinearRing ring) {
        this.ring = ring;
        this.buildIndex();
    }

    private void buildIndex() {
        //Envelope env = ring.getEnvelopeInternal();
        this.tree = new Bintree();

        Coordinate[] pts = CoordinateArrays.removeRepeatedPoints(this.ring.getCoordinates());
        List mcList = MonotoneChainBuilder.getChains(pts);

        for (Object aMcList : mcList) {
            MonotoneChain mc = (MonotoneChain) aMcList;
            Envelope mcEnv = mc.getEnvelope();
            this.interval.min = mcEnv.getMinY();
            this.interval.max = mcEnv.getMaxY();
            this.tree.insert(this.interval, mc);
        }
    }

    private Interval interval = new Interval();

    @Override
    public boolean isInside(Coordinate pt) {
        this.crossings = 0;

        // test all segments intersected by ray from pt in positive x direction
        Envelope rayEnv = new Envelope(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, pt.y, pt.y);

        this.interval.min = pt.y;
        this.interval.max = pt.y;
        List segs = this.tree.query(this.interval);
//System.out.println("query size = " + segs.size());

        MCSelecter mcSelecter = new MCSelecter(pt);
        for (Object seg : segs) {
            MonotoneChain mc = (MonotoneChain) seg;
            this.testMonotoneChain(rayEnv, mcSelecter, mc);
        }

    /*
     *  p is inside if number of crossings is odd.
     */
        return (crossings % 2) == 1;
    }

    private void testMonotoneChain(Envelope rayEnv, MCSelecter mcSelecter, MonotoneChain mc) {
        mc.select(rayEnv, mcSelecter);
    }

    private void testLineSegment(Coordinate p, LineSegment seg) {
        double xInt;  // x intersection of segment with ray
        double x1;    // translated coordinates
        double y1;
        double x2;
        double y2;

    /*
     *  Test if segment crosses ray from test point in positive x direction.
     */
        Coordinate p1 = seg.p0;
        Coordinate p2 = seg.p1;
        x1 = p1.x - p.x;
        y1 = p1.y - p.y;
        x2 = p2.x - p.x;
        y2 = p2.y - p.y;

        if (((y1 > 0) && (y2 <= 0)) ||
                ((y2 > 0) && (y1 <= 0))) {
        /*
         *  segment straddles x axis, so compute intersection.
         */
            xInt = RobustDeterminant.signOfDet2x2(x1, y1, x2, y2) / (y2 - y1);
            //xsave = xInt;
        /*
         *  crosses ray if strictly positive intersection.
         */
            if (0.0 < xInt) {
                this.crossings++;
            }
        }
    }
}
