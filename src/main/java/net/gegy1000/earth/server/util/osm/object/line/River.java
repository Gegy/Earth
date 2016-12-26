package net.gegy1000.earth.server.util.osm.object.line;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import net.gegy1000.earth.server.util.Rasterize;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class River extends Line {
    private double width;

    public River(EarthGenerator generator, LineString lines, Map<String, String> tags) {
        super(lines, tags);
        String waterway = tags.get("waterway");
        if (waterway == null) {
            waterway = tags.get("water");
        }
        switch (waterway) {
            case "river":
                this.width = 50;
                break;
            case "stream":
            case "canal":
                this.width = 4;
                break;
        }
        this.width *= generator.getRatio();
    }

    @Override
    public void generate(EarthGenerator generator, MapBlockAccess storage, int pass) {
        if (pass == 0) {
            IBlockState state = Blocks.WATER.getDefaultState();
            for (int i = 0; i < this.line.getNumPoints() - 1; i++) {
                Coordinate point = generator.toWorldCoordinates(this.line.getPointN(i));
                Coordinate next = generator.toWorldCoordinates(this.line.getPointN(i + 1));
                double deltaX = next.x - point.x;
                double deltaZ = next.y - point.y;
                double length = Math.sqrt((deltaX * deltaX) + (deltaZ * deltaZ));
                double offsetX = (this.width * deltaZ / length) / 2;
                double offsetZ = (this.width * deltaX / length) / 2;
                List<Coordinate> points = new ArrayList<>();
                points.add(new Coordinate((int) (point.x - offsetX), (int) (point.y + offsetZ)));
                points.add(new Coordinate((int) (point.x + offsetX), (int) (point.y - offsetZ)));
                points.add(new Coordinate((int) (next.x - offsetX), (int) (next.y + offsetZ)));
                points.add(new Coordinate((int) (next.x + offsetX), (int) (next.y - offsetZ)));
                Set<BlockPos> quad = Rasterize.quad(points);
                for (BlockPos pos : quad) {//-14392 80 -5724950
                    int x = pos.getX();
                    int z = pos.getZ();
                    storage.set(new BlockPos(x, generator.getGenerationHeight(x, z), z), state);
                }
            }
        }
    }
}
