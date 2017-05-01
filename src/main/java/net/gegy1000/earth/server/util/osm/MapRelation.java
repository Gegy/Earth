package net.gegy1000.earth.server.util.osm;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.util.osm.tag.Tags;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class MapRelation implements MapObject {
    private final OsmEntityProvider data;

    private final Tags tags;
    private final OsmRelation relation;
    private final Set<OsmWay> ways;
    private final int layer;

    public MapRelation(OsmEntityProvider data, Tags tags, OsmRelation relation, Set<OsmWay> ways) {
        this.data = data;
        this.tags = tags;
        this.relation = relation;
        this.ways = ways;
        this.layer = tags.tag("layer").get(TagType.INTEGER, OSMConstants.DEFAULT_LAYER);
    }

    @Override
    public Tags getTags() {
        return this.tags;
    }

    public OsmRelation getRelation() {
        return this.relation;
    }

    @Override
    public Collection<LineString> toLines() {
        return null;
    }

    @Override
    public MultiPolygon toArea() {
        return OpenStreetMap.createArea(this.data, this.relation);
    }

    @Override
    public List<Point> toPoints() {
        return null;
    }

    @Override
    public int getLayer() {
        return this.layer;
    }

    public Set<OsmWay> getWays() {
        return this.ways;
    }
}
