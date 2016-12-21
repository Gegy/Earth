package net.gegy1000.earth.server.util.osm.object;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import net.gegy1000.earth.server.util.Rasterize;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Street implements MapObject {
    private LineString lines;
    private double width;

    public Street(EarthGenerator generator, LineString lines) {
        this.lines = lines;
        this.width = generator.getRatio() * 6.0;
    }

    @Override
    public Map<BlockPos, IBlockState> generate(EarthGenerator generator) {
        Map<BlockPos, IBlockState> blocks = new HashMap<>();
        IBlockState state = Blocks.COAL_BLOCK.getDefaultState();
        for (int i = 0; i < this.lines.getNumPoints() - 1; i++) {
            Coordinate point = generator.toWorldCoordinates(this.lines.getPointN(i));
            Coordinate next = generator.toWorldCoordinates(this.lines.getPointN(i + 1));
            double deltaX = next.x - point.x;
            double deltaZ = next.y - point.y;
            double length = Math.sqrt((deltaX * deltaX) + (deltaZ * deltaZ));
            double offsetX = (this.width * deltaZ / length) / 2;
            double offsetZ = (this.width * deltaX / length) / 2;
            List<Coordinate> points = new ArrayList<>();
            points.add(new Coordinate(point.x - offsetX, point.y + offsetZ));
            points.add(new Coordinate(point.x + offsetX, point.y - offsetZ));
            points.add(new Coordinate(next.x - offsetX, next.y + offsetZ));
            points.add(new Coordinate(next.x + offsetX, next.y - offsetZ));
            Set<BlockPos> quad = Rasterize.quad(points);
            for (BlockPos pos : quad) {
                int x = pos.getX();
                int z = pos.getZ();
                this.set(blocks, new BlockPos(x, generator.getGenerationHeight(x, z), z), state);
            }
            /*for (Coordinate p : points) {
                this.set(blocks, new BlockPos(p.x, 65, p.y), Blocks.REDSTONE_BLOCK.getDefaultState());
            }
            for (int t = 0; t < Math.abs(deltaX) + Math.abs(deltaZ); t++) {
                double f = t / (Math.abs(deltaX) + Math.abs(deltaZ));
                this.set(blocks, new BlockPos(point.x + (next.x - point.x) * f, 65, point.y + (next.y - point.y) * f), Blocks.COBBLESTONE.getDefaultState());
            }*/
        }
        return blocks;
    }
}
