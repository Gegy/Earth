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

package de.topobyte.osm4j.core.access;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

import java.io.IOException;

public class OsmBridge {

    public static void write(OsmIteratorInput input, OsmStreamOutput output)
            throws IOException {
        write(input.getIterator(), output.getOsmOutput());
        input.close();
        output.close();
    }

    public static void write(OsmReaderInput input, OsmStreamOutput output)
            throws IOException, OsmInputException {
        write(input.getReader(), output.getOsmOutput());
        input.close();
        output.close();
    }

    public static void write(OsmIterator iterator, OsmStreamOutput output)
            throws IOException {
        write(iterator, output.getOsmOutput());
        output.close();
    }

    public static void write(OsmReader reader, OsmStreamOutput output)
            throws IOException, OsmInputException {
        write(reader, output.getOsmOutput());
        output.close();
    }

    public static void write(OsmIteratorInput input, OsmOutputStream output)
            throws IOException {
        write(input.getIterator(), output);
        input.close();
    }

    public static void write(OsmReaderInput input, OsmOutputStream output)
            throws IOException, OsmInputException {
        write(input.getReader(), output);
        input.close();
    }

    public static void write(OsmIterator iterator, OsmOutputStream output)
            throws IOException {
        if (iterator.hasBounds()) {
            output.write(iterator.getBounds());
        }
        while (iterator.hasNext()) {
            EntityContainer container = iterator.next();
            switch (container.getType()) {
                case Node:
                    output.write((OsmNode) container.getEntity());
                    break;
                case Way:
                    output.write((OsmWay) container.getEntity());
                    break;
                case Relation:
                    output.write((OsmRelation) container.getEntity());
                    break;
            }
        }
        output.complete();
    }

    public static void write(OsmReader reader, OsmOutputStream output)
            throws IOException, OsmInputException {
        OsmHandler handler = new OsmOutputStreamHandler(output);
        reader.setHandler(handler);
        reader.read();
    }
}
