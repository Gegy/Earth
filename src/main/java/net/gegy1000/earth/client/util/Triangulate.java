package net.gegy1000.earth.client.util;

import javax.vecmath.Vector2f;
import java.util.List;

public class Triangulate {
    private static final float EPSILON = 0.0000000001F;

    public static boolean process(List<Vector2f> contour, List<Vector2f> result) {
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
            if (contourLength <= lastIndex) {
                lastIndex = 0;
            }
            index = lastIndex + 1;
            if (contourLength <= index) {
                index = 0;
            }
            int nextIndex = index + 1;
            if (contourLength <= nextIndex) {
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

    public static float area(List<Vector2f> contour) {
        int count = contour.size();
        float area = 0.0F;
        for (int previous = count - 1, current = 0; current < count; previous = current++) {
            area += contour.get(previous).getX() * contour.get(current).getY() - contour.get(current).getX() * contour.get(previous).getY();
        }
        return area * 0.5F;
    }

    public static boolean inside(float lastX, float lastY, float x, float y, float nextX, float nextY, float px, float py) {
        float aCrossBp = (nextX - x) * (py - y) - (nextY - y) * (px - x);
        float bCrossCp = (lastX - nextX) * (py - nextY) - (lastY - nextY) * (px - nextX);
        float cCrossAp = (x - lastX) * (py - lastY) - (y - lastY) * (px - lastX);

        return ((aCrossBp >= 0.0F) && (bCrossCp >= 0.0F) && (cCrossAp >= 0.0F));
    }

    private static boolean snip(List<Vector2f> contour, int lastIndex, int index, int nextIndex, int count, int[] indices) {
        float lastX = contour.get(indices[lastIndex]).getX();
        float lastY = contour.get(indices[lastIndex]).getY();
        float x = contour.get(indices[index]).getX();
        float y = contour.get(indices[index]).getY();
        float nextX = contour.get(indices[nextIndex]).getX();
        float nextY = contour.get(indices[nextIndex]).getY();
        if ((x - lastX) * (nextY - lastY) - (y - lastY) * (nextX - lastX) < EPSILON) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            if (i == lastIndex || i == index || i == nextIndex) {
                continue;
            }
            float px = contour.get(indices[i]).getX();
            float py = contour.get(indices[i]).getY();
            if (inside(lastX, lastY, x, y, nextX, nextY, px, py)) {
                return false;
            }
        }
        return true;
    }
}
