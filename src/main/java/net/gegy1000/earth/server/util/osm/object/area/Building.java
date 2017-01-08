package net.gegy1000.earth.server.util.osm.object.area;

import com.vividsolutions.jts.geom.Geometry;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.OSMConstants;
import net.gegy1000.earth.server.util.osm.object.MapObject;
import net.gegy1000.earth.server.util.osm.object.MapObjectType;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.util.raster.Rasterize;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;
import java.util.Set;

public class Building extends Area {
    protected int minHeight;
    protected int height;
    protected int levels;
    protected int minLevel;

    public Building(EarthGenerator generator, Geometry geometry, Tags tags) {
        super(geometry, tags);
        this.levels = tags.top("levels").get(TagType.INTEGER, 1);
        this.minLevel = tags.top("min_level").get(TagType.INTEGER, 0);
        double scaleRatio = generator.getScaleRatio();
        double defaultHeight = this.levels * OSMConstants.LEVEL_HEIGHT;
        this.height = MathHelper.ceil(Math.max(OSMConstants.LEVEL_HEIGHT, tags.top("height").get(TagType.DOUBLE, defaultHeight) / scaleRatio));
        this.minHeight = MathHelper.ceil(tags.top("min_height").get(TagType.DOUBLE, 0.0) / scaleRatio);
        if (this.surface == null) {
            this.surface = Blocks.COBBLESTONE.getDefaultState();
        }
    }

    @Override
    public void generate(EarthGenerator generator, MapBlockAccess storage, int pass) {
        if (pass == 0) {
            int[] levelHeights = new int[this.levels];
            double levelHeight = (double) this.height / this.levels;
            double currentHeight = this.minHeight;
            for (int i = 0; i < this.levels; i++) {
                currentHeight += levelHeight;
                levelHeights[i] = (int) currentHeight;
            }
            for (MapObject relation : this.relations) {
                if (relation instanceof BuildingPart) {
                    BuildingPart part = (BuildingPart) relation;
                    if (part.hasMinLevel) {
                        levelHeights[part.minLevel] = part.minHeight;
                    }
                }
            }
            levelHeights[levelHeights.length - 1] = this.minHeight + this.height;
            IBlockState state = this.material != null ? this.material : Blocks.QUARTZ_BLOCK.getDefaultState();
            Rasterize.AreaWithOutline rasterized = Rasterize.areaOutline(generator, this.geometry);
            Set<BlockPos> rasterizedOutline = rasterized.getOutline();
            Set<BlockPos> rasterizedArea = rasterized.getArea();
            for (BlockPos pos : rasterizedOutline) {
                int y = generator.getGenerationHeight(pos.getX(), pos.getZ()) + 1 + this.minHeight;
                storage.fillY(pos.up(y), state, this.height - this.minHeight);
            }
            Set<BlockPos> area = new HashSet<>();
            for (BlockPos pos : area) {
                storage.set(pos, this.surface);
            }
            for (BlockPos pos : rasterizedArea) {
                int y = generator.getGenerationHeight(pos.getX(), pos.getZ());
                area.add(pos.up(y));
            }
            for (BlockPos pos : area) {
                for (int height : levelHeights) {
                    storage.set(pos.up(height + 1), state);
                }
            }
        }
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.BUILDING;
    }
}
