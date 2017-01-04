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

package de.topobyte.osm4j.core.model.impl;

import de.topobyte.osm4j.core.model.iface.OsmMetadata;

public class Metadata implements OsmMetadata {

    private int version;
    private long timestamp;
    private long uid;
    private String user;
    private long changeset;

    public Metadata(int version, long timestamp, long uid, String user,
                    long changeset) {
        this.version = version;
        this.timestamp = timestamp;
        this.uid = uid;
        this.user = user;
        this.changeset = changeset;
    }

    @Override
    public int getVersion() {
        return this.version;
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public long getUid() {
        return this.uid;
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public long getChangeset() {
        return this.changeset;
    }
}
