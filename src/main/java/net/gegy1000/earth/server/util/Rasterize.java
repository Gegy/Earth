package net.gegy1000.earth.server.util;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Rasterize {
    public static Set<BlockPos> quad(List<Coordinate> points) {
        if (points.size() != 4) {
            throw new IllegalArgumentException("Rasterization expected 4 points! Got " + points.size() + " points!");
        }
        Rasterize.sortDescendingY(points);
        List<Coordinate> bottom = Lists.newArrayList(points.get(0), points.get(1), points.get(2));
        List<Coordinate> top = Lists.newArrayList(points.get(0), points.get(2), points.get(3));
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
            Coordinate p4 = new Coordinate(p1.x + (p2.y - p1.y) / (p3.y - p1.y) * (p3.x - p1.x), p2.y);
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
            int min = (int) Math.min(currentX1, currentX2);
            int max = (int) Math.max(currentX1, currentX2);
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
            int min = (int) Math.min(currentX1, currentX2);
            int max = (int) Math.max(currentX1, currentX2);
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
