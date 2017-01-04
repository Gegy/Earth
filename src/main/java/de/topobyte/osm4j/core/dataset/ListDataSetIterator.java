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

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ListDataSetIterator implements OsmIterator {

    private InMemoryListDataSet data;

    private int index = 0;

    public ListDataSetIterator(InMemoryListDataSet data) {
        this.data = data;
    }

    @Override
    public Iterator<EntityContainer> iterator() {
        return this;
    }

    @Override
    public boolean hasBounds() {
        return this.data.hasBounds();
    }

    @Override
    public OsmBounds getBounds() {
        return this.data.getBounds();
    }

    @Override
    public boolean hasNext() {
        List<OsmNode> nodes = this.data.getNodes();
        List<OsmWay> ways = this.data.getWays();
        List<OsmRelation> relations = this.data.getRelations();
        return this.index < nodes.size() + ways.size() + relations.size();
    }

    @Override
    public EntityContainer next() {
        List<OsmNode> nodes = this.data.getNodes();
        List<OsmWay> ways = this.data.getWays();
        List<OsmRelation> relations = this.data.getRelations();
        if (this.index < nodes.size()) {
            return new EntityContainer(EntityType.Node, nodes.get(this.index++));
        }
        int wayIndex = this.index - nodes.size();
        if (wayIndex < ways.size()) {
            this.index++;
            return new EntityContainer(EntityType.Way, ways.get(wayIndex));
        }
        int relationIndex = wayIndex - ways.size();
        if (relationIndex < relations.size()) {
            this.index++;
            return new EntityContainer(EntityType.Relation,
                    relations.get(relationIndex));
        }
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
