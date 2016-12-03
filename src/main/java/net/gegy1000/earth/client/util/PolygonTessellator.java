package net.gegy1000.earth.client.util;

import net.gegy1000.earth.server.util.MapPoint;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.GLUtessellator;
import org.lwjgl.util.glu.GLUtessellatorCallbackAdapter;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolygonTessellator {
    private static final GLUtessellator TESSELLATOR = GLU.gluNewTess();

    public static void draw(TessellationObject tessellationObject) {
        Callback callback = new Callback();
        TESSELLATOR.gluTessCallback(GLU.GLU_TESS_BEGIN_DATA, callback);
        TESSELLATOR.gluTessCallback(GLU.GLU_TESS_VERTEX_DATA, callback);
        TESSELLATOR.gluTessCallback(GLU.GLU_TESS_END_DATA, callback);
        TESSELLATOR.gluTessBeginPolygon(tessellationObject);
        TESSELLATOR.gluTessBeginContour();
        List<MapPoint> points = tessellationObject.getPoints();
        for (MapPoint point : points) {
            TESSELLATOR.gluTessVertex(new double[] { point.getX(), 0, point.getZ() }, 0, point);
        }
        TESSELLATOR.gluTessEndContour();
        TESSELLATOR.gluTessEndPolygon();
    }

    private static class Callback extends GLUtessellatorCallbackAdapter {
        @Override
        public void beginData(int type, Object polygonData) {
            TessellationObject tessellationObject = (TessellationObject) polygonData;
            tessellationObject.begin(type);
        }

        @Override
        public void vertexData(Object vertexData, Object polygonData) {
            TessellationObject tessellationObject = (TessellationObject) polygonData;
            MapPoint point = (MapPoint) vertexData;
            tessellationObject.vertex(point.getX(), point.getY(), point.getZ());
        }

        @Override
        public void endData(Object polygonData) {
            TessellationObject tessellationObject = (TessellationObject) polygonData;
            tessellationObject.end();
        }
    }

    public static class TessellationObject {
        private VertexBuffer builder;
        private List<net.minecraft.client.renderer.vertex.VertexBuffer> buffers;
        private Map<net.minecraft.client.renderer.vertex.VertexBuffer, Integer> types = new HashMap<>();
        private List<MapPoint> points;
        private Vector3d center;
        private VertexFormat format;
        private double height;

        public TessellationObject(VertexFormat format, VertexBuffer builder, List<MapPoint> points, Vector3d center, double height) {
            this.format = format;
            this.builder = builder;
            this.points = points;
            this.center = center;
            this.height = height;
            this.buffers = new ArrayList<>();
        }

        public VertexBuffer getBuilder() {
            return this.builder;
        }

        public List<net.minecraft.client.renderer.vertex.VertexBuffer> getBuffers() {
            return this.buffers;
        }

        public List<MapPoint> getPoints() {
            return this.points;
        }

        public Vector3d getCenter() {
            return this.center;
        }

        public VertexFormat getFormat() {
            return this.format;
        }

        public void begin(int type) {
            net.minecraft.client.renderer.vertex.VertexBuffer buffer = new net.minecraft.client.renderer.vertex.VertexBuffer(this.format);
            this.buffers.add(buffer);
            this.types.put(buffer, type);
            buffer.bindBuffer();
            this.builder.setTranslation(-this.center.x, -this.center.y, -this.center.z);
            this.builder.begin(type, this.format);
        }

        public void end() {
            net.minecraft.client.renderer.vertex.VertexBuffer buffer = this.getBuffer();
            this.builder.finishDrawing();
            this.builder.setTranslation(0, 0, 0);
            this.builder.reset();
            buffer.bufferData(this.builder.getByteBuffer());
            buffer.unbindBuffer();
        }

        public void vertex(double x, double y, double z) {
            this.builder.pos(x, this.height > 0 ? this.height : y, z);
            if (this.format.hasNormal()) {
                this.builder.normal(0.0F, 1.0F, 0.0F);
            }
            this.builder.endVertex();
        }

        private net.minecraft.client.renderer.vertex.VertexBuffer getBuffer() {
            return this.buffers.get(this.buffers.size() - 1);
        }

        public void draw() {
            for (net.minecraft.client.renderer.vertex.VertexBuffer buffer : this.buffers) {
                buffer.bindBuffer();
                GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 16, 0);
                if (this.format.hasNormal()) {
                    GL11.glNormalPointer(GL11.GL_BYTE, 16, 12);
                }
                buffer.drawArrays(this.types.get(buffer));
                buffer.unbindBuffer();
            }
        }

        public void delete() {
            for (net.minecraft.client.renderer.vertex.VertexBuffer buffer : this.buffers) {
                buffer.deleteGlBuffers();
            }
        }
    }
}
