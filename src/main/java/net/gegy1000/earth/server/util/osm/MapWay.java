package net.gegy1000.earth.server.util.osm;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.util.osm.tag.Tags;

import java.util.Collection;
import java.util.List;

public class MapWay implements MapObject {
    private final OsmEntityProvider data;

    private final Tags tags;
    private final OsmWay way;
    private final int layer;

    public MapWay(OsmEntityProvider data, Tags tags, OsmWay way) {
        this.data = data;
        this.tags = tags;
        this.way = way;
        this.layer = tags.tag("layer").get(TagType.INTEGER, OSMConstants.DEFAULT_LAYER);
    }

    @Override
    public Tags getTags() {
        return this.tags;
    }

    public OsmWay getWay() {
        return this.way;
    }

    @Override
    public Collection<LineString> toLines() {
        return OpenStreetMap.createLines(this.data, this.way);
    }

    @Override
    public MultiPolygon toArea() {
        return OpenStreetMap.createArea(this.data, this.way);
    }

    @Override
    public List<Point> toPoints() {
        return OpenStreetMap.createPoints(this.data, this.way);
    }

    @Override
    public int getLayer() {
        return this.layer;
    }
}
