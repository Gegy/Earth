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
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.core.model.impl.Entity;
import de.topobyte.osm4j.core.model.impl.Metadata;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.RelationMember;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.xml.dynsax.Child;
import de.topobyte.xml.dynsax.ChildType;
import de.topobyte.xml.dynsax.Data;
import de.topobyte.xml.dynsax.DynamicSaxHandler;
import de.topobyte.xml.dynsax.Element;
import de.topobyte.xml.dynsax.ParsingException;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

class OsmSaxHandler extends DynamicSaxHandler {

    static OsmSaxHandler createInstance(boolean parseMetadata) {
        return new OsmSaxHandler(null, parseMetadata);
    }

    static OsmSaxHandler createInstance(OsmHandler handler,
                                        boolean parseMetadata) {
        return new OsmSaxHandler(handler, parseMetadata);
    }

    private OsmHandler handler;
    private boolean parseMetadata;
    private DateParser dateParser;

    private OsmSaxHandler(OsmHandler handler, boolean parseMetadata) {
        this.handler = handler;
        this.parseMetadata = parseMetadata;
        if (parseMetadata) {
            this.dateParser = new DateParser();
        }
        this.setRoot(this.createRoot(), true);
    }

    void setHandler(OsmHandler handler) {
        this.handler = handler;
    }

    private Element root, bounds, bound, node, way, relation;

    private static final String NAME_OSM = "osm";
    private static final String NAME_BOUNDS = "bounds";
    private static final String NAME_BOUND = "bound";
    private static final String NAME_NODE = "node";
    private static final String NAME_WAY = "way";
    private static final String NAME_RELATION = "relation";
    private static final String NAME_TAG = "tag";
    private static final String NAME_ND = "nd";
    private static final String NAME_MEMBER = "member";

    private static final String ATTR_MIN_LAT = "minlat";
    private static final String ATTR_MAX_LAT = "maxlat";
    private static final String ATTR_MIN_LON = "minlon";
    private static final String ATTR_MAX_LON = "maxlon";
    private static final String ATTR_BOX = "box";

    private static final String ATTR_ID = "id";
    private static final String ATTR_K = "k";
    private static final String ATTR_V = "v";
    private static final String ATTR_LON = "lon";
    private static final String ATTR_LAT = "lat";
    private static final String ATTR_REF = "ref";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_ROLE = "role";

    private static final String ATTR_VERSION = "version";
    private static final String ATTR_TIMESTAMP = "timestamp";
    private static final String ATTR_UID = "uid";
    private static final String ATTR_USER = "user";
    private static final String ATTR_CHANGESET = "changeset";

    private Element createRoot() {
        this.root = new Element(NAME_OSM, false);

        // the bounds elements

        this.bounds = new Element(NAME_BOUNDS, false);
        this.bounds.addAttribute(ATTR_MIN_LON);
        this.bounds.addAttribute(ATTR_MAX_LON);
        this.bounds.addAttribute(ATTR_MIN_LAT);
        this.bounds.addAttribute(ATTR_MAX_LAT);

        // the bound element

        this.bound = new Element(NAME_BOUND, false);
        this.bound.addAttribute(ATTR_BOX);

        // the 3 basic types

        this.node = new Element(NAME_NODE, false);
        this.node.addAttribute(ATTR_ID);
        this.node.addAttribute(ATTR_LON);
        this.node.addAttribute(ATTR_LAT);

        this.way = new Element(NAME_WAY, false);
        this.way.addAttribute(ATTR_ID);

        this.relation = new Element(NAME_RELATION, false);
        this.relation.addAttribute(ATTR_ID);

        List<Element> entities = new ArrayList<>();
        entities.add(this.node);
        entities.add(this.way);
        entities.add(this.relation);

        // add to root element

        this.root.addChild(new Child(this.bounds, ChildType.IGNORE, true));
        this.root.addChild(new Child(this.bound, ChildType.IGNORE, true));
        this.root.addChild(new Child(this.node, ChildType.IGNORE, true));
        this.root.addChild(new Child(this.way, ChildType.IGNORE, true));
        this.root.addChild(new Child(this.relation, ChildType.IGNORE, true));

        // tag for each type

        Element tag = new Element(NAME_TAG, false);
        tag.addAttribute(ATTR_K);
        tag.addAttribute(ATTR_V);

        for (Element element : entities) {
            element.addChild(new Child(tag, ChildType.LIST, false));
        }

        // meta attributes for each type
        if (this.parseMetadata) {
            for (Element element : entities) {
                element.addAttribute(ATTR_VERSION);
                element.addAttribute(ATTR_TIMESTAMP);
                element.addAttribute(ATTR_UID);
                element.addAttribute(ATTR_USER);
                element.addAttribute(ATTR_CHANGESET);
            }
        }

        // way members

        Element nd = new Element(NAME_ND, false);
        nd.addAttribute(ATTR_REF);
        this.way.addChild(new Child(nd, ChildType.LIST, false));

        // relation members

        Element member = new Element(NAME_MEMBER, false);
        member.addAttribute(ATTR_TYPE);
        member.addAttribute(ATTR_REF);
        member.addAttribute(ATTR_ROLE);
        this.relation.addChild(new Child(member, ChildType.LIST, false));

        return this.root;
    }

    @Override
    public void emit(Data data) throws ParsingException {
        if (data.getElement() == this.bounds) {
            String aMinLon = data.getAttribute(ATTR_MIN_LON);
            String aMaxLon = data.getAttribute(ATTR_MAX_LON);
            String aMinLat = data.getAttribute(ATTR_MIN_LAT);
            String aMaxLat = data.getAttribute(ATTR_MAX_LAT);
            double minLon = Double.parseDouble(aMinLon);
            double minLat = Double.parseDouble(aMinLat);
            double maxLon = Double.parseDouble(aMaxLon);
            double maxLat = Double.parseDouble(aMaxLat);
            try {
                this.handler.handle(new Bounds(minLon, maxLon, maxLat, minLat));
            } catch (IOException e) {
                throw new ParsingException("while handling bounds", e);
            }
        }

        if (data.getElement() == this.bound) {
            String aBox = data.getAttribute(ATTR_BOX);
            String[] parts = aBox.split(",");
            if (parts.length == 4) {
                double minLat = Double.parseDouble(parts[0]);
                double minLon = Double.parseDouble(parts[1]);
                double maxLat = Double.parseDouble(parts[2]);
                double maxLon = Double.parseDouble(parts[3]);
                try {
                    this.handler.handle(new Bounds(minLon, maxLon, maxLat, minLat));
                } catch (IOException e) {
                    throw new ParsingException("while handling bounds", e);
                }
            }
        }

        OsmMetadata metadata = null;
        if (this.parseMetadata) {
            String aVersion = data.getAttribute(ATTR_VERSION);
            String aTimestamp = data.getAttribute(ATTR_TIMESTAMP);
            String aUid = data.getAttribute(ATTR_UID);
            String user = data.getAttribute(ATTR_USER);
            String aChangeset = data.getAttribute(ATTR_CHANGESET);

            long uid = -1;
            if (aUid != null) {
                uid = Long.parseLong(aUid);
            }

            if (user == null) {
                user = "";
            }

            int version = -1;
            if (aVersion != null) {
                version = Integer.parseInt(aVersion);
            }

            long changeset = -1;
            if (aChangeset != null) {
                changeset = Long.parseLong(aChangeset);
            }

            long timestamp = -1;
            if (aTimestamp != null) {
                LocalDateTime date = this.dateParser.parse(aTimestamp);
                timestamp = date.toInstant(ZoneOffset.UTC).toEpochMilli();
            }

            metadata = new Metadata(version, timestamp, uid, user, changeset);
        }

        if (data.getElement() == this.node) {
            String aId = data.getAttribute(ATTR_ID);
            String aLon = data.getAttribute(ATTR_LON);
            String aLat = data.getAttribute(ATTR_LAT);

            long id = Long.parseLong(aId);
            double lon = Double.parseDouble(aLon);
            double lat = Double.parseDouble(aLat);

            Node node = new Node(id, lon, lat, metadata);
            this.fillTags(node, data);

            try {
                this.handler.handle(node);
            } catch (IOException e) {
                throw new ParsingException("while handling node", e);
            }
        } else if (data.getElement() == this.way) {
            String aId = data.getAttribute(ATTR_ID);
            long id = Long.parseLong(aId);

            TLongList nodes = new TLongArrayList();
            List<Data> nds = data.getList(NAME_ND);
            if (nds != null) {
                for (Data nd : nds) {
                    String aRef = nd.getAttribute(ATTR_REF);
                    long ref = Long.parseLong(aRef);
                    nodes.add(ref);
                }
            }

            Way way = new Way(id, nodes, metadata);
            this.fillTags(way, data);

            try {
                this.handler.handle(way);
            } catch (IOException e) {
                throw new ParsingException("while handling way", e);
            }
        } else if (data.getElement() == this.relation) {
            String aId = data.getAttribute(ATTR_ID);
            long id = Long.parseLong(aId);

            List<OsmRelationMember> members = new ArrayList<>();

            List<Data> memberDs = data.getList(NAME_MEMBER);
            if (memberDs != null) {
                for (Data memberD : memberDs) {
                    String aType = memberD.getAttribute(ATTR_TYPE);
                    String aRef = memberD.getAttribute(ATTR_REF);
                    String role = memberD.getAttribute(ATTR_ROLE);

                    long ref = Long.parseLong(aRef);

                    EntityType type = null;
                    switch (aType) {
                        case "node":
                            type = EntityType.Node;
                            break;
                        case "way":
                            type = EntityType.Way;
                            break;
                        case "relation":
                            type = EntityType.Relation;
                            break;
                    }

                    RelationMember member = new RelationMember(ref, type, role);
                    members.add(member);
                }
            }

            Relation relation = new Relation(id, members, metadata);
            this.fillTags(relation, data);

            try {
                this.handler.handle(relation);
            } catch (IOException e) {
                throw new ParsingException("while handling relation", e);
            }
        }
    }

    private void fillTags(Entity entity, Data data) {
        List<Data> list = data.getList(NAME_TAG);
        if (list == null) {
            return;
        }

        List<OsmTag> tags = new ArrayList<>();
        for (Data child : list) {
            String k = child.getAttribute(ATTR_K);
            String v = child.getAttribute(ATTR_V);
            tags.add(new Tag(k, v));
        }
        entity.setTags(tags);
    }
}
