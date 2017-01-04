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

import de.topobyte.osm4j.core.access.OsmElementCounter;
import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

import java.io.IOException;

public class OsmElementCounterReaderAdapter implements OsmElementCounter,
        OsmHandler {

    private OsmReader reader;

    public OsmElementCounterReaderAdapter(OsmReader reader) {
        this.reader = reader;
        reader.setHandler(this);
    }

    private int numNodes = 0;
    private int numWays = 0;
    private int numRelations = 0;

    @Override
    public void handle(OsmBounds bounds) throws IOException {
        // ignore
    }

    @Override
    public void handle(OsmNode node) throws IOException {
        this.numNodes++;
    }

    @Override
    public void handle(OsmWay way) throws IOException {
        this.numWays++;
    }

    @Override
    public void handle(OsmRelation relation) throws IOException {
        this.numRelations++;
    }

    @Override
    public void complete() throws IOException {
        // ignore
    }

    @Override
    public void count() throws OsmInputException {
        this.reader.read();
    }

    @Override
    public long getNumberOfNodes() {
        return this.numNodes;
    }

    @Override
    public long getNumberOfWays() {
        return this.numWays;
    }

    @Override
    public long getNumberOfRelations() {
        return this.numRelations;
    }

    @Override
    public long getTotalNumberOfElements() {
        return this.numNodes + this.numWays + this.numRelations;
    }

    @Override
    public long getNumberOfElements(EntityType type) {
        switch (type) {
            default:
                return 0;
            case Node:
                return this.numNodes;
            case Way:
                return this.numWays;
            case Relation:
                return this.numRelations;
        }
    }
}
