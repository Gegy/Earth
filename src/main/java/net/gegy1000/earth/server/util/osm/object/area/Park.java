package net.gegy1000.earth.server.util.osm.object.area;

import com.vividsolutions.jts.geom.Geometry;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.object.MapObjectType;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Park extends SpreadArea {
    public Park(Geometry geometry, Tags tags) {
        super(geometry, tags);
    }

    @Override
    protected Map<BlockPos, IBlockState> generate(EarthGenerator generator, BlockPos pos, MapBlockAccess storage) {
        Map<BlockPos, IBlockState> blocks = new HashMap<>();
        pos = pos.up(generator.getGenerationHeight(pos.getX(), pos.getZ()));
        if (storage.get(pos) == null) {
            blocks.put(pos.up(), Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS));
        }
        return blocks;
    }

    @Override
    protected int getMaxObjects() {
        return -1;
    }

    @Override
    protected int getSpreadScale() {
        return 2;
    }

    @Override
    protected IBlockState getSurface() {
        return null;
    }

    @Override
    protected void generateOutline(EarthGenerator generator, MapBlockAccess storage, Set<BlockPos> outline) {
        IBlockState state = Blocks.DARK_OAK_FENCE.getDefaultState();
        for (BlockPos pos : outline) {
            int height = generator.getGenerationHeight(pos.getX(), pos.getZ());
            pos = pos.up(height);
            if (storage.get(pos) == null) {
                storage.set(pos.up(), state);
            }
        }
    }

    @Override
    protected boolean useThickOutline() {
        return true;
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.PARK;
    }
}
