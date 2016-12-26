package net.gegy1000.earth.server.util;

import com.vividsolutions.jts.geom.Coordinate;

import java.util.List;

public class Triangulate {
    private static final double EPSILON = 0.0000000001F;

    public static boolean process(List<Coordinate> contour, List<Coordinate> result) {
        int contourLength = contour.size();
        if (contourLength < 3) {
            return false;
        }

        int[] indices = new int[contourLength];

        if (Triangulate.area(contour) > 0.0F) {
            for (int i = 0; i < contourLength; i++) {
                indices[i] = i;
            }
        } else {
            for (int i = 0; i < contourLength; i++) {
                indices[i] = (contourLength - 1) - i;
            }
        }

        int count = contourLength * 2;

        for (int index = contourLength - 1; contourLength > 2; ) {
            if (count-- <= 0) {
                return false;
            }

            int lastIndex = index;
            if (lastIndex >= contourLength) {
                lastIndex = 0;
            }
            index = lastIndex + 1;
            if (index >= contourLength) {
                index = 0;
            }
            int nextIndex = index + 1;
            if (nextIndex >= contourLength) {
                nextIndex = 0;
            }

            if (Triangulate.snip(contour, lastIndex, index, nextIndex, contourLength, indices)) {
                int lastVertex = indices[lastIndex];
                int vertex = indices[index];
                int nextVertex = indices[nextIndex];

                result.add(contour.get(lastVertex));
                result.add(contour.get(vertex));
                result.add(contour.get(nextVertex));

                for (int current = index, next = index + 1; next < contourLength; current++, next++) {
                    indices[current] = indices[next];
                }
                contourLength--;

                count = 2 * contourLength;
            }
        }

        return true;
    }

    public static double area(List<Coordinate> contour) {
        int count = contour.size();
        double area = 0.0F;
        for (int previous = count - 1, current = 0; current < count; previous = current++) {
            area += contour.get(previous).x * contour.get(current).z - contour.get(current).x * contour.get(previous).z;
        }
        return area * 0.5F;
    }

    public static boolean inside(double lastX, double lastY, double x, double y, double nextX, double nextY, double px, double py) {
        double as_x = px - lastX;
        double as_y = py - lastY;
        boolean p_ab = (x - lastX) * as_y - (y - lastY) * as_x > 0;
        return (nextX - lastX) * as_y - (nextY - lastY) * as_x > 0 != p_ab && (nextX - x) * (py - y) - (nextY - y) * (px - x) > 0 == p_ab;
    }

    private static boolean snip(List<Coordinate> contour, int lastIndex, int index, int nextIndex, int count, int[] indices) {
        double lastX = contour.get(indices[lastIndex]).x;
        double lastY = contour.get(indices[lastIndex]).z;
        double x = contour.get(indices[index]).x;
        double y = contour.get(indices[index]).z;
        double nextX = contour.get(indices[nextIndex]).x;
        double nextY = contour.get(indices[nextIndex]).z;
        if ((x - lastX) * (nextY - lastY) - (y - lastY) * (nextX - lastX) < EPSILON) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            if (i == lastIndex || i == index || i == nextIndex) {
                continue;
            }
            double px = contour.get(indices[i]).x;
            double py = contour.get(indices[i]).z;
            if (inside(lastX, lastY, x, y, nextX, nextY, px, py)) {
                return false;
            }
        }
        return true;
    }
}