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

package com.vividsolutions.jts.geom.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

/**
 * Creates geometries which are shaped like multi-armed stars
 * with each arm shaped like a sine wave.
 * These kinds of geometries are useful as a more complex
 * geometry for testing algorithms.
 *
 * @author Martin Davis
 */
public class SineStarFactory
        extends GeometricShapeFactory {
    protected int numArms = 8;
    protected double armLengthRatio = 0.5;

    /**
     * Creates a factory which will create sine stars using the default
     * {@link GeometryFactory}.
     *
     * @param geomFact the factory to use
     */
    public SineStarFactory() {
        super();
    }

    /**
     * Creates a factory which will create sine stars using the given
     * {@link GeometryFactory}.
     *
     * @param geomFact the factory to use
     */
    public SineStarFactory(GeometryFactory geomFact) {
        super(geomFact);
    }

    /**
     * Sets the number of arms in the star
     *
     * @param numArms the number of arms to generate
     */
    public void setNumArms(int numArms) {
        this.numArms = numArms;
    }

    /**
     * Sets the ration of the length of each arm to the distance from the tip
     * of the arm to the centre of the star.
     * Value should be between 0.0 and 1.0
     *
     * @param armLengthRatio
     */
    public void setArmLengthRatio(double armLengthRatio) {
        this.armLengthRatio = armLengthRatio;
    }

    /**
     * Generates the geometry for the sine star
     *
     * @return the geometry representing the sine star
     */
    public Geometry createSineStar() {
        Envelope env = this.dim.getEnvelope();
        double radius = env.getWidth() / 2.0;

        double armRatio = this.armLengthRatio;
        if (armRatio < 0.0) {
            armRatio = 0.0;
        }
        if (armRatio > 1.0) {
            armRatio = 1.0;
        }

        double armMaxLen = armRatio * radius;
        double insideRadius = (1 - armRatio) * radius;

        double centreX = env.getMinX() + radius;
        double centreY = env.getMinY() + radius;

        Coordinate[] pts = new Coordinate[this.nPts + 1];
        int iPt = 0;
        for (int i = 0; i < this.nPts; i++) {
            // the fraction of the way thru the current arm - in [0,1]
            double ptArcFrac = (i / (double) this.nPts) * this.numArms;
            double armAngFrac = ptArcFrac - Math.floor(ptArcFrac);

            // the angle for the current arm - in [0,2Pi]
            // (each arm is a complete sine wave cycle)
            double armAng = 2 * Math.PI * armAngFrac;
            // the current length of the arm
            double armLenFrac = (Math.cos(armAng) + 1.0) / 2.0;

            // the current radius of the curve (core + arm)
            double curveRadius = insideRadius + armMaxLen * armLenFrac;

            // the current angle of the curve
            double ang = i * (2 * Math.PI / this.nPts);
            double x = curveRadius * Math.cos(ang) + centreX;
            double y = curveRadius * Math.sin(ang) + centreY;
            pts[iPt++] = this.coord(x, y);
        }
        pts[iPt] = new Coordinate(pts[0]);

        LinearRing ring = this.geomFact.createLinearRing(pts);
        Polygon poly = this.geomFact.createPolygon(ring, null);
        return poly;
    }
}
