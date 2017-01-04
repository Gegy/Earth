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

package com.vividsolutions.jts.noding;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.util.CollectionUtil;

import java.util.Arrays;
import java.util.Collection;

/**
 * Wraps a {@link Noder} and transforms its input
 * into the integer domain.
 * This is intended for use with Snap-Rounding noders,
 * which typically are only intended to work in the integer domain.
 * Offsets can be provided to increase the number of digits of available precision.
 * <p>
 * Clients should be aware that rescaling can involve loss of precision,
 * which can cause zero-length line segments to be created.
 * These in turn can cause problems when used to build a planar graph.
 * This situation should be checked for and collapsed segments removed if necessary.
 *
 * @version 1.7
 */
public class ScaledNoder
        implements Noder {
    private Noder noder;
    private double scaleFactor;
    private double offsetX;
    private double offsetY;
    private boolean isScaled = false;

    public ScaledNoder(Noder noder, double scaleFactor) {
        this(noder, scaleFactor, 0, 0);
    }

    public ScaledNoder(Noder noder, double scaleFactor, double offsetX, double offsetY) {
        this.noder = noder;
        this.scaleFactor = scaleFactor;
        // no need to scale if input precision is already integral
        this.isScaled = !this.isIntegerPrecision();
    }

    public boolean isIntegerPrecision() {
        return this.scaleFactor == 1.0;
    }

    @Override
    public Collection getNodedSubstrings() {
        Collection splitSS = this.noder.getNodedSubstrings();
        if (this.isScaled) {
            this.rescale(splitSS);
        }
        return splitSS;
    }

    @Override
    public void computeNodes(Collection inputSegStrings) {
        Collection intSegStrings = inputSegStrings;
        if (this.isScaled) {
            intSegStrings = this.scale(inputSegStrings);
        }
        this.noder.computeNodes(intSegStrings);
    }

    private Collection scale(Collection segStrings) {
//  	System.out.println("Scaled: scaleFactor = " + scaleFactor);
        return CollectionUtil.transform(segStrings,
                obj -> {
                    SegmentString ss = (SegmentString) obj;
                    return new NodedSegmentString(scale(ss.getCoordinates()), ss.getData());
                }
        );
    }

    private Coordinate[] scale(Coordinate[] pts) {
        Coordinate[] roundPts = new Coordinate[pts.length];
        for (int i = 0; i < pts.length; i++) {
            roundPts[i] = new Coordinate(
                    Math.round((pts[i].x - this.offsetX) * this.scaleFactor),
                    Math.round((pts[i].y - this.offsetY) * this.scaleFactor),
                    pts[i].z
            );
        }
        Coordinate[] roundPtsNoDup = CoordinateArrays.removeRepeatedPoints(roundPts);
        return roundPtsNoDup;
    }

    //private double scale(double val) { return (double) Math.round(val * scaleFactor); }

    private void rescale(Collection segStrings) {
//  	System.out.println("Rescaled: scaleFactor = " + scaleFactor);
        CollectionUtil.apply(segStrings,
                obj -> {
                    SegmentString ss = (SegmentString) obj;
                    rescale(ss.getCoordinates());
                    return null;
                }
        );
    }

    private void rescale(Coordinate[] pts) {
        Coordinate p0 = null;
        Coordinate p1 = null;

        if (pts.length == 2) {
            p0 = new Coordinate(pts[0]);
            p1 = new Coordinate(pts[1]);
        }

        for (Coordinate pt : pts) {
            pt.x = pt.x / scaleFactor + offsetX;
            pt.y = pt.y / this.scaleFactor + this.offsetY;
        }

        if (pts.length == 2 && pts[0].equals2D(pts[1])) {
            System.out.println(Arrays.toString(pts));
        }
    }

    //private double rescale(double val) { return val / scaleFactor; }
}
