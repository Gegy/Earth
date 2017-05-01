package net.gegy1000.earth.server.world.gen.raster;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import de.topobyte.jgs.transform.CoordinateTransformer;
import de.topobyte.jts2awt.Jts2Awt;
import net.gegy1000.earth.Earth;
import net.gegy1000.earth.server.util.MapPoint;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public class BlockGraphics {
    private final BufferedImage rasterImage = new BufferedImage(new BlockGraphics.BlockColor(), new BlockGraphics.BlockRaster(128, 128), false, new Hashtable<>());
    private final Graphics2D graphics = this.rasterImage.createGraphics();

    private final BlockStateColor stateColor = new BlockStateColor();

    private int minX, minZ, maxX, maxZ;
    private int originX;
    private int originZ;

    private GenData data;

    public BlockGraphics() {
        this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        this.graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        this.graphics.setColor(this.stateColor);
    }

    public void frame(int minX, int minZ, int maxX, int maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public void frame(MapPoint min, MapPoint max) {
        this.frame(MathHelper.floor(min.getX()), MathHelper.floor(min.getZ()), MathHelper.ceil(max.getX()), MathHelper.ceil(max.getZ()));
    }

    public void setBlock(IBlockState state) {
        this.stateColor.set(state);
    }

    public void setStroke(Stroke stroke) {
        this.graphics.setStroke(stroke);
    }

    public void setThick(boolean thick) {
        this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, thick ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public GenData draw(Shape shape) {
        return this.draw(shape, this.graphics::draw);
    }

    public GenData drawVertices(Area area) {
        return this.draw(area, s -> {
            PathIterator pathIterator = area.getPathIterator(null);
            double[] coordinates = new double[6];
            while (!pathIterator.isDone()) {
                int type = pathIterator.currentSegment(coordinates);
                if (type == PathIterator.SEG_LINETO || type == PathIterator.SEG_MOVETO) {
                    int x = (int) coordinates[0];
                    int y = (int) coordinates[1];
                    this.graphics.drawLine(x, y, x, y);
                }
                pathIterator.next();
            }
        });
    }

    public GenData fill(Shape shape) {
        return this.draw(shape, this.graphics::fill);
    }

    public GenData draw(Shape shape, ShapeRenderer renderer) {
        Rectangle2D bounds = shape.getBounds2D();
        if (bounds.getWidth() > 0 && bounds.getHeight() > 0) {
            bounds.setRect(bounds.getMinX() - 4, bounds.getMinY() - 4, bounds.getWidth() + 8, bounds.getHeight() + 8);
            int shapeMinX = MathHelper.floor(bounds.getMinX());
            int shapeMinZ = MathHelper.floor(bounds.getMinY());
            int shapeMaxX = MathHelper.ceil(bounds.getMaxX());
            int shapeMaxZ = MathHelper.ceil(bounds.getMaxY());
            if (!(shapeMinX > this.maxX || shapeMinZ > this.maxZ || shapeMaxX < this.minX || shapeMaxZ < this.minZ)) {
                int width = MathHelper.ceil(bounds.getWidth()) + 8;
                int height = MathHelper.ceil(bounds.getHeight()) + 8;
                int imageSize = this.rasterImage.getWidth();
                this.data = new GenData(new BlockPos(shapeMinX, 0, shapeMinZ), width, height);
                int sectorsX = MathHelper.ceil(width / (double) imageSize);
                int sectorsZ = MathHelper.ceil(height / (double) imageSize);
                for (int sectorX = 0; sectorX < sectorsX; sectorX++) {
                    for (int sectorZ = 0; sectorZ < sectorsZ; sectorZ++) {
                        int x = sectorX * imageSize;
                        int z = sectorZ * imageSize;
                        int worldX = shapeMinX + x + 4;
                        int worldZ = shapeMinZ + z + 4;
                        if (!(worldX > this.maxX || worldZ > this.maxZ || worldX + imageSize < this.minX || worldZ + imageSize < this.minZ)) {
                            this.originX = worldX;
                            this.originZ = worldZ;
                            this.graphics.translate(-worldX, -worldZ);
                            try {
                                renderer.draw(shape);
                            } catch (Exception e) {
                                Earth.LOGGER.error("Failed to draw shape", e);
                            }
                            this.graphics.translate(worldX, worldZ);
                        }
                    }
                }
                return this.data;
            }
        }
        return new BlankGenData();
    }

    public Path2D toPath(EarthGenerator generator, LineString string) {
        return Jts2Awt.getPath(string, this.transformer(generator));
    }

    public Area toArea(EarthGenerator generator, MultiPolygon area) {
        return Jts2Awt.toShape(area, this.transformer(generator));
    }

    private CoordinateTransformer transformer(EarthGenerator generator) {
        return new CoordinateTransformer() {
            @Override
            public double getX(double x) {
                return MathHelper.floor(generator.fromLongitude(x));
            }

            @Override
            public double getY(double y) {
                return MathHelper.floor(generator.fromLatitude(y));
            }
        };
    }

    private class BlockRaster extends WritableRaster {
        public BlockRaster(int width, int height) {
            super(new BlockGraphics.BlockModel(width, height), new BlockGraphics.BlockBuffer(width, height), new Point(0, 0));
        }
    }

    private class BlockColor extends DirectColorModel {
        BlockColor() {
            super(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000);
        }

        @Override
        public SampleModel createCompatibleSampleModel(int w, int h) {
            return new BlockGraphics.BlockModel(w, h);
        }
    }

    private class BlockModel extends SinglePixelPackedSampleModel {
        BlockModel(int w, int h) {
            super(DataBuffer.TYPE_INT, w, h, new int[] { 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000 });
        }

        @Override
        public SampleModel createCompatibleSampleModel(int w, int h) {
            return new BlockGraphics.BlockModel(w, h);
        }

        @Override
        public SampleModel createSubsetSampleModel(int[] bands) {
            return new BlockGraphics.BlockModel(this.width, this.height);
        }

        @Override
        public DataBuffer createDataBuffer() {
            return new BlockGraphics.BlockBuffer(this.width, this.height);
        }
    }

    private class BlockBuffer extends DataBuffer {
        private final int width;

        BlockBuffer(int width, int height) {
            super(DataBuffer.TYPE_INT, width * height);
            this.width = width;
        }

        @Override
        public int getElem(int bank, int i) {
            BlockPos dataOrigin = BlockGraphics.this.data.getOrigin();
            int x = i % this.width + BlockGraphics.this.originX - dataOrigin.getX();
            int z = i / this.width + BlockGraphics.this.originZ - dataOrigin.getZ();
            return Block.getStateId(BlockGraphics.this.data.get(x, z)) | 0xFF000000;
        }

        @Override
        public void setElem(int bank, int i, int value) {
            BlockPos dataOrigin = BlockGraphics.this.data.getOrigin();
            int x = i % this.width + BlockGraphics.this.originX - dataOrigin.getX();
            int z = i / this.width + BlockGraphics.this.originZ - dataOrigin.getZ();
            BlockGraphics.this.data.put(x, z, Block.getStateById(value & 0x00FFFFFF));
        }
    }
}
