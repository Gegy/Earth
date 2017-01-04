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
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * A structure representing all entities except changesets that may be found in
 * a set of osm-data.
 *
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class InMemoryMapDataSet implements OsmEntityProvider {

    private OsmBounds bounds = null;

    private TLongObjectMap<OsmNode> nodes = new TLongObjectHashMap<>();
    private TLongObjectMap<OsmWay> ways = new TLongObjectHashMap<>();
    private TLongObjectMap<OsmRelation> relations = new TLongObjectHashMap<>();

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
     * @return all nodes.
     */
    public TLongObjectMap<OsmNode> getNodes() {
        return this.nodes;
    }

    /**
     * @return all ways.
     */
    public TLongObjectMap<OsmWay> getWays() {
        return this.ways;
    }

    /**
     * @return all relations.
     */
    public TLongObjectMap<OsmRelation> getRelations() {
        return this.relations;
    }

    /**
     * @param nodes set the nodes of this dataset to be these.
     */
    public void setNodes(TLongObjectHashMap<OsmNode> nodes) {
        this.nodes = nodes;
    }

    /**
     * @param ways set the ways of this dataset to be these.
     */
    public void setWays(TLongObjectHashMap<OsmWay> ways) {
        this.ways = ways;
    }

    /**
     * @param relations set the relations of this dataset to be these.
     */
    public void setRelations(TLongObjectHashMap<OsmRelation> relations) {
        this.relations = relations;
    }

    @Override
    public OsmNode getNode(long id) throws EntityNotFoundException {
        OsmNode node = this.nodes.get(id);
        if (node == null) {
            throw new EntityNotFoundException("unable to find node with id: "
                    + id);
        }
        return node;
    }

    @Override
    public OsmWay getWay(long id) throws EntityNotFoundException {
        OsmWay way = this.ways.get(id);
        if (way == null) {
            throw new EntityNotFoundException("unable to find way with id: "
                    + id);
        }
        return way;
    }

    @Override
    public OsmRelation getRelation(long id) throws EntityNotFoundException {
        OsmRelation relation = this.relations.get(id);
        if (relation == null) {
            throw new EntityNotFoundException(
                    "unable to find relation with id: " + id);
        }
        return relation;
    }
}
