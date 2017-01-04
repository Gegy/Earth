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

import de.topobyte.adt.graph.Graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @param <T> type of nodes in the graph.
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class BreadthFirstEnumerationBuilder<T> implements EnumerationBuilder<T> {

    @Override
    public List<T> buildEnumeration() {
        this.build();
        return this.getEnumeration();
    }

    Graph<T> graph;

    Set<T> enumerated = new HashSet<>();
    List<T> enumeration = new ArrayList<>();
    Set<T> available = new HashSet<>();

    /**
     * Create a new EnumerationBuilder for the denoted graph {@code graph}.
     *
     * @param graph the graph to build an enumeration for.
     */
    public BreadthFirstEnumerationBuilder(Graph<T> graph) {
        this.graph = graph;
    }

    @Override
    public List<T> getEnumeration() {
        return this.enumeration;
    }

    private void build() {
        Collection<T> nodes = this.graph.getNodes();
        for (T node : nodes) {
            this.available.add(node);
        }

        while (true) {
            if (this.available.size() == 0) {
                break;
            }
            T n = this.available.iterator().next();
            // System.out.println("a: " + n);
            this.enumerate(n);
            List<T> neighbours = new ArrayList<>();
            Set<T> neighbourSet = new HashSet<>();
            this.addNeighbours(neighbours, neighbourSet, n);
            while (true) {
                if (neighbourSet.size() == 0) {
                    break;
                }
                T next = this.chooseNext(neighbours, neighbourSet);
                // System.out.println("b: " + next);
                this.enumerate(next);
                this.addNeighbours(neighbours, neighbourSet, next);
            }
        }
    }

    private T chooseNext(List<T> neighbours, Set<T> neighbourSet) {
        // T next = neighbours.iterator().next();
        T next = neighbours.get(0);
        neighbours.remove(0);
        neighbourSet.remove(next);
        return next;
    }

    private void addNeighbours(List<T> neighbours, Set<T> neighbourSet, T n) {
        Set<T> nsNeighbours = this.graph.getEdgesOut(n);
        for (T neighbour : nsNeighbours) {
            if (this.enumerated.contains(neighbour)) {
                continue;
            }
            if (neighbourSet.contains(neighbour)) {
                continue;
            }
            neighbours.add(neighbour);
            neighbourSet.add(neighbour);
        }
    }

    // int i = 0;

    private void enumerate(T n) {
        // i++;
        // System.out.println("enumerated: " + i);
        // System.out.println(available.size());
        this.available.remove(n);
        this.enumerated.add(n);
        this.enumeration.add(n);
    }
}
