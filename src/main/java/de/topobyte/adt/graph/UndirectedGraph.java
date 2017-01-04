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

package de.topobyte.adt.graph;

/**
 * A class to represent an unweighted, undirected graph.
 *
 * @param <T> the type of node elements.
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class UndirectedGraph<T> extends Graph<T> {

    /**
     * Add an undirected edge to the graph.
     *
     * @param a a node.
     * @param b another node.
     */
    @Override
    public void addEdge(T a, T b) {
        super.addEdge(a, b);
        super.addEdge(b, a);
    }

    /**
     * Remove an undirected edge from graph.
     *
     * @param a a node.
     * @param b another node.
     */
    @Override
    public void removeEdge(T a, T b) {
        super.removeEdge(a, b);
        super.removeEdge(b, a);
    }
}
