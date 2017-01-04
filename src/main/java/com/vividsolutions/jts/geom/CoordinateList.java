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

import java.util.ArrayList;
import java.util.Collection;

/**
 * A list of {@link Coordinate}s, which may
 * be set to prevent repeated coordinates from occuring in the list.
 *
 * @version 1.7
 */
public class CoordinateList
        extends ArrayList {
    //With contributions from Markus Schaber [schabios@logi-track.com]
    //[Jon Aquino 2004-03-25]
    private final static Coordinate[] coordArrayType = new Coordinate[0];

    /**
     * Constructs a new list without any coordinates
     */
    public CoordinateList() {
        super();
    }

    /**
     * Constructs a new list from an array of Coordinates, allowing repeated points.
     * (I.e. this constructor produces a {@link CoordinateList} with exactly the same set of points
     * as the input array.)
     *
     * @param coord the initial coordinates
     */
    public CoordinateList(Coordinate[] coord) {
        this.ensureCapacity(coord.length);
        this.add(coord, true);
    }

    /**
     * Constructs a new list from an array of Coordinates,
     * allowing caller to specify if repeated points are to be removed.
     *
     * @param coord the array of coordinates to load into the list
     * @param allowRepeated if <code>false</code>, repeated points are removed
     */
    public CoordinateList(Coordinate[] coord, boolean allowRepeated) {
        this.ensureCapacity(coord.length);
        this.add(coord, allowRepeated);
    }

    public Coordinate getCoordinate(int i) {
        return (Coordinate) this.get(i);
    }

    /**
     * Adds a section of an array of coordinates to the list.
     *
     * @param coord The coordinates
     * @param allowRepeated if set to false, repeated coordinates are collapsed
     * @param start the index to start from
     * @param end the index to add up to but not including
     * @return true (as by general collection contract)
     */
    public boolean add(Coordinate[] coord, boolean allowRepeated, int start, int end) {
        int inc = 1;
        if (start > end) {
            inc = -1;
        }

        for (int i = start; i != end; i += inc) {
            this.add(coord[i], allowRepeated);
        }
        return true;
    }

    /**
     * Adds an array of coordinates to the list.
     *
     * @param coord The coordinates
     * @param allowRepeated if set to false, repeated coordinates are collapsed
     * @param direction if false, the array is added in reverse order
     * @return true (as by general collection contract)
     */
    public boolean add(Coordinate[] coord, boolean allowRepeated, boolean direction) {
        if (direction) {
            for (Coordinate aCoord : coord) {
                add(aCoord, allowRepeated);
            }
        } else {
            for (int i = coord.length - 1; i >= 0; i--) {
                this.add(coord[i], allowRepeated);
            }
        }
        return true;
    }

    /**
     * Adds an array of coordinates to the list.
     *
     * @param coord The coordinates
     * @param allowRepeated if set to false, repeated coordinates are collapsed
     * @return true (as by general collection contract)
     */
    public boolean add(Coordinate[] coord, boolean allowRepeated) {
        this.add(coord, allowRepeated, true);
        return true;
    }

    /**
     * Adds a coordinate to the list.
     *
     * @param obj The coordinate to add
     * @param allowRepeated if set to false, repeated coordinates are collapsed
     * @return true (as by general collection contract)
     */
    public boolean add(Object obj, boolean allowRepeated) {
        this.add((Coordinate) obj, allowRepeated);
        return true;
    }

    /**
     * Adds a coordinate to the end of the list.
     *
     * @param coord The coordinates
     * @param allowRepeated if set to false, repeated coordinates are collapsed
     */
    public void add(Coordinate coord, boolean allowRepeated) {
        // don't add duplicate coordinates
        if (!allowRepeated) {
            if (this.size() >= 1) {
                Coordinate last = (Coordinate) this.get(this.size() - 1);
                if (last.equals2D(coord)) {
                    return;
                }
            }
        }
        super.add(coord);
    }

    /**
     * Inserts the specified coordinate at the specified position in this list.
     *
     * @param i the position at which to insert
     * @param coord the coordinate to insert
     * @param allowRepeated if set to false, repeated coordinates are collapsed
     */
    public void add(int i, Coordinate coord, boolean allowRepeated) {
        // don't add duplicate coordinates
        if (!allowRepeated) {
            int size = this.size();
            if (size > 0) {
                if (i > 0) {
                    Coordinate prev = (Coordinate) this.get(i - 1);
                    if (prev.equals2D(coord)) {
                        return;
                    }
                }
                if (i < size) {
                    Coordinate next = (Coordinate) this.get(i);
                    if (next.equals2D(coord)) {
                        return;
                    }
                }
            }
        }
        super.add(i, coord);
    }

    /**
     * Add an array of coordinates
     *
     * @param coll The coordinates
     * @param allowRepeated if set to false, repeated coordinates are collapsed
     * @return true (as by general collection contract)
     */
    public boolean addAll(Collection coll, boolean allowRepeated) {
        boolean isChanged = false;
        for (Object aColl : coll) {
            add((Coordinate) aColl, allowRepeated);
            isChanged = true;
        }
        return isChanged;
    }

    /**
     * Ensure this coordList is a ring, by adding the start point if necessary
     */
    public void closeRing() {
        if (this.size() > 0) {
            this.add(new Coordinate((Coordinate) this.get(0)), false);
        }
    }

    /**
     * Returns the Coordinates in this collection.
     *
     * @return the coordinates
     */
    public Coordinate[] toCoordinateArray() {
        return (Coordinate[]) this.toArray(coordArrayType);
    }

    /**
     * Returns a deep copy of this <tt>CoordinateList</tt> instance.
     *
     * @return a clone of this <tt>CoordinateList</tt> instance
     */
    @Override
    public Object clone() {
        CoordinateList clone = (CoordinateList) super.clone();
        for (int i = 0; i < this.size(); i++) {
            clone.add(i, ((Coordinate) this.get(i)).clone());
        }
        return clone;
    }
}
