package net.gegy1000.earth.server.util.osm.object.area;

import com.vividsolutions.jts.geom.Geometry;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.OSMConstants;
import net.gegy1000.earth.server.util.osm.object.MapObjectType;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.util.raster.Rasterize;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Set;

public class BuildingPart extends Area {
    protected IBlockState surface;
    protected int minHeight;
    protected int height;
    protected int levels;
    protected int minLevel;
    protected boolean hasMinLevel = true;

    public BuildingPart(EarthGenerator generator, Geometry geometry, Tags tags) {
        super(geometry, tags);
        this.levels = tags.top("levels").get(TagType.INTEGER, 1);
        this.minLevel = tags.top("min_level").get(TagType.INTEGER, Integer.MIN_VALUE);
        if (this.minLevel == Integer.MIN_VALUE) {
            this.hasMinLevel = false;
            this.minLevel = 0;
        }
        double scaleRatio = generator.getScaleRatio();
        double defaultHeight = this.levels * OSMConstants.LEVEL_HEIGHT;
        this.height = MathHelper.ceil(Math.max(OSMConstants.LEVEL_HEIGHT, tags.top("height").get(TagType.DOUBLE, defaultHeight) / scaleRatio));
        this.minHeight = MathHelper.ceil(tags.top("min_height").get(TagType.DOUBLE, 0.0) / scaleRatio);
        if (this.surface == null) {
            this.surface = Blocks.QUARTZ_BLOCK.getDefaultState();
        }
    }

    @Override
    public void generate(EarthGenerator generator, MapBlockAccess storage, int pass) {
        if (pass == 0) {
            IBlockState state = this.material != null ? this.material : Blocks.QUARTZ_BLOCK.getDefaultState();
            Rasterize.AreaWithOutline rasterized = Rasterize.areaOutline(generator, this.geometry);
            Set<BlockPos> rasterizedOutline = rasterized.getOutline();
            Set<BlockPos> rasterizedArea = rasterized.getArea();
            for (BlockPos pos : rasterizedOutline) {
                int y = generator.getGenerationHeight(pos.getX(), pos.getZ()) + 1 + this.minHeight;
                storage.fillY(pos.up(y), state, this.height - this.minHeight);
            }
            for (BlockPos pos : rasterizedArea) {
                int y = generator.getGenerationHeight(pos.getX(), pos.getZ()) + this.minHeight;
                storage.set(pos.up(y), this.surface);
                storage.set(pos.up(y + this.height + 1), state);
            }
        }
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.BUILDING_PART;
    }
}
