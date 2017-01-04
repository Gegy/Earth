package net.gegy1000.earth.server.util.osm.object.line.highway;

import com.vividsolutions.jts.geom.LineString;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.object.MapObjectType;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Set;

public class Street extends Highway {
    private static final IBlockState SURFACE = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BLACK);
    private static final IBlockState LINES = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.WHITE);

    public Street(EarthGenerator generator, LineString lines, Map<String, String> tags) {
        super(generator, lines, 3.5, tags);
    }

    /*@Override
    protected void generate(Coordinate point, Coordinate next, List<Coordinate> points, Set<BlockPos> quad, MapBlockAccess storage, EarthGenerator generator, int offsetY) {
        for (BlockPos pos : quad) {
            int x = pos.getX();
            int z = pos.getZ();
            storage.set(pos.up(generator.getGenerationHeight(x, z) + offsetY), this.surface);
        }
        //TODO line between each lane
        *//*List<BlockPos> line = Rasterize.line(point, next, false);
        int lastIndex = 0;
        for (int i = 0; i < line.size(); i++) {
            int delta = i - lastIndex;
            if (delta == 5) {
                lastIndex = i;
            } else if (delta < 3) {
                BlockPos pos = line.get(i);
                int x = pos.getX();
                int z = pos.getZ();
                storage.set(new BlockPos(x, generator.getGenerationHeight(x, z) + offsetY, z), LINES);
            }
        }*//*
    }*/

    @Override
    protected void generate(Set<BlockPos> road, MapBlockAccess access, EarthGenerator generator, int offsetY) {
        for (BlockPos pos : road) {
            int x = pos.getX();
            int z = pos.getZ();
            access.set(new BlockPos(x, generator.getGenerationHeight(x, z) + offsetY, z), this.surface);
        }
    }

    @Override
    public IBlockState getDefaultSurface() {
        return SURFACE;
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.STREET;
    }
}
