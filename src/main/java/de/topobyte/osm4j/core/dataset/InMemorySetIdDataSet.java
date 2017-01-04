// Copyright 2015 Sebastian Kuerten
//
// This file is part of osm4j.
//
// osm4j is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// osm4j is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with osm4j. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.osm4j.core.dataset;

import de.topobyte.osm4j.core.model.iface.OsmBounds;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

/**
 * A structure representing the ids of elements found in a set of osm-data.
 *
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class InMemorySetIdDataSet {

    private OsmBounds bounds = null;

    private TLongSet nodeIds = new TLongHashSet();
    private TLongSet wayIds = new TLongHashSet();
    private TLongSet relationIds = new TLongHashSet();

    public boolean hasBounds() {
        return this.bounds != null;
    }

    public OsmBounds getBounds() {
        return this.bounds;
    }

    public void setBounds(OsmBounds bounds) {
        this.bounds = bounds;
    }

    /**
     * @return all node ids.
     */
    public TLongSet getNodeIds() {
        return this.nodeIds;
    }

    /**
     * @return all way ids.
     */
    public TLongSet getWayIds() {
        return this.wayIds;
    }

    /**
     * @return all relation ids.
     */
    public TLongSet getRelationIds() {
        return this.relationIds;
    }

    /**
     * @param nodeIds set the node ids of this dataset to be these.
     */
    public void setNodeIds(TLongSet nodeIds) {
        this.nodeIds = nodeIds;
    }

    /**
     * @param wayIds set the way ids of this dataset to be these.
     */
    public void setWayIds(TLongSet wayIds) {
        this.wayIds = wayIds;
    }

    /**
     * @param relationIds set the relation ids of this dataset to be these.
     */
    public void setRelationIds(TLongSet relationIds) {
        this.relationIds = relationIds;
    }
}
