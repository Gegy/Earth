package net.gegy1000.earth.client.event;

import net.gegy1000.earth.client.gui.EarthMainMenuGUI;
import net.gegy1000.earth.client.gui.MapGUI;
import net.gegy1000.earth.client.gui.StreetViewGUI;
import net.gegy1000.earth.client.gui.TeleportPlaceGUI;
import net.gegy1000.earth.client.key.EarthKeyBinds;
import net.gegy1000.earth.client.map.MapHandler;
import net.gegy1000.earth.client.map.MapObject;
import net.gegy1000.earth.client.map.MapTile;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.Set;

public class ClientEventHandler {
    private static final Minecraft MC = Minecraft.getMinecraft();
    private boolean isMapEnabled;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGUIOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiMainMenu && !(event.getGui() instanceof EarthMainMenuGUI)) {
            event.setGui(new EarthMainMenuGUI());
        }
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (MC.theWorld.getWorldType() instanceof WorldTypeEarth) {
            if (EarthKeyBinds.KEY_STREET_VIEW.isPressed()) {
                MC.displayGuiScreen(new StreetViewGUI(MC.thePlayer));
            } else if (EarthKeyBinds.KEY_TELEPORT_PLACE.isPressed()) {
                MC.displayGuiScreen(new TeleportPlaceGUI());
            } else if (EarthKeyBinds.KEY_SHOW_MAP.isPressed()) {
                MC.displayGuiScreen(new MapGUI(MC.thePlayer));
            } else if (EarthKeyBinds.KEY_SHOW_MAP_OVERLAY.isPressed()) {
                this.isMapEnabled = !this.isMapEnabled;
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        EntityPlayerSP player = MC.thePlayer;
        if (this.isMapEnabled && event.phase == TickEvent.Phase.END && player != null && player.ticksExisted % 10 == 0) {
            MapHandler.update(player);
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (this.isMapEnabled) {
            EntityPlayerSP player = MC.thePlayer;
            float partialTicks = event.getPartialTicks();
            double viewX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            double viewY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            double viewZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
            ICamera camera = new Frustum();
            camera.setPosition(viewX, viewY, viewZ);

            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.disableCull();
            GlStateManager.enableFog();
            GlStateManager.enableBlend();
            GlStateManager.translate(-viewX, -viewY, -viewZ);
            GL11.glNormal3f(1.0F, 1.0F, 1.0F);
            RenderHelper.enableStandardItemLighting();
            MC.entityRenderer.enableLightmap();
            synchronized (MapHandler.MAP_LOCK) {
                for (MapTile tile : MapHandler.MAP_TILES) {
                    Set<MapObject> mapObjects = tile.getMapObjects();
                    GlStateManager.enablePolygonOffset();
                    for (MapObject mapObject : mapObjects) {
                        if (camera.isBoundingBoxInFrustum(mapObject.getBounds())) {
                            if (mapObject.hasBuilt()) {
                                mapObject.enableState();
                                GlStateManager.pushMatrix();
                                GlStateManager.doPolygonOffset(1.0F, 0.0F);
                                mapObject.render();
                                GlStateManager.popMatrix();
                                mapObject.disableState();
                            } else {
                                mapObject.build();
                            }
                        }
                    }
                    GlStateManager.disablePolygonOffset();
                }
            }
            MC.entityRenderer.disableLightmap();
            GlStateManager.popMatrix();
            GlStateManager.enableCull();
            GlStateManager.enableTexture2D();
            GlStateManager.disableFog();
        }
    }

//    private void renderBuilding(Tessellator tessellator, VertexBuffer buffer, Building building) {
//        double height = building.getHeight() * 0.0225;
//        double lowestHeight = building.getLowestHeight();
//        double highestHeight = building.getHighestHeight();
//        double top = highestHeight + height;
//        List<MapPoint> points = building.getPoints();
//        buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_NORMAL);
//        for (MapPoint point : points) {
//            double x = point.getX();
//            double z = point.getZ();
//            buffer.pos(x, lowestHeight, z).normal(0.0F, 0.0F, 1.0F).endVertex();
//            buffer.pos(x, top, z).normal(0.0F, 0.0F, 1.0F).endVertex();
//        }
//        tessellator.draw();
//        GlStateManager.color(0.8F, 0.8F, 0.8F);
//        buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_NORMAL);
//        for (MapPoint point : points) {
//            buffer.pos(point.getX(), top, point.getZ()).normal(0.0F, 1.0F, 0.0F).endVertex();
//        }
//        tessellator.draw();
//                            /*GlStateManager.glLineWidth(10.0F);
//                            GlStateManager.color(1.0F, 1.0F, 1.0F);
//                            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
//                            int i = 0;
//                            for (MapPoint point : points) {
////                                if (point.getX() > 33169.1 && point.getX() < 33169.5 && point.getZ() > 61047.5 && point.getZ() < 61047.7) {
////                                    System.out.println();
////                                    System.out.println("======");
////                                    for (MapPoint p : points) {
////                                        System.out.println((p.getX() - 33169.1) + ", " + (p.getZ() - 61047.5));
////                                    }
////                                }
//                                buffer.pos(point.getX(), point.getY() + height, point.getZ()).color(0.0F, i < 2 ? 1.0F : 0.0F, i >= points.size() - 1 ? 1.0F : 0.0F, 1.0F).endVertex();
//                                i++;
//                            }
//                            tessellator.draw();*/
//    }
//
//    private void renderStreet(Tessellator tessellator, VertexBuffer buffer, int skip, Street street) {
//        if (street.isWaterway()) {
//            GlStateManager.doPolygonOffset(-1.0F, 3.0F);
//            GlStateManager.color(0.19F, 0.89F, 1.0F, 1.0F);
//        } else {
//            GlStateManager.doPolygonOffset(1.0F, 0.0F);
//            GlStateManager.color(0.1F, 0.1F, 0.1F, 1.0F);
//        }
//        int streetSkip = skip;
//        buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
//        List<MapPoint> points = street.getPoints();
//        if (points.size() < 10) {
//            streetSkip = 1;
//        }
//        double streetThickness = 0.1;
//        for (int i = 0; i < points.size(); i += streetSkip) {
//            if (i >= points.size()) {
//                i = points.size() - 1;
//            }
//            MapPoint point = points.get(i);
//            if (i < points.size() - 1) {
//                MapPoint next = points.get(i + 1);
//                double deltaX = next.getX() - point.getX();
//                double deltaZ = next.getZ() - point.getZ();
//                double length = Math.sqrt((deltaX * deltaX) + (deltaZ * deltaZ));
//                double offsetX = (streetThickness * deltaZ / length) / 2;
//                double offsetZ = (streetThickness * deltaX / length) / 2;
//                buffer.pos(point.getX() - offsetX, point.getY(), point.getZ() + offsetZ).endVertex();
//                buffer.pos(point.getX() + offsetX, point.getY(), point.getZ() - offsetZ).endVertex();
//                buffer.pos(next.getX() - offsetX, next.getY(), next.getZ() + offsetZ).endVertex();
//                buffer.pos(next.getX() + offsetX, next.getY(), next.getZ() - offsetZ).endVertex();
//            }
//        }
//        tessellator.draw();
//    }
}
