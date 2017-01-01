package net.gegy1000.earth.server.util;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Rasterize {
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

    public static Set<BlockPos> polygonOutline(List<Coordinate> points) {
        return Rasterize.polygonOutline(points, false);
    }

    public static Set<BlockPos> polygonOutline(List<Coordinate> points, boolean thick) {
        Set<BlockPos> outline = new HashSet<>();
        Coordinate lastPoint = points.get(points.size() - 1);
        for (Coordinate coordinate : points) {
            outline.addAll(Rasterize.line(lastPoint, coordinate, thick));
            lastPoint = coordinate;
        }
        return outline;
    }

    public static Set<BlockPos> polygon(List<Coordinate> points) {
        while (points.size() > 3 && points.get(0).equals(points.get(points.size() - 1))) {
            points.remove(points.size() - 1);
        }
        if (points.size() >= 3) {
            if (points.size() == 3) {
                return Rasterize.triangle(points);
            }
            Set<BlockPos> rasterized = new HashSet<>();
            List<Coordinate> result = new ArrayList<>();
            Triangulate.process(points, result);
            if (result.isEmpty()) {
                result = points;
            }
            for (int i = 0; i < result.size(); i += 3) {
                ArrayList<Coordinate> trianglePoints = Lists.newArrayList(result.get(i), result.get(i + 1), result.get(i + 2));
                rasterized.addAll(Rasterize.triangle(trianglePoints));
            }
            return rasterized;
        } else {
            throw new IllegalArgumentException("Rasterization requires 3 or more points!");
        }
//        return Rasterize.fillPolygonOutline(Rasterize.polygonOutline(points));
    }

    public static Set<BlockPos> fillPolygonOutline(Set<BlockPos> outline) {
        Set<BlockPos> rasterized = new HashSet<>();
        int minX = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : outline) {
            int x = pos.getX();
            int z = pos.getZ();
            if (x < minX) {
                minX = x;
            }
            if (z < minZ) {
                minZ = z;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (z > maxZ) {
                maxZ = z;
            }
        }
        Set<BlockPos> insideLine = new HashSet<>();
        for (int z = minZ; z <= maxZ; z++) {
            boolean inside = false;
            boolean lastPixel = false;
            int x = minX - 1;
            while (x++ <= maxX) {
                BlockPos pos = new BlockPos(x, 0, z);
                boolean pixel = outline.contains(pos);
                if (!lastPixel && lastPixel != pixel) {
                    inside = !inside;
                    if (!inside) {
                        rasterized.addAll(insideLine);
                        insideLine.clear();
                    }
                }
                lastPixel = pixel;
                if (inside) {
                    insideLine.add(pos);
                }
            }
            insideLine.clear();
        }
        rasterized.addAll(outline);
        return rasterized;
    }

    public static Set<BlockPos> quad(List<Coordinate> points) {
        if (points.size() != 4) {
            throw new IllegalArgumentException("Rasterization expected 4 points! Got " + points.size() + " points!");
        }
        Rasterize.sortDescendingY(points);
        points.sort((p1, p2) -> Double.compare(p2.x, p1.x));
        List<Coordinate> bottom = Lists.newArrayList(points.get(0), points.get(1), points.get(2));
        List<Coordinate> top = Lists.newArrayList(points.get(1), points.get(2), points.get(3));
        Set<BlockPos> rasterizedBottom = Rasterize.triangle(bottom);
        Set<BlockPos> rasterizedTop = Rasterize.triangle(top);
        Set<BlockPos> rasterized = new HashSet<>();
        rasterized.addAll(rasterizedBottom);
        rasterized.addAll(rasterizedTop);
        return rasterized;
    }

    public static Set<BlockPos> slopeQuad(List<Coordinate> points, int startY, int endY) {
        if (points.size() != 4) {
            throw new IllegalArgumentException("Rasterization expected 4 points! Got " + points.size() + " points!");
        }
        Set<BlockPos> sloped = new HashSet<>();
        List<BlockPos> line1 = Rasterize.line(points.get(0), points.get(2), false);
        List<BlockPos> line2 = Rasterize.line(points.get(1), points.get(3), false);
        int length = Math.min(line1.size(), line2.size());
        double increment = (double) (endY - startY) / length;
        double currentY = startY;
        for (int i = 0; i < length; i++) {
            BlockPos pos1 = line1.get(i);
            BlockPos pos2 = line2.get(i);
            List<BlockPos> line = Rasterize.line(new Coordinate(pos1.getX(), pos1.getZ()), new Coordinate(pos2.getX(), pos2.getZ()), true);
            for (BlockPos pos : line) {
                sloped.add(pos.up(MathHelper.floor(currentY)));
            }
            currentY += increment;
        }
        return sloped;
    }

    public static Set<BlockPos> triangle(List<Coordinate> points) {
        if (points.size() != 3) {
            throw new IllegalArgumentException("Rasterization expected 3 points! Got " + points.size() + " points!");
        }
        Rasterize.sortDescendingY(points);
        Coordinate p1 = points.get(0);
        Coordinate p2 = points.get(1);
        Coordinate p3 = points.get(2);
        if (p2.y == p3.y) {
            return Rasterize.flatSide(p1, p2, p3);
        } else if (p1.y == p2.y) {
            return Rasterize.flatSide(p3, p1, p2);
        } else {
            Coordinate p4 = new Coordinate(MathHelper.floor(p1.x + (p2.y - p1.y) / (p3.y - p1.y) * (p3.x - p1.x)), p2.y);
            Set<BlockPos> bottom = Rasterize.flatSide(p1, p2, p4);
            Set<BlockPos> top = Rasterize.flatSide(p3, p2, p4);
            Set<BlockPos> rasterized = new HashSet<>();
            rasterized.addAll(bottom);
            rasterized.addAll(top);
            return rasterized;
        }
    }

    private static Set<BlockPos> flatSide(Coordinate p1, Coordinate p2, Coordinate p3) {
        Set<BlockPos> rasterized = new HashSet<>();

        Coordinate currentPoint1 = new Coordinate(MathHelper.floor(p1.x), MathHelper.floor(p1.y));
        Coordinate currentPoint2 = new Coordinate(MathHelper.floor(p1.x), MathHelper.floor(p1.y));

        boolean changed1 = false;
        boolean changed2 = false;

        int deltaX1 = Math.max(1, Math.abs(MathHelper.floor(p2.x) - MathHelper.floor(p1.x)));
        int deltaY1 = Math.max(1, Math.abs(MathHelper.floor(p2.y) - MathHelper.floor(p1.y)));

        int deltaX2 = Math.max(1, Math.abs(MathHelper.floor(p3.x) - MathHelper.floor(p1.x)));
        int deltaY2 = Math.max(1, Math.abs(MathHelper.floor(p3.y) - MathHelper.floor(p1.y)));

        int signumX1 = Integer.signum(MathHelper.floor(p2.x) - MathHelper.floor(p1.x));
        int signumX2 = Integer.signum(MathHelper.floor(p3.x) - MathHelper.floor(p1.x));

        int signumY1 = Integer.signum(MathHelper.floor(p2.y) - MathHelper.floor(p1.y));
        int signumY2 = Integer.signum(MathHelper.floor(p3.y) - MathHelper.floor(p1.y));

        if (deltaY1 > deltaX1) {
            int tmp = deltaX1;
            deltaX1 = deltaY1;
            deltaY1 = tmp;
            changed1 = true;
        }

        if (deltaY2 > deltaX2) {
            int tmp = deltaX2;
            deltaX2 = deltaY2;
            deltaY2 = tmp;
            changed2 = true;
        }

        double long1 = 2 * deltaY1 - deltaX1;
        double long2 = 2 * deltaY2 - deltaX2;

        for (int i = 0; i <= deltaX1; i++) {
            rasterized.addAll(line(currentPoint1, currentPoint2, false));

            while (long1 >= 0) {
                if (changed1) {
                    currentPoint1.x += signumX1;
                } else {
                    currentPoint1.y += signumY1;
                }
                long1 = long1 - 2 * deltaX1;
            }

            if (changed1) {
                currentPoint1.y += signumY1;
            } else {
                currentPoint1.x += signumX1;
            }

            long1 = long1 + 2 * deltaY1;

            while (MathHelper.floor(currentPoint2.y) != MathHelper.floor(currentPoint1.y)) {
                while (long2 >= 0) {
                    if (changed2) {
                        currentPoint2.x += signumX2;
                    } else {
                        currentPoint2.y += signumY2;
                    }
                    long2 = long2 - 2 * deltaX2;
                }

                if (changed2) {
                    currentPoint2.y += signumY2;
                } else {
                    currentPoint2.x += signumX2;
                }

                long2 = long2 + 2 * deltaY2;
            }
        }

        return rasterized;
    }

    private static void sortDescendingY(List<Coordinate> points) {
        points.sort(Comparator.comparingDouble(point -> point.y));
    }
}
