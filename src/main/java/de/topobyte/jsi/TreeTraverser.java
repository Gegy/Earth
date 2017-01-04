//  This file is part of JSI.
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package de.topobyte.jsi;

/**
 * @param <T> element type
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class TreeTraverser<T> {

    private Traversal<T> traversal;

    private GenericRTree<T> tree;

    /**
     * Create a new Traverser for the denoted tree.
     *
     * @param tree the tree to traverse.
     */
    public TreeTraverser(GenericRTree<T> tree, Traversal<T> traversal) {
        this.tree = tree;
        this.traversal = traversal;
    }

    /**
     * Execute the traversal.
     */
    public void traverse() {
        TraversalAdapter<T> adapter = new TraversalAdapter<>(this.tree, this.traversal);
        com.infomatiq.jsi.rtree.TreeTraverser traverser = new com.infomatiq.jsi.rtree.TreeTraverser(
                this.tree.rtree, adapter);
        traverser.traverse();
    }
}
