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

import java.io.Serializable;

/**
 * The CoordinateSequence implementation that Geometries use by default. In
 * this implementation, Coordinates returned by #toArray and #get are live --
 * parties that change them are actually changing the
 * DefaultCoordinateSequence's underlying data.
 *
 * @version 1.7
 * @deprecated no longer used
 */
class DefaultCoordinateSequence
        implements CoordinateSequence, Serializable {
    //With contributions from Markus Schaber [schabios@logi-track.com] 2004-03-26
    private static final long serialVersionUID = -915438501601840650L;
    private Coordinate[] coordinates;

    /**
     * Constructs a DefaultCoordinateSequence based on the given array (the
     * array is not copied).
     *
     * @param coordinates the coordinate array that will be referenced.
     */
    public DefaultCoordinateSequence(Coordinate[] coordinates) {
        if (Geometry.hasNullElements(coordinates)) {
            throw new IllegalArgumentException("Null coordinate");
        }
        this.coordinates = coordinates;
    }

    /**
     * Creates a new sequence based on a deep copy of the given {@link CoordinateSequence}.
     *
     * @param coordSeq the coordinate sequence that will be copied.
     */
    public DefaultCoordinateSequence(CoordinateSequence coordSeq) {
        this.coordinates = new Coordinate[coordSeq.size()];
        for (int i = 0; i < this.coordinates.length; i++) {
            this.coordinates[i] = coordSeq.getCoordinateCopy(i);
        }
    }

    /**
     * Constructs a sequence of a given size, populated
     * with new {@link Coordinate}s.
     *
     * @param size the size of the sequence to create
     */
    public DefaultCoordinateSequence(int size) {
        this.coordinates = new Coordinate[size];
        for (int i = 0; i < size; i++) {
            this.coordinates[i] = new Coordinate();
        }
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#getDimension()
     */
    @Override
    public int getDimension() {
        return 3;
    }

    /**
     * Get the Coordinate with index i.
     *
     * @param i the index of the coordinate
     * @return the requested Coordinate instance
     */
    @Override
    public Coordinate getCoordinate(int i) {
        return this.coordinates[i];
    }

    /**
     * Get a copy of the Coordinate with index i.
     *
     * @param i the index of the coordinate
     * @return a copy of the requested Coordinate
     */
    @Override
    public Coordinate getCoordinateCopy(int i) {
        return new Coordinate(this.coordinates[i]);
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#getX(int)
     */
    @Override
    public void getCoordinate(int index, Coordinate coord) {
        coord.x = this.coordinates[index].x;
        coord.y = this.coordinates[index].y;
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#getX(int)
     */
    @Override
    public double getX(int index) {
        return this.coordinates[index].x;
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#getY(int)
     */
    @Override
    public double getY(int index) {
        return this.coordinates[index].y;
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#getOrdinate(int, int)
     */
    @Override
    public double getOrdinate(int index, int ordinateIndex) {
        switch (ordinateIndex) {
            case CoordinateSequence.X:
                return this.coordinates[index].x;
            case CoordinateSequence.Y:
                return this.coordinates[index].y;
            case CoordinateSequence.Z:
                return this.coordinates[index].z;
        }
        return Double.NaN;
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#setOrdinate(int, int, double)
     */
    @Override
    public void setOrdinate(int index, int ordinateIndex, double value) {
        switch (ordinateIndex) {
            case CoordinateSequence.X:
                this.coordinates[index].x = value;
                break;
            case CoordinateSequence.Y:
                this.coordinates[index].y = value;
                break;
            case CoordinateSequence.Z:
                this.coordinates[index].z = value;
                break;
        }
    }

    /**
     * Creates a deep copy of the Object
     *
     * @return The deep copy
     */
    @Override
    public Object clone() {
        Coordinate[] cloneCoordinates = new Coordinate[this.size()];
        for (int i = 0; i < this.coordinates.length; i++) {
            cloneCoordinates[i] = (Coordinate) this.coordinates[i].clone();
        }
        return new DefaultCoordinateSequence(cloneCoordinates);
    }

    /**
     * Returns the size of the coordinate sequence
     *
     * @return the number of coordinates
     */
    @Override
    public int size() {
        return this.coordinates.length;
    }

    /**
     * This method exposes the internal Array of Coordinate Objects
     *
     * @return the Coordinate[] array.
     */
    @Override
    public Coordinate[] toCoordinateArray() {
        return this.coordinates;
    }

    @Override
    public Envelope expandEnvelope(Envelope env) {
        for (Coordinate coordinate : coordinates) {
            env.expandToInclude(coordinate);
        }
        return env;
    }

    /**
     * Returns the string Representation of the coordinate array
     *
     * @return The string
     */
    public String toString() {
        if (this.coordinates.length > 0) {
            StringBuffer strBuf = new StringBuffer(17 * this.coordinates.length);
            strBuf.append('(');
            strBuf.append(this.coordinates[0]);
            for (int i = 1; i < this.coordinates.length; i++) {
                strBuf.append(", ");
                strBuf.append(this.coordinates[i]);
            }
            strBuf.append(')');
            return strBuf.toString();
        } else {
            return "()";
        }
    }
}