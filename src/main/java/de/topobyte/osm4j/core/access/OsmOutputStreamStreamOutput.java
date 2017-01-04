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

import java.io.IOException;
import java.io.OutputStream;

public class OsmOutputStreamStreamOutput implements OsmStreamOutput {

    private OutputStream output;
    private OsmOutputStream osmOutput;

    public OsmOutputStreamStreamOutput(OutputStream output,
                                       OsmOutputStream osmOutput) {
        this.output = output;
        this.osmOutput = osmOutput;
    }

    public OutputStream getOutputStream() {
        return this.output;
    }

    @Override
    public OsmOutputStream getOsmOutput() {
        return this.osmOutput;
    }

    @Override
    public void close() throws IOException {
        this.output.close();
    }
}
