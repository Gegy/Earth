package net.gegy1000.earth.server.util.osm.object.line.highway;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.OSMConstants;
import net.gegy1000.earth.server.util.osm.object.line.Line;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.util.raster.Rasterize;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Highway extends Line {
    protected double width;
    protected Set<Sidewalk> sidewalk;

    public Highway(EarthGenerator generator, LineString lines, double laneWidth, Tags tags) {
        super(lines, tags);
        int lanes = tags.tag("lanes").get(TagType.INTEGER, 1);
        double defaultWidth = laneWidth * lanes;
        this.width = Math.max(1, tags.tag("width").get(TagType.DOUBLE, defaultWidth) / generator.getScaleRatio());
        this.sidewalk = Sidewalk.get(tags);
    }

    @Override
    public void generate(EarthGenerator generator, MapBlockAccess storage, int pass) {
        if (this.surface == null) {
            this.surface = this.getDefaultSurface();
        }
        int offsetY = this.bridge ? MathHelper.ceil(this.layer * OSMConstants.LAYER_HEIGHT / generator.getScaleRatio()) : 0;
        if (pass == 0) {
            Set<BlockPos> road = Rasterize.path(generator, this.line, MathHelper.ceil(this.width));
            this.generate(road, storage, generator, offsetY);
        } else if (pass == 1 && this.sidewalk.size() > 0) {
            for (int i = 0; i < this.line.getNumPoints() - 1; i++) {
                Coordinate point = generator.toWorldCoordinates(this.line.getPointN(i));
                Coordinate next = generator.toWorldCoordinates(this.line.getPointN(i + 1));
                List<Coordinate> points = this.toQuad(point, next, this.width + 1.5);
                Set<BlockPos> sidewalk = new HashSet<>();
                if (this.sidewalk.contains(Sidewalk.RIGHT)) {
                    sidewalk.addAll(Rasterize.line(points.get(0), points.get(2), true));
                }
                if (this.sidewalk.contains(Sidewalk.LEFT)) {
                    sidewalk.addAll(Rasterize.line(points.get(1), points.get(3), true));
                }
                for (BlockPos pos : sidewalk) {
                    int x = pos.getX();
                    int z = pos.getZ();
                    pos = new BlockPos(x, generator.getGenerationHeight(x, z) + offsetY, z);
                    if (storage.get(pos.down()) == null) {
                        storage.set(pos, Blocks.DOUBLE_STONE_SLAB.getDefaultState());
                    }
                }
            }
        }
    }

    protected abstract void generate(Set<BlockPos> road, MapBlockAccess access, EarthGenerator generator, int offsetY);

    public abstract IBlockState getDefaultSurface();

    public static Highway get(EarthGenerator generator, LineString line, Tags tags) {
        String highway = tags.get("highway");
        switch (highway) {
            case "primary":
            case "motorway":
            case "primary_link":
            case "motorway_link":
            case "secondary":
            case "secondary_link":
            case "residential":
            case "service":
            case "tertiary":
            case "tertiary_link":
            case "unclassified":
            case "abandoned":
            case "living_street":
                return new Street(generator, line, tags);
            case "footway":
            case "path":
            case "cycleway":
            case "pedestrian":
            case "stairs":
            case "steps":
                return new Path(generator, line, tags);
            case "track":
                return new Track(generator, line, tags);
        }
        return null;
    }

    public enum Sidewalk {
        LEFT,
        RIGHT;

        public static Set<Sidewalk> get(Tags tags) {
            Set<Sidewalk> sidewalks = new HashSet<>();
            String sidewalk = tags.tag("sidewalk").get(TagType.STRING, "no");
            switch (sidewalk) {
                case "right":
                    sidewalks.add(RIGHT);
                    break;
                case "left":
                    sidewalks.add(LEFT);
                    break;
                case "both":
                    sidewalks.add(RIGHT);
                    sidewalks.add(LEFT);
                    break;
            }
            return sidewalks;
        }
    }
}
