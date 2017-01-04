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
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class IdentityCoordinateTransformer implements CoordinateTransformer {

    @Override
    public double getX(double x) {
        return x;
    }

    @Override
    public double getY(double y) {
        return y;
    }
}
