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

package de.topobyte.osm4j.core.model.iface;

public class IdContainer {

    private EntityType type;
    private long id;

    public IdContainer(EntityType type, long id) {
        this.type = type;
        this.id = id;
    }

    public EntityType getType() {
        return this.type;
    }

    public long getId() {
        return this.id;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public void setId(long id) {
        this.id = id;
    }
}
