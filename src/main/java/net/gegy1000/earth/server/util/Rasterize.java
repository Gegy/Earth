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
        List<BlockPos> positions = new ArrayList<>();
        int deltaX = MathHelper.floor(end.x - start.x);
        int deltaY = MathHelper.floor(end.y - start.y);
        int longest = Math.abs(deltaX);
        int shortest = Math.abs(deltaY);
        int deltaX1 = 0, deltaY1 = 0, deltaX2 = 0, deltaY2 = 0;
        if (deltaX < 0) {
            deltaX1 = -1;
        } else if (deltaX > 0) {
            deltaX1 = 1;
        }
        if (deltaY < 0) {
            deltaY1 = -1;
        } else if (deltaY > 0) {
            deltaY1 = 1;
        }
        if (deltaX < 0) {
            deltaX2 = -1;
        } else if (deltaX > 0) {
            deltaX2 = 1;
        }
        if (longest <= shortest) {
            longest = Math.abs(deltaY);
            shortest = Math.abs(deltaX);
            if (deltaY < 0) {
                deltaY2 = -1;
            } else if (deltaY > 0) {
                deltaY2 = 1;
            }
            deltaX2 = 0;
        }
        int x = MathHelper.floor(start.x);
        int y = MathHelper.floor(start.y);
        int numerator = longest >> 1;
        for (int i = 0; i <= longest; i++) {
            positions.add(new BlockPos(x, 0, y));
            numerator += shortest;
            if (numerator >= longest) {
                numerator -= longest;
                x += deltaX1;
                if (thick) {
                    positions.add(new BlockPos(x, 0, y));
                }
                y += deltaY1;
            } else {
                x += deltaX2;
                if (thick) {
                    positions.add(new BlockPos(x, 0, y));
                }
                y += deltaY2;
            }
        }
        return positions;
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

    public static Set<BlockPos> triangle(List<Coordinate> points) {
        if (points.size() != 3) {
            throw new IllegalArgumentException("Rasterization expected 3 points! Got " + points.size() + " points!");
        }

        Rasterize.sortDescendingY(points);
        Coordinate p1 = points.get(0);
        Coordinate p2 = points.get(1);
        Coordinate p3 = points.get(2);
        if (p2.y == p3.y) {
            return Rasterize.bottomFlat(p1, p2, p3);
        } else if (p1.y == p2.y) {
            return Rasterize.topFlat(p1, p2, p3);
        } else {
            Coordinate p4 = new Coordinate(MathHelper.floor(p1.x + (p2.y - p1.y) / (p3.y - p1.y) * (p3.x - p1.x)), p2.y);
            Set<BlockPos> bottom = Rasterize.bottomFlat(p1, p2, p4);
            Set<BlockPos> top = Rasterize.topFlat(p2, p4, p3);
            Set<BlockPos> rasterized = new HashSet<>();
            rasterized.addAll(bottom);
            rasterized.addAll(top);
            return rasterized;
        }
    }

    private static Set<BlockPos> bottomFlat(Coordinate p1, Coordinate p2, Coordinate p3) {
        Set<BlockPos> rasterized = new HashSet<>();

        double invSlope1 = (p2.x - p1.x) / (p2.y - p1.y);
        double invSlope2 = (p3.x - p1.x) / (p3.y - p1.y);
        double currentX1 = p1.x;
        double currentX2 = p1.x;

        int minY = MathHelper.floor(p1.y);
        int maxY = MathHelper.ceil(p2.y);
        for (int scanLineY = minY; scanLineY <= maxY; scanLineY++) {
            int min = MathHelper.floor(Math.min(currentX1, currentX2));
            int max = MathHelper.ceil(Math.max(currentX1, currentX2));
            for (int x = min; x < max; x++) {
                rasterized.add(new BlockPos(x, 0, scanLineY));
            }
            currentX1 += invSlope1;
            currentX2 += invSlope2;
        }

        return rasterized;
    }

    private static Set<BlockPos> topFlat(Coordinate p1, Coordinate p2, Coordinate p3) {
        Set<BlockPos> rasterized = new HashSet<>();

        double invSlope1 = (p3.x - p1.x) / (p3.y - p1.y);
        double invSlope2 = (p3.x - p2.x) / (p3.y - p2.y);
        double currentX1 = p3.x;
        double currentX2 = p3.x;

        int minY = MathHelper.floor(p1.y);
        int maxY = MathHelper.ceil(p3.y);
        for (int scanLineY = maxY; scanLineY > minY; scanLineY--) {
            int min = MathHelper.floor(Math.min(currentX1, currentX2));
            int max = MathHelper.ceil(Math.max(currentX1, currentX2));
            for (int x = min; x < max; x++) {
                rasterized.add(new BlockPos(x, 0, scanLineY));
            }
            currentX1 -= invSlope1;
            currentX2 -= invSlope2;
        }

        return rasterized;
    }

    private static void sortDescendingY(List<Coordinate> points) {
        points.sort(Comparator.comparingDouble(point -> point.y));
    }
}
