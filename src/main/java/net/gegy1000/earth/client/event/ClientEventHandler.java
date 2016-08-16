package net.gegy1000.earth.client.event;

import net.gegy1000.earth.client.gui.EarthMainMenuGUI;
import net.gegy1000.earth.client.gui.MapGUI;
import net.gegy1000.earth.client.gui.StreetViewGUI;
import net.gegy1000.earth.client.gui.TeleportPlaceGUI;
import net.gegy1000.earth.client.key.EarthKeyBinds;
import net.gegy1000.earth.client.map.MapOverlayHandler;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

public class ClientEventHandler {
    private static final Minecraft MC = Minecraft.getMinecraft();
    private boolean isMapOverlayEnabled;

    private int overlayDisplayList;
    private boolean overlayListCompiled;

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
                this.isMapOverlayEnabled = !this.isMapOverlayEnabled;
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (this.isMapOverlayEnabled && event.phase == TickEvent.Phase.END && MC.thePlayer != null && MC.thePlayer.ticksExisted % 10 == 0) {
            MapOverlayHandler.update();
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (this.isMapOverlayEnabled) {
            if (!this.overlayListCompiled) {
                this.overlayDisplayList = GLAllocation.generateDisplayLists(1);
                GlStateManager.glNewList(this.overlayDisplayList, GL11.GL_COMPILE);
                Tessellator tessellator = Tessellator.getInstance();
                VertexBuffer buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).endVertex();
                buffer.pos(1.0, 0.0, 0.0).tex(1.0, 0.0).endVertex();
                buffer.pos(1.0, 0.0, 1.0).tex(1.0, 1.0).endVertex();
                buffer.pos(0.0, 0.0, 1.0).tex(0.0, 1.0).endVertex();
                tessellator.draw();
                GlStateManager.glEndList();
                this.overlayListCompiled = true;
            }
            EntityPlayerSP player = MC.thePlayer;
            float partialTicks = LLibrary.PROXY.getPartialTicks();
            double viewX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
            double viewY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
            double viewZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
            GlStateManager.disableCull();
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            RenderHelper.enableStandardItemLighting();
            MC.entityRenderer.enableLightmap();
            GL11.glNormal3f(1.0F, 1.0F, 1.0F);
            int range = 22;
            for (int offsetX = -range - 1; offsetX <= range; offsetX++) {
                for (int offsetZ = -range - 1; offsetZ <= range; offsetZ++) {
                    BlockPos pos = new BlockPos((int) player.posX + offsetX, 0, (int) player.posZ + offsetZ);
                    double deltaX = viewX - (pos.getX() + 0.5);
                    double deltaY = ((viewY - player.getEyeHeight()) - (pos.getY() + 0.5)) * 0.1;
                    double deltaZ = viewZ - (pos.getZ() + 0.5);
                    double alpha = (1.0 - Math.min(1.0, Math.max(0.0, (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / (range * range)))) * 0.9;
                    if (alpha > 0.0) {
                        AdvancedDynamicTexture tile = MapOverlayHandler.get(pos);
                        if (tile != null) {
                            tile.bind();
                            GlStateManager.pushMatrix();
                            pos = player.worldObj.getTopSolidOrLiquidBlock(pos);
                            int light = player.worldObj.getCombinedLight(pos, 0);
                            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536.0F);
                            GlStateManager.color(1.0F, 1.0F, 1.0F, (float) alpha);
                            GlStateManager.translate(pos.getX() - viewX, pos.getY() - viewY + 0.008, pos.getZ() - viewZ);
                            GlStateManager.callList(this.overlayDisplayList);
                            GlStateManager.popMatrix();
                            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
                        }
                    }
                }
            }
            MC.entityRenderer.disableLightmap();
        }
    }
}
