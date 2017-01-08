package net.gegy1000.earth.server.util.osm.object.line.highway;

import com.vividsolutions.jts.geom.LineString;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.MapMaterial;
import net.gegy1000.earth.server.util.osm.object.MapObjectType;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public class Steps extends Highway {
    private IBlockState defaultSurface = MapMaterial.get("paved", null);

    public Steps(EarthGenerator generator, LineString lines, Tags tags) {
        super(generator, lines, 2.0, tags);
    }

    @Override
    protected void generate(Set<BlockPos> road, MapBlockAccess access, EarthGenerator generator, int offsetY) {
        for (BlockPos pos : road) {
            access.set(pos.up(generator.getGenerationHeight(pos.getX(), pos.getZ()) + offsetY), this.surface);
        }
    }

    @Override
    public IBlockState getDefaultSurface() {
        return this.defaultSurface;
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.STEPS;
    }
}
