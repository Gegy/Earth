// Copyright 2015 Sebastian Kuerten
//
// This file is part of jgs.
//
// jgs is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// jgs is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with jgs. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.jgs.transform;

/**
 * A CoordinateTransformer maps from one coordinate space to another.
 *
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public interface CoordinateTransformer {

    /**
     * Convert a source x coordinate to a target x coordinate.
     *
     * @param x the source coordinate.
     * @return the coordinate in target space.
     */
    double getX(double x);

    /**
     * Convert a source y coordinate to a target y coordinate.
     *
     * @param y the source coordinate.
     * @return the coordinate in target space.
     */
    double getY(double y);
}
