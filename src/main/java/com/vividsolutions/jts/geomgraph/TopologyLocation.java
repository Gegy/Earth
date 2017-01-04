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
package com.vividsolutions.jts.geomgraph;

import com.vividsolutions.jts.geom.Location;

/**
 * A TopologyLocation is the labelling of a
 * GraphComponent's topological relationship to a single Geometry.
 * <p>
 * If the parent component is an area edge, each side and the edge itself
 * have a topological location.  These locations are named
 * <ul>
 * <li> ON: on the edge
 * <li> LEFT: left-hand side of the edge
 * <li> RIGHT: right-hand side
 * </ul>
 * If the parent component is a line edge or node, there is a single
 * topological relationship attribute, ON.
 * <p>
 * The possible values of a topological location are
 * {Location.NONE, Location.EXTERIOR, Location.BOUNDARY, Location.INTERIOR}
 * <p>
 * The labelling is stored in an array location[j] where
 * where j has the values ON, LEFT, RIGHT
 *
 * @version 1.7
 */
public class TopologyLocation {

    int location[];

    public TopologyLocation(int[] location) {
        this.init(location.length);
    }

    /**
     * Constructs a TopologyLocation specifying how points on, to the left of, and to the
     * right of some GraphComponent relate to some Geometry. Possible values for the
     * parameters are Location.NULL, Location.EXTERIOR, Location.BOUNDARY,
     * and Location.INTERIOR.
     *
     * @see Location
     */
    public TopologyLocation(int on, int left, int right) {
        this.init(3);
        this.location[Position.ON] = on;
        this.location[Position.LEFT] = left;
        this.location[Position.RIGHT] = right;
    }

    public TopologyLocation(int on) {
        this.init(1);
        this.location[Position.ON] = on;
    }

    public TopologyLocation(TopologyLocation gl) {
        this.init(gl.location.length);
        if (gl != null) {
            System.arraycopy(gl.location, 0, location, 0, location.length);
        }
    }

    private void init(int size) {
        this.location = new int[size];
        this.setAllLocations(Location.NONE);
    }

    public int get(int posIndex) {
        if (posIndex < this.location.length) {
            return this.location[posIndex];
        }
        return Location.NONE;
    }

    /**
     * @return true if all locations are NULL
     */
    public boolean isNull() {
        for (int aLocation : location) {
            if (aLocation != Location.NONE) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if any locations are NULL
     */
    public boolean isAnyNull() {
        for (int aLocation : location) {
            if (aLocation == Location.NONE) {
                return true;
            }
        }
        return false;
    }

    public boolean isEqualOnSide(TopologyLocation le, int locIndex) {
        return this.location[locIndex] == le.location[locIndex];
    }

    public boolean isArea() {
        return this.location.length > 1;
    }

    public boolean isLine() {
        return this.location.length == 1;
    }

    public void flip() {
        if (this.location.length <= 1) {
            return;
        }
        int temp = this.location[Position.LEFT];
        this.location[Position.LEFT] = this.location[Position.RIGHT];
        this.location[Position.RIGHT] = temp;
    }

    public void setAllLocations(int locValue) {
        for (int i = 0; i < this.location.length; i++) {
            this.location[i] = locValue;
        }
    }

    public void setAllLocationsIfNull(int locValue) {
        for (int i = 0; i < this.location.length; i++) {
            if (this.location[i] == Location.NONE) {
                this.location[i] = locValue;
            }
        }
    }

    public void setLocation(int locIndex, int locValue) {
        this.location[locIndex] = locValue;
    }

    public void setLocation(int locValue) {
        this.setLocation(Position.ON, locValue);
    }

    public int[] getLocations() {
        return this.location;
    }

    public void setLocations(int on, int left, int right) {
        this.location[Position.ON] = on;
        this.location[Position.LEFT] = left;
        this.location[Position.RIGHT] = right;
    }

    public boolean allPositionsEqual(int loc) {
        for (int aLocation : location) {
            if (aLocation != loc) {
                return false;
            }
        }
        return true;
    }

    /**
     * merge updates only the NULL attributes of this object
     * with the attributes of another.
     */
    public void merge(TopologyLocation gl) {
        // if the src is an Area label & and the dest is not, increase the dest to be an Area
        if (gl.location.length > this.location.length) {
            int[] newLoc = new int[3];
            newLoc[Position.ON] = this.location[Position.ON];
            newLoc[Position.LEFT] = Location.NONE;
            newLoc[Position.RIGHT] = Location.NONE;
            this.location = newLoc;
        }
        for (int i = 0; i < this.location.length; i++) {
            if (this.location[i] == Location.NONE && i < gl.location.length) {
                this.location[i] = gl.location[i];
            }
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (this.location.length > 1) {
            buf.append(Location.toLocationSymbol(this.location[Position.LEFT]));
        }
        buf.append(Location.toLocationSymbol(this.location[Position.ON]));
        if (this.location.length > 1) {
            buf.append(Location.toLocationSymbol(this.location[Position.RIGHT]));
        }
        return buf.toString();
    }
}
