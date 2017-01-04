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
package com.vividsolutions.jts.geom.impl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import java.io.Serializable;

/**
 * A {@link CoordinateSequence} backed by an array of {@link Coordinates}.
 * This is the implementation that {@link Geometry}s use by default.
 * Coordinates returned by #toArray and #getCoordinate are live --
 * modifications to them are actually changing the
 * CoordinateSequence's underlying data.
 * A dimension may be specified for the coordinates in the sequence,
 * which may be 2 or 3.
 * The actual coordinates will always have 3 ordinates,
 * but the dimension is useful as metadata in some situations.
 *
 * @version 1.7
 */
public class CoordinateArraySequence
        implements CoordinateSequence, Serializable {
    //With contributions from Markus Schaber [schabios@logi-track.com] 2004-03-26
    private static final long serialVersionUID = -915438501601840650L;

    /**
     * The actual dimension of the coordinates in the sequence.
     * Allowable values are 2 or 3.
     */
    private int dimension = 3;

    private Coordinate[] coordinates;

    /**
     * Constructs a sequence based on the given array
     * of {@link Coordinate}s (the
     * array is not copied).
     * The coordinate dimension defaults to 3.
     *
     * @param coordinates the coordinate array that will be referenced.
     */
    public CoordinateArraySequence(Coordinate[] coordinates) {
        this(coordinates, 3);
    }

    /**
     * Constructs a sequence based on the given array
     * of {@link Coordinate}s (the
     * array is not copied).
     *
     * @param coordinates the coordinate array that will be referenced.
     * @param the dimension of the coordinates
     */
    public CoordinateArraySequence(Coordinate[] coordinates, int dimension) {
        this.coordinates = coordinates;
        this.dimension = dimension;
        if (coordinates == null) {
            this.coordinates = new Coordinate[0];
        }
    }

    /**
     * Constructs a sequence of a given size, populated
     * with new {@link Coordinate}s.
     *
     * @param size the size of the sequence to create
     */
    public CoordinateArraySequence(int size) {
        this.coordinates = new Coordinate[size];
        for (int i = 0; i < size; i++) {
            this.coordinates[i] = new Coordinate();
        }
    }

    /**
     * Constructs a sequence of a given size, populated
     * with new {@link Coordinate}s.
     *
     * @param size the size of the sequence to create
     * @param the dimension of the coordinates
     */
    public CoordinateArraySequence(int size, int dimension) {
        this.coordinates = new Coordinate[size];
        this.dimension = dimension;
        for (int i = 0; i < size; i++) {
            this.coordinates[i] = new Coordinate();
        }
    }

    /**
     * Creates a new sequence based on a deep copy of the given {@link CoordinateSequence}.
     * The coordinate dimension is set to equal the dimension of the input.
     *
     * @param coordSeq the coordinate sequence that will be copied.
     */
    public CoordinateArraySequence(CoordinateSequence coordSeq) {
        if (coordSeq != null) {
            this.dimension = coordSeq.getDimension();
            this.coordinates = new Coordinate[coordSeq.size()];
        } else {
            this.coordinates = new Coordinate[0];
        }

        for (int i = 0; i < this.coordinates.length; i++) {
            this.coordinates[i] = coordSeq.getCoordinateCopy(i);
        }
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#getDimension()
     */
    @Override
    public int getDimension() {
        return this.dimension;
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
        coord.z = this.coordinates[index].z;
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
        return new CoordinateArraySequence(cloneCoordinates);
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
            default:
                throw new IllegalArgumentException("invalid ordinateIndex");
        }
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