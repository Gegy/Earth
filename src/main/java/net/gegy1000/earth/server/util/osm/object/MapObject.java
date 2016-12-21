package net.gegy1000.earth.server.util.osm.object;

import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public interface MapObject {
    Map<BlockPos, IBlockState> generate(EarthGenerator generator);

    default void set(Map<BlockPos, IBlockState> storage, BlockPos pos, IBlockState state) {
        storage.put(pos, state);
    }
}
