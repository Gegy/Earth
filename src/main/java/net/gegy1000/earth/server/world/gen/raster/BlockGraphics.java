package net.gegy1000.earth.server.world.gen.raster;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import de.topobyte.jgs.transform.CoordinateTransformer;
import de.topobyte.jts2awt.Jts2Awt;
import net.gegy1000.earth.Earth;
import net.gegy1000.earth.server.util.MapPoint;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.util.math.MathHelper;

import java.awt.BasicStroke;
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
    private static final BasicStroke RESET_STROKE = new BasicStroke(1);

    private final BufferedImage rasterImage = new BufferedImage(new BlockGraphics.BlockColor(), new BlockGraphics.BlockRaster(128, 128), false, new Hashtable<>());
    private final Graphics2D graphics = this.rasterImage.createGraphics();

    private final BlockStateColor stateColor = new BlockStateColor();

    private int minX, minZ, maxX, maxZ;
    private int originX;
    private int originZ;

    private GenData data;

    private boolean thick;

    public BlockGraphics() {
        this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        this.graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        this.graphics.setColor(this.stateColor);
        this.resetStroke();
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

    public void setState(int state) {
        this.stateColor.set(state);
    }

    public void setStroke(Stroke stroke) {
        this.graphics.setStroke(stroke);
    }

    public void resetStroke() {
        this.graphics.setStroke(RESET_STROKE);
    }

    public void setThick(boolean thick) {
        if (this.thick != thick) {
            this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, thick ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
            this.thick = thick;
        }
    }

    public void draw(Shape shape) {
        this.graphics.draw(shape);
    }

    public void fill(Shape shape) {
        this.graphics.fill(shape);
    }

    public void drawVertices(Area area) {
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
                this.data = new GenData(shapeMinX, shapeMinZ, width, height);
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
            super(8, 0xFF, 0, 0, 0);
        }

        @Override
        public SampleModel createCompatibleSampleModel(int w, int h) {
            return new BlockGraphics.BlockModel(w, h);
        }
    }

    private class BlockModel extends SinglePixelPackedSampleModel {
        BlockModel(int w, int h) {
            super(DataBuffer.TYPE_BYTE, w, h, new int[] { 0xFF, 0, 0 });
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
            super(DataBuffer.TYPE_BYTE, width * height);
            this.width = width;
        }

        @Override
        public int getElem(int bank, int i) {
            int x = i % this.width + BlockGraphics.this.originX - BlockGraphics.this.data.getOriginX();
            int z = i / this.width + BlockGraphics.this.originZ - BlockGraphics.this.data.getOriginZ();
            return BlockGraphics.this.data.get(x, z) & 0xFF;
        }

        @Override
        public void setElem(int bank, int i, int value) {
            int x = i % this.width + BlockGraphics.this.originX - BlockGraphics.this.data.getOriginX();
            int z = i / this.width + BlockGraphics.this.originZ - BlockGraphics.this.data.getOriginZ();
            BlockGraphics.this.data.put(x, z, (byte) (value & 0xFF));
        }
    }
}
