package net.gegy1000.earth.server.util.osm.object.line;

import com.vividsolutions.jts.geom.LineString;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
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

public class River extends Line {
    private double width;

    public River(EarthGenerator generator, LineString lines, Tags tags) {
        super(lines, tags);
        String waterway = tags.get("waterway");
        if (waterway == null) {
            waterway = tags.get("water");
        }
        switch (waterway) {
            case "river":
                this.width = 30;
                break;
            case "stream":
            case "canal":
                this.width = 4;
                break;
        }
        this.width = tags.tag("width").get(TagType.DOUBLE, this.width);
        this.width /= generator.getScaleRatio();
    }

    @Override
    public void generate(EarthGenerator generator, MapBlockAccess storage, int pass) {
        if (pass == 0) {
            IBlockState state = Blocks.WATER.getDefaultState();
            Set<BlockPos> river = Rasterize.path(generator, this.line, MathHelper.ceil(this.width));
            for (BlockPos pos : river) {
                int x = pos.getX();
                int z = pos.getZ();
                storage.set(new BlockPos(x, generator.getGenerationHeight(x, z), z), state);
            }
        }
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.RIVER;
    }
}
