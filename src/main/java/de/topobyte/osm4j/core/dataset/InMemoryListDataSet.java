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

import de.topobyte.osm4j.core.dataset.sort.IdComparator;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InMemoryListDataSet implements OsmEntityProvider {

    private OsmBounds bounds = null;

    private List<OsmNode> nodes = new ArrayList<>();
    private List<OsmWay> ways = new ArrayList<>();
    private List<OsmRelation> relations = new ArrayList<>();

    public boolean hasBounds() {
        return this.bounds != null;
    }

    public OsmBounds getBounds() {
        return this.bounds;
    }

    public void setBounds(OsmBounds bounds) {
        this.bounds = bounds;
    }

    public List<OsmNode> getNodes() {
        return this.nodes;
    }

    public void setNodes(List<OsmNode> nodes) {
        this.nodes = nodes;
    }

    public List<OsmWay> getWays() {
        return this.ways;
    }

    public void setWays(List<OsmWay> ways) {
        this.ways = ways;
    }

    public List<OsmRelation> getRelations() {
        return this.relations;
    }

    public void setRelations(List<OsmRelation> relations) {
        this.relations = relations;
    }

    public void sort() {
        this.sort(new IdComparator());
    }

    public void sort(Comparator<? super OsmEntity> comparator) {
        nodes.sort(comparator);
        ways.sort(comparator);
        relations.sort(comparator);
    }

    public void sort(Comparator<? super OsmNode> nodeComparator,
                     Comparator<? super OsmWay> wayComparator,
                     Comparator<? super OsmRelation> relationComparator) {
        nodes.sort(nodeComparator);
        ways.sort(wayComparator);
        relations.sort(relationComparator);
    }

    @Override
    public OsmNode getNode(long id) throws EntityNotFoundException {
        return this.find(this.nodes, id);
    }

    @Override
    public OsmWay getWay(long id) throws EntityNotFoundException {
        return this.find(this.ways, id);
    }

    @Override
    public OsmRelation getRelation(long id) throws EntityNotFoundException {
        return this.find(this.relations, id);
    }

    private <T extends OsmEntity> T find(List<T> list, long nodeId)
            throws EntityNotFoundException {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            T v = list.get(mid);
            int cmp = Long.compare(v.getId(), nodeId);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return v;
            }
        }
        throw new EntityNotFoundException("element not available in data set");
    }
}
