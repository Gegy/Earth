package net.gegy1000.earth.server.util.osm.object.area;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import net.gegy1000.earth.server.util.Rasterize;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.OSMConstants;
import net.gegy1000.earth.server.util.osm.object.MapObject;
import net.gegy1000.earth.server.util.osm.tag.TagHandler;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Building extends Area {
    protected int minHeight;
    protected int height;
    protected int levels;
    protected int minLevel;

    public Building(EarthGenerator generator, Geometry geometry, Map<String, String> tags) {
        super(geometry, tags);
        this.levels = TagHandler.getFull(TagType.INTEGER, tags, 1, "levels", "building:levels");
        this.minLevel = TagHandler.getFull(TagType.INTEGER, tags, 0, "min_level", "building:min_level");
        double scaleRatio = generator.getRatio();
        double defaultHeight = this.levels * OSMConstants.LEVEL_HEIGHT * scaleRatio;
        this.height = MathHelper.ceil(Math.max(OSMConstants.LEVEL_HEIGHT, TagHandler.getFull(TagType.DOUBLE, tags, defaultHeight, "height", "building:height") * scaleRatio));
        this.minHeight = MathHelper.ceil(TagHandler.getFull(TagType.DOUBLE, tags, 0.0, "min_height", "building:min_height") * scaleRatio);
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
            Coordinate[] coordArray = this.geometry.getCoordinates();
            List<Coordinate> coordinates = new ArrayList<>();
            for (Coordinate coordinate : coordArray) {
                coordinates.add(generator.toWorldCoordinates(coordinate));
            }
            Set<BlockPos> rasterizedOutline = Rasterize.polygonOutline(coordinates);
            for (BlockPos pos : rasterizedOutline) {
                int y = generator.getGenerationHeight(pos.getX(), pos.getZ()) + 1 + this.minHeight;
                storage.fillY(pos.up(y), state, this.height - this.minHeight);
            }
            Set<BlockPos> rasterizedArea = Rasterize.polygon(coordinates);
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
}
