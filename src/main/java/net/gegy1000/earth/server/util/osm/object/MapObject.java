package net.gegy1000.earth.server.util.osm.object;

import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.MapMaterial;
import net.gegy1000.earth.server.util.osm.WayType;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;

import java.util.HashSet;
import java.util.Set;

public abstract class MapObject {
    protected Set<MapObject> relations = new HashSet<>();
    protected int layer;
    protected IBlockState surface;
    protected IBlockState material;

    public MapObject(Tags tags) {
        this.layer = tags.top("layer").get(TagType.INTEGER, 1);
        String surface = tags.tag("surface").get(TagType.STRING, null);
        String material = tags.tag("material").get(TagType.STRING, null);
        if (surface != null) {
            switch (this.getSurfaceType()) {
                case NORMAL:
                    this.surface = MapMaterial.get(surface, null);
                    break;
                case BARRIER:
                    this.surface = MapMaterial.getBarrier(surface, null);
                    break;
                case STAIRS:
                    this.surface = MapMaterial.getStairs(surface, null);
                    break;
            }
        }
        this.material = MapMaterial.get(material, null);
    }

    public abstract void generate(EarthGenerator generator, MapBlockAccess storage, int pass);

    public abstract MapObjectType getType();

    public abstract WayType getWayType();

    public int getLayer() {
        return this.layer;
    }

    public void init(Set<MapObject> relations) {
        this.relations = relations;
    }

    public SurfaceType getSurfaceType() {
        return SurfaceType.NORMAL;
    }

    public enum SurfaceType {
        NORMAL,
        BARRIER,
        STAIRS
    }
}
