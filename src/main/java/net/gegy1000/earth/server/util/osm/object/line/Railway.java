package net.gegy1000.earth.server.util.osm.object.line;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import net.gegy1000.earth.server.util.Rasterize;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.tag.TagHandler;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Railway extends Line {
    private double width;

    public Railway(EarthGenerator generator, LineString lines, Map<String, String> tags) {
        super(lines, tags);
        int tracks = TagHandler.getFull(TagType.INTEGER, tags, 1, "tracks");
        this.width = 3.0 * tracks * generator.getRatio();
    }

    @Override
    public void generate(EarthGenerator generator, MapBlockAccess storage, int pass) {
        if (pass == 0) {
            IBlockState state = Blocks.RAIL.getDefaultState();
            for (int i = 0; i < this.line.getNumPoints() - 1; i++) {
                Coordinate point = generator.toWorldCoordinates(this.line.getPointN(i));
                Coordinate next = generator.toWorldCoordinates(this.line.getPointN(i + 1));
                List<Coordinate> points = this.toQuad(point, next, this.width);
                Set<BlockPos> rail = new HashSet<>();
                rail.addAll(Rasterize.line(points.get(0), points.get(2), true));
                rail.addAll(Rasterize.line(points.get(1), points.get(3), true));
                for (BlockPos pos : rail) {
                    int x = pos.getX();
                    int z = pos.getZ();
                    pos = new BlockPos(x, generator.getGenerationHeight(x, z) + 1, z);
                    storage.set(pos, state);
                }
            }
        }
    }
}
