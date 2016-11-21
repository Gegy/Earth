package net.gegy1000.earth.client.map;

import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.math.AxisAlignedBB;

import javax.vecmath.Vector3d;

public interface MapObject {
    net.minecraft.client.renderer.VertexBuffer BUILDER = new net.minecraft.client.renderer.VertexBuffer(0xFFFF);

    boolean hasBuilt();

    void build();

    void render();

    void enableState();
    void disableState();

    void delete();

    Vector3d getCenter();

    AxisAlignedBB getBounds();

    default void finish(VertexBuffer buffer) {
        BUILDER.finishDrawing();
        BUILDER.setTranslation(0, 0, 0);
        BUILDER.reset();
        buffer.bufferData(BUILDER.getByteBuffer());
    }
}
