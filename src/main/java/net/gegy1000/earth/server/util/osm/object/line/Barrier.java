package net.gegy1000.earth.server.util.osm.object.line;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import net.gegy1000.earth.server.util.Rasterize;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.MapMaterial;
import net.gegy1000.earth.server.util.osm.object.MapObjectType;
import net.gegy1000.earth.server.util.osm.tag.TagHandler;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Map;

public class Barrier extends Line {
    private IBlockState block = Blocks.OAK_FENCE.getDefaultState();
    private int height;

    public Barrier(EarthGenerator generator, LineString lines, Map<String, String> tags) {
        super(lines, tags);
        String barrier = tags.get("barrier");
        String material = tags.get("material");
        String fenceType = tags.get("fence_type");
        if (material == null) {
            material = fenceType;
        }
        IBlockState barrierBlock = material != null ? MapMaterial.getBarrier(material, null) : null;
        if (barrierBlock != null) {
            this.block = barrierBlock;
        } else {
            if (barrier.contains("wall")) {
                this.block = Blocks.COBBLESTONE_WALL.getDefaultState();
            }
        }
        this.height = MathHelper.ceil(TagHandler.getFull(TagType.DOUBLE, tags, 1.0, "height", "barrier:height") * generator.getRatio());
    }

    @Override
    public void generate(EarthGenerator generator, MapBlockAccess storage, int pass) {
        if (pass == 0) {
            for (int i = 0; i < this.line.getNumPoints() - 1; i++) {
                Coordinate point = generator.toWorldCoordinates(this.line.getPointN(i));
                Coordinate next = generator.toWorldCoordinates(this.line.getPointN(i + 1));
                List<BlockPos> line = Rasterize.line(point, next, true);
                for (BlockPos pos : line) {
                    int x = pos.getX();
                    int z = pos.getZ();
                    int y = generator.getGenerationHeight(x, z) + 1;
                    storage.fillY(pos.up(y), this.block, Math.max(1, this.height));
                }
            }
        }
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.BARRIER;
    }
}
