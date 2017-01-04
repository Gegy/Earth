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

package de.topobyte.osm4j.xml.dynsax;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is a SAX-based parser for OSM XML data.
 *
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class OsmXmlReader implements OsmReader {

    private OsmHandler handler;

    private boolean parseMetadata;
    private InputStream inputStream;

    public OsmXmlReader(InputStream inputStream, boolean parseMetadata) {
        this.inputStream = inputStream;
        this.parseMetadata = parseMetadata;
    }

    public OsmXmlReader(File file, boolean parseMetadata)
            throws FileNotFoundException {
        InputStream fis = new FileInputStream(file);
        this.inputStream = new BufferedInputStream(fis);
        this.parseMetadata = parseMetadata;
    }

    public OsmXmlReader(String pathname, boolean parseMetadata)
            throws FileNotFoundException {
        this(new File(pathname), parseMetadata);
    }

    @Override
    public void setHandler(OsmHandler handler) {
        this.handler = handler;
    }

    @Override
    public void read() throws OsmInputException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser parser;
        try {
            parser = saxParserFactory.newSAXParser();
        } catch (Exception e) {
            throw new OsmInputException("error while creating xml parser", e);
        }

        OsmSaxHandler saxHandler = OsmSaxHandler.createInstance(this.handler,
                this.parseMetadata);

        try {
            parser.parse(this.inputStream, saxHandler);
        } catch (Exception e) {
            throw new OsmInputException("error while parsing xml data", e);
        }

        try {
            this.handler.complete();
        } catch (IOException e) {
            throw new OsmInputException("error while completing handler", e);
        }
    }
}
