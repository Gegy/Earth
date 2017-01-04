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

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

import java.io.IOException;

public class ListDataSetOutputStream implements OsmOutputStream {

    private InMemoryListDataSet data = new InMemoryListDataSet();

    public InMemoryListDataSet getData() {
        return this.data;
    }

    @Override
    public void write(OsmBounds bounds) throws IOException {
        this.data.setBounds(bounds);
    }

    @Override
    public void write(OsmNode node) throws IOException {
        this.data.getNodes().add(node);
    }

    @Override
    public void write(OsmWay way) throws IOException {
        this.data.getWays().add(way);
    }

    @Override
    public void write(OsmRelation relation) throws IOException {
        this.data.getRelations().add(relation);
    }

    @Override
    public void complete() throws IOException {
        // nothing to do here
    }
}
