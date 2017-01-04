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
package com.vividsolutions.jts.geom;

import com.vividsolutions.jts.util.Assert;

import java.util.Arrays;
import java.util.TreeSet;

/**
 * Models a collection of {@link Geometry}s of
 * arbitrary type and dimension.
 *
 * @version 1.7
 */
public class GeometryCollection extends Geometry {
    //  With contributions from Markus Schaber [schabios@logi-track.com] 2004-03-26
    private static final long serialVersionUID = -5694727726395021467L;
    /**
     * Internal representation of this <code>GeometryCollection</code>.
     */
    protected Geometry[] geometries;

    /**
     * @deprecated Use GeometryFactory instead
     */
    public GeometryCollection(Geometry[] geometries, PrecisionModel precisionModel, int SRID) {
        this(geometries, new GeometryFactory(precisionModel, SRID));
    }

    /**
     * @param geometries the <code>Geometry</code>s for this <code>GeometryCollection</code>,
     * or <code>null</code> or an empty array to create the empty
     * geometry. Elements may be empty <code>Geometry</code>s,
     * but not <code>null</code>s.
     */
    public GeometryCollection(Geometry[] geometries, GeometryFactory factory) {
        super(factory);
        if (geometries == null) {
            geometries = new Geometry[] {};
        }
        if (hasNullElements(geometries)) {
            throw new IllegalArgumentException("geometries must not contain null elements");
        }
        this.geometries = geometries;
    }

    @Override
    public Coordinate getCoordinate() {
        if (this.isEmpty()) {
            return null;
        }
        return this.geometries[0].getCoordinate();
    }

    /**
     * Collects all coordinates of all subgeometries into an Array.
     * <p>
     * Note that while changes to the coordinate objects themselves
     * may modify the Geometries in place, the returned Array as such
     * is only a temporary container which is not synchronized back.
     *
     * @return the collected coordinates
     */
    @Override
    public Coordinate[] getCoordinates() {
        Coordinate[] coordinates = new Coordinate[this.getNumPoints()];
        int k = -1;
        for (Geometry geometry : geometries) {
            Coordinate[] childCoordinates = geometry.getCoordinates();
            for (Coordinate childCoordinate : childCoordinates) {
                k++;
                coordinates[k] = childCoordinate;
            }
        }
        return coordinates;
    }

    @Override
    public boolean isEmpty() {
        for (Geometry geometry : geometries) {
            if (!geometry.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getDimension() {
        int dimension = Dimension.FALSE;
        for (Geometry geometry : geometries) {
            dimension = Math.max(dimension, geometry.getDimension());
        }
        return dimension;
    }

    @Override
    public int getBoundaryDimension() {
        int dimension = Dimension.FALSE;
        for (Geometry geometry : geometries) {
            dimension = Math.max(dimension, ((Geometry) geometry).getBoundaryDimension());
        }
        return dimension;
    }

    @Override
    public int getNumGeometries() {
        return this.geometries.length;
    }

    @Override
    public Geometry getGeometryN(int n) {
        return this.geometries[n];
    }

    @Override
    public int getNumPoints() {
        int numPoints = 0;
        for (Geometry geometry : geometries) {
            numPoints += ((Geometry) geometry).getNumPoints();
        }
        return numPoints;
    }

    @Override
    public String getGeometryType() {
        return "GeometryCollection";
    }

    @Override
    public Geometry getBoundary() {
        this.checkNotGeometryCollection(this);
        Assert.shouldNeverReachHere();
        return null;
    }

    /**
     * Returns the area of this <code>GeometryCollection</code>
     *
     * @return the area of the polygon
     */
    @Override
    public double getArea() {
        double area = 0.0;
        for (Geometry geometry : geometries) {
            area += geometry.getArea();
        }
        return area;
    }

    @Override
    public double getLength() {
        double sum = 0.0;
        for (Geometry geometry : geometries) {
            sum += (geometry).getLength();
        }
        return sum;
    }

    @Override
    public boolean equalsExact(Geometry other, double tolerance) {
        if (!this.isEquivalentClass(other)) {
            return false;
        }
        GeometryCollection otherCollection = (GeometryCollection) other;
        if (this.geometries.length != otherCollection.geometries.length) {
            return false;
        }
        for (int i = 0; i < this.geometries.length; i++) {
            if (!geometries[i].equalsExact(otherCollection.geometries[i], tolerance)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void apply(CoordinateFilter filter) {
        for (Geometry geometry : geometries) {
            geometry.apply(filter);
        }
    }

    @Override
    public void apply(CoordinateSequenceFilter filter) {
        if (this.geometries.length == 0) {
            return;
        }
        for (Geometry geometry : geometries) {
            geometry.apply(filter);
            if (filter.isDone()) {
                break;
            }
        }
        if (filter.isGeometryChanged()) {
            this.geometryChanged();
        }
    }

    @Override
    public void apply(GeometryFilter filter) {
        filter.filter(this);
        for (Geometry geometry : geometries) {
            geometry.apply(filter);
        }
    }

    @Override
    public void apply(GeometryComponentFilter filter) {
        filter.filter(this);
        for (Geometry geometry : geometries) {
            geometry.apply(filter);
        }
    }

    /**
     * Creates and returns a full copy of this {@link GeometryCollection} object.
     * (including all coordinates contained by it).
     *
     * @return a clone of this instance
     */
    @Override
    public Object clone() {
        GeometryCollection gc = (GeometryCollection) super.clone();
        gc.geometries = new Geometry[this.geometries.length];
        for (int i = 0; i < this.geometries.length; i++) {
            gc.geometries[i] = (Geometry) this.geometries[i].clone();
        }
        return gc;// return the clone
    }

    @Override
    public void normalize() {
        for (Geometry geometry : geometries) {
            geometry.normalize();
        }
        Arrays.sort(this.geometries);
    }

    @Override
    protected Envelope computeEnvelopeInternal() {
        Envelope envelope = new Envelope();
        for (Geometry geometry : geometries) {
            envelope.expandToInclude(geometry.getEnvelopeInternal());
        }
        return envelope;
    }

    @Override
    protected int compareToSameClass(Object o) {
        TreeSet theseElements = new TreeSet(Arrays.asList(this.geometries));
        TreeSet otherElements = new TreeSet(Arrays.asList(((GeometryCollection) o).geometries));
        return this.compare(theseElements, otherElements);
    }

    @Override
    protected int compareToSameClass(Object o, CoordinateSequenceComparator comp) {
        GeometryCollection gc = (GeometryCollection) o;

        int n1 = this.getNumGeometries();
        int n2 = gc.getNumGeometries();
        int i = 0;
        while (i < n1 && i < n2) {
            Geometry thisGeom = this.getGeometryN(i);
            Geometry otherGeom = gc.getGeometryN(i);
            int holeComp = thisGeom.compareToSameClass(otherGeom, comp);
            if (holeComp != 0) {
                return holeComp;
            }
            i++;
        }
        if (i < n1) {
            return 1;
        }
        if (i < n2) {
            return -1;
        }
        return 0;
    }

    /**
     * Creates a {@link GeometryCollection} with
     * every component reversed.
     * The order of the components in the collection are not reversed.
     *
     * @return a {@link GeometryCollection} in the reverse order
     */
    @Override
    public Geometry reverse() {
        int n = this.geometries.length;
        Geometry[] revGeoms = new Geometry[n];
        for (int i = 0; i < this.geometries.length; i++) {
            revGeoms[i] = this.geometries[i].reverse();
        }
        return this.getFactory().createGeometryCollection(revGeoms);
    }
}

