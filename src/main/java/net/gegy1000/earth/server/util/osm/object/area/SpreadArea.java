package net.gegy1000.earth.server.util.osm.object.area;

import com.vividsolutions.jts.geom.Geometry;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.raster.Rasterize;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public abstract class SpreadArea extends Area {
    public SpreadArea(Geometry geometry, Map<String, String> tags) {
        super(geometry, tags);
    }

    @Override
    public void generate(EarthGenerator generator, MapBlockAccess storage, int pass) {
        if (pass == 0) {
            IBlockState surface = this.getSurface();
            Rasterize.AreaWithOutline rasterized = Rasterize.areaOutline(generator, this.geometry);
            Set<BlockPos> rasterizedOutline = rasterized.getOutline();
            Set<BlockPos> rasterizedArea = rasterized.getArea();
            this.generateOutline(generator, storage, rasterizedOutline);
            List<BlockPos> spreadArea = new ArrayList<>(rasterizedArea.size());
            for (BlockPos pos : rasterizedArea) {
                int height = generator.getGenerationHeight(pos.getX(), pos.getZ());
                if (!rasterizedOutline.contains(pos)) {
                    spreadArea.add(pos);
                }
                if (surface != null) {
                    storage.set(pos.up(height), surface);
                }
            }
            Set<BlockPos> filledArea = new HashSet<>();
            Random random = new Random(spreadArea.size() << 8);
            int maxObjects = this.getMaxObjects();
            int spreadScale = this.getSpreadScale();
            for (int i = 0; i < maxObjects || maxObjects < 0; i++) {
                if (filledArea.size() >= spreadArea.size() - 2) {
                    break;
                }
                BlockPos generatePos = null;
                while (generatePos == null || filledArea.contains(generatePos)) {
                    generatePos = spreadArea.get(random.nextInt(spreadArea.size() - 1));
                }
                Map<BlockPos, IBlockState> generated = this.generate(generator, generatePos, storage);
                if (generated.size() > 0) {
                    int minX = Integer.MAX_VALUE;
                    int minZ = Integer.MAX_VALUE;
                    int maxX = Integer.MIN_VALUE;
                    int maxZ = Integer.MIN_VALUE;
                    for (Map.Entry<BlockPos, IBlockState> entry : generated.entrySet()) {
                        BlockPos pos = entry.getKey();
                        int x = pos.getX();
                        int z = pos.getZ();
                        if (x < minX) {
                            minX = x;
                        }
                        if (x > maxX) {
                            maxX = x;
                        }
                        if (z < minZ) {
                            minZ = z;
                        }
                        if (z > maxZ) {
                            maxZ = z;
                        }
                        storage.set(pos, entry.getValue());
                    }
                    minX -= spreadScale;
                    minZ -= spreadScale;
                    maxX += spreadScale;
                    maxZ += spreadScale;
                    for (int x = minX; x < maxX; x++) {
                        for (int z = minZ; z < maxZ; z++) {
                            filledArea.add(new BlockPos(x, 0, z));
                        }
                    }
                } else {
                    int minX = generatePos.getX() - spreadScale;
                    int minZ = generatePos.getZ() - spreadScale;
                    int maxX = generatePos.getX() + spreadScale;
                    int maxZ = generatePos.getZ() + spreadScale;
                    for (int x = minX; x < maxX; x++) {
                        for (int z = minZ; z < maxZ; z++) {
                            filledArea.add(new BlockPos(x, 0, z));
                        }
                    }
                }
            }
        }
    }

    protected abstract Map<BlockPos, IBlockState> generate(EarthGenerator generator, BlockPos pos, MapBlockAccess storage);

    protected abstract int getMaxObjects();

    protected abstract int getSpreadScale();

    protected abstract IBlockState getSurface();

    protected abstract void generateOutline(EarthGenerator generator, MapBlockAccess storage, Set<BlockPos> outline);

    protected abstract boolean useThickOutline();
}
