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
package com.vividsolutions.jts.geom.prep;

import com.vividsolutions.jts.algorithm.PointLocator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.util.ComponentCoordinateExtracter;

import java.util.List;

/**
 * A base class for {@link PreparedGeometry} subclasses.
 * Contains default implementations for methods, which simply delegate
 * to the equivalent {@link Geometry} methods.
 * This class may be used as a "no-op" class for Geometry types
 * which do not have a corresponding {@link PreparedGeometry} implementation.
 *
 * @author Martin Davis
 */
class BasicPreparedGeometry
        implements PreparedGeometry {
    private Geometry baseGeom;
    private List representativePts;  // List<Coordinate>

    public BasicPreparedGeometry(Geometry geom) {
        this.baseGeom = geom;
        this.representativePts = ComponentCoordinateExtracter.getCoordinates(geom);
    }

    @Override
    public Geometry getGeometry() {
        return this.baseGeom;
    }

    /**
     * Gets the list of representative points for this geometry.
     * One vertex is included for every component of the geometry
     * (i.e. including one for every ring of polygonal geometries)
     *
     * @return a List of Coordinate
     */
    public List getRepresentativePoints() {
        return this.representativePts;
    }

    /**
     * Tests whether any representative of the target geometry
     * intersects the test geometry.
     * This is useful in A/A, A/L, A/P, L/P, and P/P cases.
     *
     * @param geom the test geometry
     * @param repPts the representative points of the target geometry
     * @return true if any component intersects the areal test geometry
     */
    public boolean isAnyTargetComponentInTest(Geometry testGeom) {
        PointLocator locator = new PointLocator();
        for (Object representativePt : representativePts) {
            Coordinate p = (Coordinate) representativePt;
            if (locator.intersects(p, testGeom)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether a Geometry g interacts with
     * this geometry by testing the geometry envelopes.
     *
     * @param g a Geometry
     * @return true if the envelopes intersect
     */
    protected boolean envelopesIntersect(Geometry g) {
        return baseGeom.getEnvelopeInternal().intersects(g.getEnvelopeInternal());
    }

    /**
     * Determines whether the envelope of
     * this geometry covers the Geometry g.
     *
     * @param g a Geometry
     * @return true if g is contained in this envelope
     */
    protected boolean envelopeCovers(Geometry g) {
        return baseGeom.getEnvelopeInternal().covers(g.getEnvelopeInternal());
    }

    /**
     * Default implementation.
     */
    @Override
    public boolean contains(Geometry g) {
        return this.baseGeom.contains(g);
    }

    /**
     * Default implementation.
     */
    @Override
    public boolean containsProperly(Geometry g) {
        // since raw relate is used, provide some optimizations

        // short-circuit test
        if (!this.baseGeom.getEnvelopeInternal().contains(g.getEnvelopeInternal())) {
            return false;
        }

        // otherwise, compute using relate mask
        return this.baseGeom.relate(g, "T**FF*FF*");
    }

    /**
     * Default implementation.
     */
    @Override
    public boolean coveredBy(Geometry g) {
        return this.baseGeom.coveredBy(g);
    }

    /**
     * Default implementation.
     */
    @Override
    public boolean covers(Geometry g) {
        return this.baseGeom.covers(g);
    }

    /**
     * Default implementation.
     */
    @Override
    public boolean crosses(Geometry g) {
        return this.baseGeom.crosses(g);
    }

    /**
     * Standard implementation for all geometries.
     * Supports {@link GeometryCollection}s as input.
     */
    @Override
    public boolean disjoint(Geometry g) {
        return !this.intersects(g);
    }

    /**
     * Default implementation.
     */
    @Override
    public boolean intersects(Geometry g) {
        return this.baseGeom.intersects(g);
    }

    /**
     * Default implementation.
     */
    @Override
    public boolean overlaps(Geometry g) {
        return this.baseGeom.overlaps(g);
    }

    /**
     * Default implementation.
     */
    @Override
    public boolean touches(Geometry g) {
        return this.baseGeom.touches(g);
    }

    /**
     * Default implementation.
     */
    @Override
    public boolean within(Geometry g) {
        return this.baseGeom.within(g);
    }

    public String toString() {
        return this.baseGeom.toString();
    }
}
