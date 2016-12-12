package net.gegy1000.earth.client.map;

import net.gegy1000.earth.server.util.MapPoint;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;

public class Way implements MapObject {
    private final EarthGenerator generator;
    private final String name;
    private final List<MapPoint> points;
    private final double width;
    private final Type type;

    public Way(World world, String name, List<MapPoint> points, double width, Type type) {
        this.name = name;
        this.generator = WorldTypeEarth.getGenerator(world);
        this.points = points;
        this.type = type;
        if (this.type == Type.PATH) {
            width /= 2;
        }
        this.width = width / this.generator.getRatio();
    }

    public String getName() {
        return this.name;
    }

    public List<MapPoint> getPoints() {
        return this.points;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public void render(Tessellator tessellator, VertexBuffer builder, MapPoint center) {
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
        float red = this.type.red;
        float green = this.type.green;
        float blue = this.type.blue;

        builder.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);
        builder.setTranslation(-center.getX(), 0, -center.getZ());

        for (int i = 0; i < this.points.size() - 1; i++) {
            MapPoint point = this.points.get(i);
            MapPoint next = this.points.get(i + 1);
            double deltaX = next.getX() - point.getX();
            double deltaZ = next.getZ() - point.getZ();
            double length = Math.sqrt((deltaX * deltaX) + (deltaZ * deltaZ));
            double offsetX = (this.width * deltaZ / length) / 2;
            double offsetZ = (this.width * deltaX / length) / 2;
            builder.pos(point.getX() - offsetX, point.getY(), point.getZ() + offsetZ).color(red, green, blue, 1.0F).endVertex();
            builder.pos(point.getX() + offsetX, point.getY(), point.getZ() - offsetZ).color(red, green, blue, 1.0F).endVertex();
            builder.pos(next.getX() - offsetX, next.getY(), next.getZ() + offsetZ).color(red, green, blue, 1.0F).endVertex();
            builder.pos(next.getX() + offsetX, next.getY(), next.getZ() - offsetZ).color(red, green, blue, 1.0F).endVertex();
        }

        tessellator.draw();

        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.disableCull();
    }

    public enum Type implements MapObjectType<Way> {
        DEFAULT(255, 0, 0),
        LARGE_ROAD(64, 64, 64),
        ROAD(0, 0, 0),
        RAILWAY(200, 128, 0),
        STREAM(0, 140, 255),
        PATH(110, 60, 20);

        private float red;
        private float green;
        private float blue;

        Type(int red, int green, int blue) {
            this.red = red / 255.0F;
            this.green = green / 255.0F;
            this.blue = blue / 255.0F;
        }

        @Override
        public Way create(Map<String, String> tags, World world, List<MapPoint> points) {
            String name = tags.get("name");
            String highway = tags.get("highway");
            String waterway = tags.get("waterway");
            String railway = tags.get("railway");
            if (highway != null) {
                int lanes = 1;
                if (tags.containsKey("lanes")) {
                    try {
                        lanes = Integer.parseInt(tags.get("lanes"));
                    } catch (NumberFormatException e) {
                    }
                }
                Type type = ROAD;
                switch (highway) {
                    case "motorway_link":
                    case "primary_link":
                    case "primary":
                    case "motorway":
                        type = LARGE_ROAD;
                        break;
                    case "footway":
                    case "path":
                    case "steps":
                    case "pedestrian":
                    case "track":
                        type = PATH;
                        break;
                }
                return new Way(world, name, points, Math.max(1, lanes) * 3.0, type);
            } else if (waterway != null) {
                return new Way(world, name, points, 3.0, STREAM);
            } else if (railway != null) {
                return new Way(world, name, points, 2.0, RAILWAY);
            }
            return null;
        }
    }
}
