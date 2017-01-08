package net.gegy1000.earth.server.util.osm.object.area;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.object.MapObjectType;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.util.raster.Rasterize;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Garden extends Area {
    private static final BlockFlower FLOWER = Blocks.RED_FLOWER;
    private static final IBlockState[] BLOCKS = new IBlockState[] {
            FLOWER.getDefaultState(),
            FLOWER.getDefaultState().withProperty(FLOWER.getTypeProperty(), BlockFlower.EnumFlowerType.ALLIUM),
            FLOWER.getDefaultState().withProperty(FLOWER.getTypeProperty(), BlockFlower.EnumFlowerType.BLUE_ORCHID),
            FLOWER.getDefaultState().withProperty(FLOWER.getTypeProperty(), BlockFlower.EnumFlowerType.HOUSTONIA),
            FLOWER.getDefaultState().withProperty(FLOWER.getTypeProperty(), BlockFlower.EnumFlowerType.ORANGE_TULIP),
            FLOWER.getDefaultState().withProperty(FLOWER.getTypeProperty(), BlockFlower.EnumFlowerType.PINK_TULIP),
            FLOWER.getDefaultState().withProperty(FLOWER.getTypeProperty(), BlockFlower.EnumFlowerType.WHITE_TULIP),
            Blocks.YELLOW_FLOWER.getDefaultState(),
            Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS),
    };

    public Garden(Geometry geometry, Tags tags) {
        super(geometry, tags);
    }

    @Override
    public void generate(EarthGenerator generator, MapBlockAccess storage, int pass) {
        if (pass == 0) {
            Coordinate[] coordArray = this.geometry.getCoordinates();
            List<Coordinate> coordinates = new ArrayList<>();
            for (Coordinate coordinate : coordArray) {
                coordinates.add(generator.toWorldCoordinates(coordinate));
            }
            Rasterize.AreaWithOutline rasterized = Rasterize.areaOutline(generator, this.geometry);
            Set<BlockPos> rasterizedOutline = rasterized.getOutline();
            Set<BlockPos> rasterizedArea = rasterized.getArea();
            for (BlockPos pos : rasterizedOutline) {
                pos = pos.up(generator.getGenerationHeight(pos.getX(), pos.getZ()));
                if (storage.get(pos) == null) {
                    storage.set(pos.up(), Blocks.DARK_OAK_FENCE.getDefaultState());
                }
            }
            Random random = new Random(rasterizedArea.size() << 8);
            for (BlockPos pos : rasterizedArea) {
                if (!rasterizedOutline.contains(pos)) {
                    pos = pos.up(generator.getGenerationHeight(pos.getX(), pos.getZ()));
                    if (storage.get(pos) == null) {
                        storage.set(pos.up(), BLOCKS[random.nextInt(BLOCKS.length)]);
                    }
                }
            }
        }
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.GARDEN;
    }
}
