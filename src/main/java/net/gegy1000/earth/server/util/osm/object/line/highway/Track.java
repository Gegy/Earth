package net.gegy1000.earth.server.util.osm.object.line.highway;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Track extends Highway {
    private IBlockState defaultSurface = Blocks.GRASS_PATH.getDefaultState();

    public Track(EarthGenerator generator, LineString lines, Map<String, String> tags) {
        super(generator, lines, 3.0, tags);
        if (this.bridge) {
            this.defaultSurface = Blocks.STONE_SLAB.getDefaultState();
        }
    }

    @Override
    protected void generate(Coordinate point, Coordinate next, List<Coordinate> points, Set<BlockPos> quad, MapBlockAccess storage, EarthGenerator generator, int offsetY) {
        for (BlockPos pos : quad) {
            int x = pos.getX();
            int z = pos.getZ();
            storage.set(pos.up(generator.getGenerationHeight(x, z)), this.surface);
        }
    }

    @Override
    public IBlockState getDefaultSurface() {
        return this.defaultSurface;
    }
}
