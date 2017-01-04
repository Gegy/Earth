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

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

import java.io.IOException;

public class ListDataSetReader implements OsmReader {

    private InMemoryListDataSet data;
    private OsmHandler handler;

    public ListDataSetReader(InMemoryListDataSet data) {
        this.data = data;
    }

    @Override
    public void setHandler(OsmHandler handler) {
        this.handler = handler;
    }

    @Override
    public void read() throws OsmInputException {
        try {
            if (this.data.hasBounds()) {
                this.handler.handle(this.data.getBounds());
            }
            for (OsmNode node : this.data.getNodes()) {
                this.handler.handle(node);
            }
            for (OsmWay way : this.data.getWays()) {
                this.handler.handle(way);
            }
            for (OsmRelation relation : this.data.getRelations()) {
                this.handler.handle(relation);
            }
            this.handler.complete();
        } catch (IOException e) {
            throw new OsmInputException(e);
        }
    }
}
