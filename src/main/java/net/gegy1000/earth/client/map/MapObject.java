package net.gegy1000.earth.client.map;

import net.gegy1000.earth.server.util.MapPoint;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;

public interface MapObject {
    void render(Tessellator tessellator, VertexBuffer builder, MapPoint center);
}
