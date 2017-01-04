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

package de.topobyte.osm4j.core.access.wrapper;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmIdHandler;
import de.topobyte.osm4j.core.access.OsmIdReader;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

import java.io.IOException;

public class OsmIdReaderAdapter implements OsmIdReader, OsmHandler {

    private OsmReader reader;
    private OsmIdHandler handler;

    public OsmIdReaderAdapter(OsmReader reader) {
        this.reader = reader;
        reader.setHandler(this);
    }

    @Override
    public void setIdHandler(OsmIdHandler handler) {
        this.handler = handler;
    }

    @Override
    public void read() throws OsmInputException {
        this.reader.read();
    }

    @Override
    public void handle(OsmBounds bounds) throws IOException {
        this.handler.handle(bounds);
    }

    @Override
    public void handle(OsmNode node) throws IOException {
        this.handler.handleNode(node.getId());
    }

    @Override
    public void handle(OsmWay way) throws IOException {
        this.handler.handleWay(way.getId());
    }

    @Override
    public void handle(OsmRelation relation) throws IOException {
        this.handler.handleRelation(relation.getId());
    }

    @Override
    public void complete() throws IOException {
        this.handler.complete();
    }
}
