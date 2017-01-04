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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.noding.OrientedCoordinateArray;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A EdgeList is a list of Edges.  It supports locating edges
 * that are pointwise equals to a target edge.
 *
 * @version 1.7
 */
public class EdgeList {
    private List edges = new ArrayList();
    /**
     * An index of the edges, for fast lookup.
     */
    private Map ocaMap = new TreeMap();

    public EdgeList() {
    }

    /**
     * Insert an edge unless it is already in the list
     */
    public void add(Edge e) {
        this.edges.add(e);
        OrientedCoordinateArray oca = new OrientedCoordinateArray(e.getCoordinates());
        this.ocaMap.put(oca, e);
    }

    public void addAll(Collection edgeColl) {
        for (Object anEdgeColl : edgeColl) {
            add((Edge) anEdgeColl);
        }
    }

    public List getEdges() {
        return this.edges;
    }

    /**
     * If there is an edge equal to e already in the list, return it.
     * Otherwise return null.
     *
     * @return equal edge, if there is one already in the list
     * null otherwise
     */
    public Edge findEqualEdge(Edge e) {
        OrientedCoordinateArray oca = new OrientedCoordinateArray(e.getCoordinates());
        // will return null if no edge matches
        Edge matchEdge = (Edge) this.ocaMap.get(oca);
        return matchEdge;
    }

    public Iterator iterator() {
        return this.edges.iterator();
    }

    public Edge get(int i) {
        return (Edge) this.edges.get(i);
    }

    /**
     * If the edge e is already in the list, return its index.
     *
     * @return index, if e is already in the list
     * -1 otherwise
     */
    public int findEdgeIndex(Edge e) {
        for (int i = 0; i < this.edges.size(); i++) {
            if (edges.get(i).equals(e)) {
                return i;
            }
        }
        return -1;
    }

    public void print(PrintStream out) {
        out.print("MULTILINESTRING ( ");
        for (int j = 0; j < this.edges.size(); j++) {
            Edge e = (Edge) this.edges.get(j);
            if (j > 0) {
                out.print(",");
            }
            out.print("(");
            Coordinate[] pts = e.getCoordinates();
            for (int i = 0; i < pts.length; i++) {
                if (i > 0) {
                    out.print(",");
                }
                out.print(pts[i].x + " " + pts[i].y);
            }
            out.println(")");
        }
        out.print(")  ");
    }
}
