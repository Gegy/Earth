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

import com.infomatiq.jsi.Rectangle;

/**
 * @param <T> element type
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
class TraversalAdapter<T> implements com.infomatiq.jsi.rtree.Traversal {

    private GenericRTree<T> tree;
    private Traversal<T> traversal;

    public TraversalAdapter(GenericRTree<T> tree, Traversal<T> traversal) {
        this.tree = tree;
        this.traversal = traversal;
    }

    @Override
    public void element(Rectangle rectangle, int nodeId) {
        T element = this.tree.idToThing.get(nodeId);
        this.traversal.element(rectangle, element);
    }

    @Override
    public void node(Rectangle rectangle) {
        this.traversal.node(rectangle);
    }
}
