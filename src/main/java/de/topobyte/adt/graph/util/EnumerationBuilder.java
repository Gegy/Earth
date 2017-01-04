// Copyright 2015 Sebastian Kuerten
//
// This file is part of adt-graph.
//
// adt-graph is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// adt-graph is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with adt-graph. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.adt.graph.util;

import java.util.List;

/**
 * @param <T> type of nodes in the graph.
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public interface EnumerationBuilder<T> {

    /**
     * Builds an enumeration of the nodes in the graph. This method will build a
     * list containing all nodes of the graph such that algorithms may work on
     * the nodes of the graph sequentially.
     *
     * @return an enumeration of the nodes in the graph.
     */
    List<T> buildEnumeration();

    /**
     * Get the enumeration previously build without any computation overhead.
     *
     * @return the enumeration generated with a call to
     * {@link #buildEnumeration()} before.
     */
    List<T> getEnumeration();
}
