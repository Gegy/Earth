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
package com.vividsolutions.jts.index.quadtree;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;

/**
 * Represents a node of a {@link Quadtree}.  Nodes contain
 * items which have a spatial extent corresponding to the node's position
 * in the quadtree.
 *
 * @version 1.7
 */
public class Node
        extends NodeBase {
    public static Node createNode(Envelope env) {
        Key key = new Key(env);
        Node node = new Node(key.getEnvelope(), key.getLevel());
        return node;
    }

    public static Node createExpanded(Node node, Envelope addEnv) {
        Envelope expandEnv = new Envelope(addEnv);
        if (node != null) {
            expandEnv.expandToInclude(node.env);
        }

        Node largerNode = createNode(expandEnv);
        if (node != null) {
            largerNode.insertNode(node);
        }
        return largerNode;
    }

    private Envelope env;
    private double centrex;
    private double centrey;
    private int level;

    public Node(Envelope env, int level) {
        //this.parent = parent;
        this.env = env;
        this.level = level;
        this.centrex = (env.getMinX() + env.getMaxX()) / 2;
        this.centrey = (env.getMinY() + env.getMaxY()) / 2;
    }

    public Envelope getEnvelope() {
        return this.env;
    }

    @Override
    protected boolean isSearchMatch(Envelope searchEnv) {
        return this.env.intersects(searchEnv);
    }

    /**
     * Returns the subquad containing the envelope <tt>searchEnv</tt>.
     * Creates the subquad if
     * it does not already exist.
     *
     * @return the subquad containing the search envelope
     */
    public Node getNode(Envelope searchEnv) {
        int subnodeIndex = getSubnodeIndex(searchEnv, this.centrex, this.centrey);
        // if subquadIndex is -1 searchEnv is not contained in a subquad
        if (subnodeIndex != -1) {
            // create the quad if it does not exist
            Node node = this.getSubnode(subnodeIndex);
            // recursively search the found/created quad
            return node.getNode(searchEnv);
        } else {
            return this;
        }
    }

    /**
     * Returns the smallest <i>existing</i>
     * node containing the envelope.
     */
    public NodeBase find(Envelope searchEnv) {
        int subnodeIndex = getSubnodeIndex(searchEnv, this.centrex, this.centrey);
        if (subnodeIndex == -1) {
            return this;
        }
        if (this.subnode[subnodeIndex] != null) {
            // query lies in subquad, so search it
            Node node = this.subnode[subnodeIndex];
            return node.find(searchEnv);
        }
        // no existing subquad, so return this one anyway
        return this;
    }

    void insertNode(Node node) {
        Assert.isTrue(this.env == null || this.env.contains(node.env));
//System.out.println(env);
//System.out.println(quad.env);
        int index = getSubnodeIndex(node.env, this.centrex, this.centrey);
//System.out.println(index);
        if (node.level == this.level - 1) {
            this.subnode[index] = node;
//System.out.println("inserted");
        } else {
            // the quad is not a direct child, so make a new child quad to contain it
            // and recursively insert the quad
            Node childNode = this.createSubnode(index);
            childNode.insertNode(node);
            this.subnode[index] = childNode;
        }
    }

    /**
     * get the subquad for the index.
     * If it doesn't exist, create it
     */
    private Node getSubnode(int index) {
        if (this.subnode[index] == null) {
            this.subnode[index] = this.createSubnode(index);
        }
        return this.subnode[index];
    }

    private Node createSubnode(int index) {
        // create a new subquad in the appropriate quadrant

        double minx = 0.0;
        double maxx = 0.0;
        double miny = 0.0;
        double maxy = 0.0;

        switch (index) {
            case 0:
                minx = this.env.getMinX();
                maxx = this.centrex;
                miny = this.env.getMinY();
                maxy = this.centrey;
                break;
            case 1:
                minx = this.centrex;
                maxx = this.env.getMaxX();
                miny = this.env.getMinY();
                maxy = this.centrey;
                break;
            case 2:
                minx = this.env.getMinX();
                maxx = this.centrex;
                miny = this.centrey;
                maxy = this.env.getMaxY();
                break;
            case 3:
                minx = this.centrex;
                maxx = this.env.getMaxX();
                miny = this.centrey;
                maxy = this.env.getMaxY();
                break;
        }
        Envelope sqEnv = new Envelope(minx, maxx, miny, maxy);
        Node node = new Node(sqEnv, this.level - 1);
        return node;
    }
}
