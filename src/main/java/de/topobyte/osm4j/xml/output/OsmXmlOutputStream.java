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

package de.topobyte.osm4j.xml.output;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OsmXmlOutputStream implements OsmOutputStream {

    private final PrintWriter out;
    private final boolean printMetadata;

    public OsmXmlOutputStream(PrintWriter out, boolean printMetadata) {
        this.out = out;
        this.printMetadata = printMetadata;
        this.writeHeader();
    }

    public OsmXmlOutputStream(OutputStream os, boolean printMetadata) {
        this(new PrintWriter(os), printMetadata);
    }

    private void writeHeader() {
        this.out.println("<?xml version='1.0' encoding='UTF-8'?>");
        this.out.println("<osm version=\"0.6\">");
    }

    @Override
    public void complete() {
        this.out.println("</osm>");
        this.out.flush();
    }

    private DecimalFormat f = new DecimalFormat("0.#######;-0.#######",
            new DecimalFormatSymbols(Locale.US));

    private DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // private CharSequenceTranslator escaper = new LookupTranslator(
    // EntityArrays.BASIC_ESCAPE());

    private CharSequenceTranslator escaper = StringEscapeUtils.ESCAPE_XML;

    private String templateBounds = "  <bounds minlon=\"%f\" minlat=\"%f\" maxlon=\"%f\" maxlat=\"%f\"/>";

    @Override
    public void write(OsmBounds bounds) throws IOException {
        this.out.println(String.format(this.templateBounds, bounds.getLeft(),
                bounds.getBottom(), bounds.getRight(), bounds.getTop()));
    }

    @Override
    public void write(OsmNode node) {
        this.out.print("  <node id=\"" + node.getId() + "\"");
        this.out.print(" lat=\"" + this.f.format(node.getLatitude()) + "\"");
        this.out.print(" lon=\"" + this.f.format(node.getLongitude()) + "\"");
        if (this.printMetadata) {
            OsmMetadata metadata = node.getMetadata();
            this.printMetadata(metadata);
        }
        if (node.getNumberOfTags() == 0) {
            this.out.println("/>");
        } else {
            this.out.println(">");
            this.printTags(node);
            this.out.println("  </node>");
        }
    }

    @Override
    public void write(OsmWay way) {
        this.out.print("  <way id=\"" + way.getId() + "\"");
        if (this.printMetadata) {
            OsmMetadata metadata = way.getMetadata();
            this.printMetadata(metadata);
        }
        if (way.getNumberOfTags() == 0 && way.getNumberOfNodes() == 0) {
            this.out.println("/>");
        } else {
            this.out.println(">");
            for (int i = 0; i < way.getNumberOfNodes(); i++) {
                long nodeId = way.getNodeId(i);
                this.out.println("    <nd ref=\"" + nodeId + "\"/>");
            }
            this.printTags(way);
            this.out.println("  </way>");
        }
    }

    @Override
    public void write(OsmRelation relation) {
        this.out.print("  <relation id=\"" + relation.getId() + "\"");
        if (this.printMetadata) {
            OsmMetadata metadata = relation.getMetadata();
            this.printMetadata(metadata);
        }
        if (relation.getNumberOfTags() == 0
                && relation.getNumberOfMembers() == 0) {
            this.out.println("/>");
        } else {
            this.out.println(">");
            for (int i = 0; i < relation.getNumberOfMembers(); i++) {
                OsmRelationMember member = relation.getMember(i);
                EntityType type = member.getType();
                String t = type == EntityType.Node ? "node"
                        : type == EntityType.Way ? "way" : "relation";
                String role = member.getRole();
                role = StringEscapeUtils.escapeXml(role);
                this.out.println("    <member type=\"" + t + "\" ref=\""
                        + member.getId() + "\" role=\"" + role + "\"/>");
            }
            this.printTags(relation);
            this.out.println("  </relation>");
        }
    }

    private void printMetadata(OsmMetadata metadata) {
        if (metadata == null) {
            return;
        }
        this.out.print(" version=\"" + metadata.getVersion() + "\"");
        this.out.print(" timestamp=\"" + this.formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(metadata.getTimestamp()), ZoneId.systemDefault()))
                + "\"");
        if (metadata.getUid() >= 0) {
            this.out.print(" uid=\"" + metadata.getUid() + "\"");
            String user = metadata.getUser();
            if (user != null) {
                user = this.escaper.translate(user);
            }
            this.out.print(" user=\"" + user + "\"");
        }
        this.out.print(" changeset=\"" + metadata.getChangeset() + "\"");
    }

    private void printTags(OsmEntity entity) {
        for (int i = 0; i < entity.getNumberOfTags(); i++) {
            OsmTag tag = entity.getTag(i);
            String key = tag.getKey();
            String value = tag.getValue();
            key = this.escaper.translate(key);
            value = this.escaper.translate(value);
            this.out.println("    <tag k=\"" + key + "\" v=\"" + value + "\"/>");
        }
    }
}
