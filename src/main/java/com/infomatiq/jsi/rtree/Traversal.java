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

package com.infomatiq.jsi.rtree;

import com.infomatiq.jsi.Rectangle;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public interface Traversal {

    /**
     * Method gets called for each element contained in the tree.
     *
     * @param rectangle the minimal bounding box of the element.
     * @param nodeId the id of the element.
     */
    void element(Rectangle rectangle, int nodeId);

    /**
     * Method gets called for each inner node in the tree.
     *
     * @param rectangle the minimal bounding box of the node.
     */
    void node(Rectangle rectangle);
}
