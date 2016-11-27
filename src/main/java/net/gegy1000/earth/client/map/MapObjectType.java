package net.gegy1000.earth.client.map;

import net.gegy1000.earth.server.util.MapPoint;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

public interface MapObjectType<M extends MapObject> {
    M create(Map<String, String> tags, World world, List<MapPoint> points);
    void prepareRender();
}
