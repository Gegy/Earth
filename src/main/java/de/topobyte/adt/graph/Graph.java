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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A class to represent an unweighted directed graph.
 *
 * @param <T> the type of node elements.
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class Graph<T> {

    private Set<T> nodes = new HashSet<>();
    private Map<T, Set<T>> edgesOut = new HashMap<>();
    private Map<T, Set<T>> edgesIn = new HashMap<>();

    /**
     * Add a node to the graph.
     *
     * @param node the node to add.
     */
    public void addNode(T node) {
        this.nodes.add(node);
        this.edgesOut.put(node, new HashSet<>());
        this.edgesIn.put(node, new HashSet<>());
    }

    /**
     * Remove a node from the graph.
     *
     * @param node the node to remove.
     */
    public void removeNode(T node) {
        this.nodes.remove(node);
        Set<T> in = this.edgesIn.get(node);
        Set<T> out = this.edgesOut.get(node);
        this.edgesIn.remove(node);
        this.edgesOut.remove(node);
        for (T other : out) {
            this.edgesIn.get(other).remove(node);
        }
        for (T other : in) {
            this.edgesOut.get(other).remove(node);
        }
    }

    /**
     * Remove a collection of nodes from the graph.
     *
     * @param toRemove the nodes to remove.
     */
    public void removeAllNodes(Collection<T> toRemove) {
        for (T node : toRemove) {
            this.removeNode(node);
        }
    }

    /**
     * Add all these nodes to the graph.
     *
     * @param nodesToAdd a set of nodes to add.
     */
    public void addNodes(Set<T> nodesToAdd) {
        for (T node : nodesToAdd) {
            this.addNode(node);
        }
    }

    /**
     * Add this edge to the graph.
     *
     * @param a a node.
     * @param b another node.
     */
    public void addEdge(T a, T b) {
        this.edgesOut.get(a).add(b);
        this.edgesIn.get(b).add(a);
    }

    /**
     * Remove this edge from graph.
     *
     * @param a a node.
     * @param b another node.
     */
    public void removeEdge(T a, T b) {
        this.edgesOut.get(a).remove(b);
        this.edgesIn.get(b).remove(a);
    }

    /**
     * Return the nodes of this graph.
     *
     * @return the set of nodes.
     */
    public Collection<T> getNodes() {
        return this.nodes;
    }

    /**
     * Get all outgoing edges (the nodes that can be reached via these edges).
     *
     * @param node the node whose edges to get.
     * @return a set of reachable nodes.
     */
    public Set<T> getEdgesOut(T node) {
        return this.edgesOut.get(node);
    }

    /**
     * Get all incoming edges (the nodes that can be reached via these edges).
     *
     * @param node the node whose incoming edges to get.
     * @return a set of nodes that can reach this one.
     */
    public Set<T> getEdgesIn(T node) {
        return this.edgesIn.get(node);
    }

    /**
     * Get the number of incoming edges.
     *
     * @param node the node.
     * @return the number of incoming edges.
     */
    public int degreeIn(T node) {
        return this.getEdgesIn(node).size();
    }

    /**
     * Get the number of outgoing edges.
     *
     * @param node the node.
     * @return the number of outgoing edges.
     */
    public int degreeOut(T node) {
        return this.getEdgesOut(node).size();
    }

    /**
     * Get a partition of the graph. Each subset consist of a set of nodes that
     * are connected. This currently expects the graph to be undirected.
     *
     * @return a partition of the graph.
     */
    public Set<Set<T>> getPartition() {
        Set<Set<T>> partition = new HashSet<>();

        Set<T> left = new HashSet<>();
        left.addAll(this.nodes);
        while (left.size() > 0) {
            T next = left.iterator().next();
            left.remove(next);
            Set<T> connected = new HashSet<>();
            connected.add(next);
            Set<T> out = this.getReachable(next);
            for (T o : out) {
                connected.add(o);
                left.remove(o);
            }
            partition.add(connected);
        }

        return partition;
    }

    private Set<T> getReachable(T node) {
        Set<T> reachable = new HashSet<>();
        Set<T> check = new HashSet<>();
        check.add(node);
        while (check.size() > 0) {
            T next = check.iterator().next();
            check.remove(next);
            reachable.add(next);

            Set<T> out = this.getEdgesOut(next);
            for (T o : out) {
                if (reachable.contains(o)) {
                    continue;
                }
                if (check.contains(o)) {
                    continue;
                }
                check.add(o);
            }
        }
        return reachable;
    }
}
