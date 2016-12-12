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
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
import java.util.Set;

public class ClientEventHandler {
    public static boolean isMapEnabled;
    private static final Minecraft MC = Minecraft.getMinecraft();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGUIOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiMainMenu && !(event.getGui() instanceof EarthMainMenuGUI)) {
            event.setGui(new EarthMainMenuGUI());
        }
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (MC.world.getWorldType() instanceof WorldTypeEarth) {
            if (EarthKeyBinds.KEY_STREET_VIEW.isPressed()) {
                MC.displayGuiScreen(new StreetViewGUI(MC.player));
            } else if (EarthKeyBinds.KEY_TELEPORT_PLACE.isPressed()) {
                MC.displayGuiScreen(new TeleportPlaceGUI());
            } else if (EarthKeyBinds.KEY_SHOW_MAP.isPressed()) {
                MC.displayGuiScreen(new MapGUI(MC.player));
            } else if (EarthKeyBinds.KEY_SHOW_MAP_OVERLAY.isPressed()) {
                isMapEnabled = !isMapEnabled;
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        EntityPlayerSP player = MC.player;
        if (isMapEnabled && event.phase == TickEvent.Phase.END && player != null && player.ticksExisted % 10 == 0) {
            MapHandler.update(player);
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (isMapEnabled) {
            double viewX = TileEntityRendererDispatcher.staticPlayerX;
            double viewY = TileEntityRendererDispatcher.staticPlayerY;
            double viewZ = TileEntityRendererDispatcher.staticPlayerZ;
            ICamera camera = new Frustum();
            camera.setPosition(viewX, viewY, viewZ);

            GlStateManager.disableTexture2D();
            GlStateManager.disableCull();
            GlStateManager.enableFog();
            GlStateManager.enableBlend();
            GL11.glNormal3f(1.0F, 1.0F, 1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.enableStandardItemLighting();
            MC.entityRenderer.enableLightmap();
            synchronized (MapHandler.MAP_LOCK) {
                for (MapTile tile : MapHandler.MAP_TILES) {
                    Set<MapObject> mapObjects = tile.getMapObjects();
                    for (MapObject mapObject : mapObjects) {
                        if (camera.isBoundingBoxInFrustum(mapObject.getBounds())) {
                            if (mapObject.hasBuilt()) {
                                mapObject.enableState();
                                GlStateManager.pushMatrix();
                                Vector3d center = mapObject.getCenter();
                                GlStateManager.translate(center.x - viewX, center.y - viewY, center.z - viewZ);
                                mapObject.render();
                                GlStateManager.popMatrix();
                                mapObject.disableState();
                            } else {
                                mapObject.build();
                            }
                        }
                    }
                }
            }
            MC.entityRenderer.disableLightmap();
            GlStateManager.enableCull();
            GlStateManager.enableTexture2D();
            GlStateManager.disableFog();
        }
    }
}
