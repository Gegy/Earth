package net.gegy1000.earth.server.util.osm.object;

import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.MapMaterial;
import net.gegy1000.earth.server.util.osm.tag.TagHandler;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class MapObject {
    protected Set<MapObject> relations = new HashSet<>();
    protected int layer;
    protected IBlockState surface;
    protected IBlockState material;

    public MapObject(Map<String, String> tags) {
        this.layer = TagHandler.getTop(TagType.INTEGER, tags, 1, "layer");
        String surface = TagHandler.getFull(TagType.STRING, tags, null, "surface");
        String material = TagHandler.getTop(TagType.STRING, tags, null, "material");
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
