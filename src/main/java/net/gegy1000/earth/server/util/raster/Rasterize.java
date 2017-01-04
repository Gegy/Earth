package net.gegy1000.earth.server.util.raster;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import de.topobyte.jgs.transform.CoordinateTransformer;
import de.topobyte.jts2awt.Jts2Awt;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class Rasterize {
    private static final Stroke RESET_STROKE = new BasicStroke(1);
    private static final Stroke THICK_STROKE = new BasicStroke(2);

    private static final int IMAGE_PADDING = 4;

    private static final BufferedImage RASTER_IMAGE = new BufferedImage(new BlockColor(), new BlockRaster(128, 128), false, new Hashtable<>());
    private static final Graphics2D GRAPHICS = RASTER_IMAGE.createGraphics();

    private static int minX, minZ, maxX, maxZ;
    private static BlockPos origin;
    private static Set<BlockPos> raster = new HashSet<>();

    static {
        GRAPHICS.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        GRAPHICS.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        GRAPHICS.setColor(new Color(255, 0, 0));
    }

    public static void setLimits(int minX, int minZ, int maxX, int maxZ) {
        Rasterize.minX = Math.min(minX, maxX);
        Rasterize.minZ = Math.min(minZ, maxZ);
        Rasterize.maxX = Math.max(minX, maxX);
        Rasterize.maxZ = Math.max(minZ, maxZ);
    }

    public static AreaWithOutline areaOutline(EarthGenerator generator, Geometry geometry) {
        Shape shape = Jts2Awt.toShape(geometry, Rasterize.transformer(generator));
        Set<BlockPos> outline = Rasterize.outline(shape);
        Set<BlockPos> area = Rasterize.area(shape);
        return new AreaWithOutline(outline, area);
    }

    public static Set<BlockPos> area(EarthGenerator generator, Geometry geometry) {
        Shape shape = Jts2Awt.toShape(geometry, Rasterize.transformer(generator));
        return Rasterize.area(shape);
    }

    public static Set<BlockPos> outline(EarthGenerator generator, Geometry geometry) {
        Shape shape = Jts2Awt.toShape(geometry, Rasterize.transformer(generator));
        return Rasterize.outline(shape);
    }

    public static Set<BlockPos> path(EarthGenerator generator, LineString line, int width) {
        Path2D path = Jts2Awt.getPath(line, Rasterize.transformer(generator));
        return Rasterize.path(path, width);
    }

    public static Set<BlockPos> area(Shape shape) {
        return Rasterize.draw(shape, RESET_STROKE, s -> {
            GRAPHICS.draw(s);
            GRAPHICS.fill(s);
        });
    }

    public static Set<BlockPos> outline(Shape shape) {
        return Rasterize.draw(shape, RESET_STROKE, GRAPHICS::draw);
    }

    public static Set<BlockPos> path(Path2D path, int width) {
        return Rasterize.draw(path, new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), GRAPHICS::draw);
    }

    public static Set<BlockPos> line(EarthGenerator generator, Coordinate start, Coordinate end, boolean thick) {
        CoordinateTransformer transformer = Rasterize.transformer(generator);
        double startX = transformer.getX(start.x);
        double startY = transformer.getY(start.y);
        double endX = transformer.getX(end.x);
        double endY = transformer.getY(end.y);
        Line2D.Double line = new Line2D.Double(startX, startY, endX, endY);
        return Rasterize.draw(line, thick ? THICK_STROKE : RESET_STROKE, GRAPHICS::draw);
    }

    private static Set<BlockPos> draw(Shape shape, Stroke stroke, Consumer<Shape> function) {
        GRAPHICS.setStroke(stroke);
        Set<BlockPos> rasterized = new HashSet<>();
        Rectangle2D bounds = shape.getBounds2D();
        if (bounds.getWidth() > 0 && bounds.getHeight() > 0) {
            bounds.setRect(bounds.getMinX() - 4, bounds.getMinY() - 4, bounds.getWidth() + 8, bounds.getHeight() + 8);
            int shapeMinX = MathHelper.floor(bounds.getMinX());
            int shapeMinY = MathHelper.floor(bounds.getMinY());
            int shapeMaxX = MathHelper.ceil(bounds.getMaxX());
            int shapeMaxY = MathHelper.ceil(bounds.getMaxY());
            if (!(shapeMinX > maxX || shapeMinY > maxZ || shapeMaxX < minX || shapeMaxY < minZ)) {
                int width = MathHelper.ceil(bounds.getWidth());
                int height = MathHelper.ceil(bounds.getHeight());
                GRAPHICS.translate(-shapeMinX, -shapeMinY);
                int size = RASTER_IMAGE.getWidth();
                int actualSize = size - (IMAGE_PADDING * 2);
                if (width < actualSize && height < actualSize) {
                    origin = new BlockPos(shapeMinX, 0, shapeMinY);
                    function.accept(shape);
                    rasterized.addAll(raster);
                    raster.clear();
                } else {
                    int divisionsX = MathHelper.ceil((width + 1) / (double) actualSize);
                    int divisionsY = MathHelper.ceil((height + 1) / (double) actualSize);
                    for (int divisionX = 0; divisionX < divisionsX; divisionX++) {
                        for (int divisionY = 0; divisionY < divisionsY; divisionY++) {
                            int x = (divisionX * size) + IMAGE_PADDING;
                            int y = (divisionY * size) + IMAGE_PADDING;
                            int worldX = x + shapeMinX;
                            int worldY = y + shapeMinY;
                            if (!(worldX > maxX || worldY > maxZ || worldX + size < minX || worldY + size < minZ)) {
                                origin = new BlockPos(shapeMinX + x, 0, shapeMinY + y);
                                GRAPHICS.translate(-x, -y);
                                function.accept(shape);
                                rasterized.addAll(raster);
                                raster.clear();
                                GRAPHICS.translate(x, y);
                            }
                        }
                    }
                }
                GRAPHICS.translate(shapeMinX, shapeMinY);
            }
        }
        return rasterized;
    }

    private static CoordinateTransformer transformer(EarthGenerator generator) {
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

    public static List<BlockPos> line(Coordinate start, Coordinate end, boolean thick) {
        List<BlockPos> rasterized = new ArrayList<>();

        Coordinate currentPoint = new Coordinate(MathHelper.floor(start.x), MathHelper.floor(start.y));

        boolean changed = false;

        int deltaX = Math.max(1, Math.abs(MathHelper.floor(end.x) - MathHelper.floor(start.x)));
        int deltaY = Math.max(1, Math.abs(MathHelper.floor(end.y) - MathHelper.floor(start.y)));

        int signumX = Integer.signum(MathHelper.floor(end.x) - MathHelper.floor(start.x));
        int signumY = Integer.signum(MathHelper.floor(end.y) - MathHelper.floor(start.y));

        if (deltaY > deltaX) {
            int tmp = deltaX;
            deltaX = deltaY;
            deltaY = tmp;
            changed = true;
        }

        double longLength = 2 * deltaY - deltaX;

        for (int i = 0; i <= deltaX; i++) {
            rasterized.add(new BlockPos(currentPoint.x, 0, currentPoint.y));

            while (longLength >= 0) {
                if (changed) {
                    currentPoint.x += signumX;
                } else {
                    currentPoint.y += signumY;
                }
                if (thick) {
                    rasterized.add(new BlockPos(currentPoint.x, 0, currentPoint.y));
                }
                longLength = longLength - 2 * deltaX;
            }

            if (changed) {
                currentPoint.y += signumY;
            } else {
                currentPoint.x += signumX;
            }
            if (thick) {
                rasterized.add(new BlockPos(currentPoint.x, 0, currentPoint.y));
            }

            longLength = longLength + 2 * deltaY;
        }

        return rasterized;
    }

    private static class BlockRaster extends WritableRaster {
        public BlockRaster(int width, int height) {
            super(new BlockModel(width, height), new BlockBuffer(width, height), new Point(0, 0));
        }
    }

    private static class BlockColor extends DirectColorModel {
        public BlockColor() {
            super(8, 0xFF, 0, 0, 0);
        }

        @Override
        public SampleModel createCompatibleSampleModel(int w, int h) {
            return new BlockModel(w, h);
        }
    }

    private static class BlockModel extends SinglePixelPackedSampleModel {
        public BlockModel(int w, int h) {
            super(DataBuffer.TYPE_BYTE, w, h, new int[] { 0xFF, 0, 0 });
        }

        @Override
        public SampleModel createCompatibleSampleModel(int w, int h) {
            return new BlockModel(w, h);
        }

        @Override
        public SampleModel createSubsetSampleModel(int[] bands) {
            return new BlockModel(this.width, this.height);
        }

        @Override
        public DataBuffer createDataBuffer() {
            return new BlockBuffer(this.width, this.height);
        }
    }

    private static class BlockBuffer extends DataBuffer {
        private final int width, height;

        public BlockBuffer(int width, int height) {
            super(DataBuffer.TYPE_BYTE, width * height);
            this.width = width;
            this.height = height;
        }

        @Override
        public int getElem(int bank, int i) {
            return raster.contains(origin.add(i % this.width, 0, i / this.width)) ? 255 : 0;
        }

        @Override
        public void setElem(int bank, int i, int value) {
            BlockPos pos = origin.add(i % this.width, 0, i / this.width);
            if (value > 128) {
                raster.add(pos);
            } else {
                raster.remove(pos);
            }
        }
    }

    public static class AreaWithOutline {
        private final Set<BlockPos> outline;
        private final Set<BlockPos> area;

        public AreaWithOutline(Set<BlockPos> outline, Set<BlockPos> area) {
            this.outline = outline;
            this.area = area;
        }

        public Set<BlockPos> getOutline() {
            return this.outline;
        }

        public Set<BlockPos> getArea() {
            return this.area;
        }
    }
}
