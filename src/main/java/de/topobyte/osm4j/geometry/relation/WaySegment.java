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

package de.topobyte.osm4j.geometry.relation;

import de.topobyte.osm4j.core.model.iface.OsmWay;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WaySegment {

    final static Logger logger = LogManager.getLogger(WaySegment.class);

    private OsmWay way;
    private boolean reverse;

    public WaySegment(OsmWay way, boolean reverse) {
        this.way = way;
        this.reverse = reverse;
    }

    public OsmWay getWay() {
        return this.way;
    }

    public boolean isReverse() {
        return this.reverse;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WaySegment) {
            WaySegment other = (WaySegment) o;
            return other.getWay().equals(this.way);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) this.way.getId();
    }

    public int getNumberOfNodes() {
        return this.way.getNumberOfNodes();
    }

    public long getNodeId(int n) {
        if (!this.reverse) {
            return this.way.getNodeId(n);
        } else {
            return this.way.getNodeId(this.way.getNumberOfNodes() - 1 - n);
        }
    }
}
