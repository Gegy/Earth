package net.gegy1000.earth.client.event;

import net.gegy1000.earth.Earth;
import net.gegy1000.earth.client.gui.EarthMainMenuGUI;
import net.gegy1000.earth.client.gui.MapGUI;
import net.gegy1000.earth.client.gui.TeleportPlaceGUI;
import net.gegy1000.earth.client.key.EarthKeyBinds;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.earth.server.util.MapPoint;
import net.gegy1000.earth.server.util.google.StreetView;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;

public class ClientEventHandler {
    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final double STREET_VIEW_SCALE = 30.0;

    private boolean streetViewEnabled;
    private boolean streetViewDownloaded;
    private float streetViewOpacity;
    private float lastStreetViewOpacity;
    private AdvancedDynamicTexture[] streetView = null;
    private BufferedImage[] streetViewImages = new BufferedImage[6];

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGUIOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiMainMenu && !(event.getGui() instanceof EarthMainMenuGUI)) {
            event.setGui(new EarthMainMenuGUI());
        }
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (MC.world.getWorldType() instanceof WorldTypeEarth) {
            EntityPlayerSP player = MC.player;
            if (EarthKeyBinds.KEY_STREET_VIEW.isPressed()) {
                if (this.streetViewDownloaded || !this.streetViewEnabled) {
                    MapPoint point = new MapPoint(player.world, player.posX, player.posY, player.posZ);
                    this.streetViewEnabled = !this.streetViewEnabled;
                    this.streetViewDownloaded = false;
                    this.streetView = null;
                    if (this.streetViewEnabled) {
                        Thread thread = new Thread(() -> {
                            float[] faceYaw = new float[] { -180.0F, -90.0F, 0.0F, 90.0F, 0.0F, 0.0F };
                            float[] facePitch = new float[] { 0.0F, 0.0F, 0.0F, 0.0F, -90.0F, 90.0F };
                            for (int i = 0; i < 6; i++) {
                                float yaw = faceYaw[i];
                                float pitch = facePitch[i];
                                try {
                                    StreetView streetView = StreetView.get(point, yaw, pitch);
                                    this.streetViewImages[i] = streetView.getImage();
                                    Earth.LOGGER.info("Downloaded " + i);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            this.streetViewDownloaded = true;
                        });
                        thread.setDaemon(true);
                        thread.setName("StreetView Download Thread");
                        thread.start();
                    }
                }
            } else if (EarthKeyBinds.KEY_TELEPORT_PLACE.isPressed()) {
                MC.displayGuiScreen(new TeleportPlaceGUI());
            } else if (EarthKeyBinds.KEY_SHOW_MAP.isPressed()) {
                MC.displayGuiScreen(new MapGUI(player));
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!this.streetViewEnabled) {
            if (this.streetViewOpacity > 0.0F) {
                this.streetViewOpacity -= 0.02F;
            } else {
                if (this.streetView != null) {
                    for (AdvancedDynamicTexture texture : this.streetView) {
                        if (texture != null) {
                            texture.delete();
                        }
                    }
                    this.streetView = null;
                }
            }
        } else if (this.streetViewDownloaded && this.streetViewOpacity < 1.0F) {
            this.streetViewOpacity += 0.02F;
        }
        this.lastStreetViewOpacity = this.streetViewOpacity;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (this.streetViewOpacity > 0.0F) {
            if (this.streetView == null) {
                this.streetView = new AdvancedDynamicTexture[6];
                for (int i = 0; i < 6; i++) {
                    BufferedImage image = this.streetViewImages[i];
                    this.streetView[i] = new AdvancedDynamicTexture("street_view_" + i, image);
                }
            }
            float partialTicks = event.getPartialTicks();
            float opacity = this.lastStreetViewOpacity + (this.streetViewOpacity - this.lastStreetViewOpacity) * partialTicks;
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GlStateManager.disableCull();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, opacity);
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer buffer = tessellator.getBuffer();
            for (int i = 0; i < 6; i++) {
                this.renderFace(tessellator, buffer, i);
            }
            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            GlStateManager.enableCull();
            GlStateManager.popMatrix();
        }
    }

    private void renderFace(Tessellator tessellator, VertexBuffer builder, int side) {
        AdvancedDynamicTexture texture = this.streetView[side];
        texture.bind();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        double scale = STREET_VIEW_SCALE;
        if (side == 0) {
            builder.pos(-scale, -scale, scale).tex(1.0, 1.0).endVertex();
            builder.pos(-scale, scale, scale).tex(1.0, 0.0).endVertex();
            builder.pos(scale, scale, scale).tex(0.0, 0.0).endVertex();
            builder.pos(scale, -scale, scale).tex(0.0, 1.0).endVertex();
        } else if (side == 1) {
            builder.pos(-scale, -scale, -scale).tex(1.0, 1.0).endVertex();
            builder.pos(-scale, -scale, scale).tex(0.0, 1.0).endVertex();
            builder.pos(-scale, scale, scale).tex(0.0, 0.0).endVertex();
            builder.pos(-scale, scale, -scale).tex(1.0, 0.0).endVertex();
        } else if (side == 2) {
            builder.pos(-scale, -scale, -scale).tex(0.0, 1.0).endVertex();
            builder.pos(-scale, scale, -scale).tex(0.0, 0.0).endVertex();
            builder.pos(scale, scale, -scale).tex(1.0, 0.0).endVertex();
            builder.pos(scale, -scale, -scale).tex(1.0, 1.0).endVertex();
        } else if (side == 3) {
            builder.pos(scale, -scale, -scale).tex(0.0, 1.0).endVertex();
            builder.pos(scale, -scale, scale).tex(1.0, 1.0).endVertex();
            builder.pos(scale, scale, scale).tex(1.0, 0.0).endVertex();
            builder.pos(scale, scale, -scale).tex(0.0, 0.0).endVertex();
        } else if (side == 4) {
            builder.pos(-scale, -scale, -scale).tex(0.0, 0.0).endVertex();
            builder.pos(-scale, -scale, scale).tex(0.0, 1.0).endVertex();
            builder.pos(scale, -scale, scale).tex(1.0, 1.0).endVertex();
            builder.pos(scale, -scale, -scale).tex(1.0, 0.0).endVertex();
        } else if (side == 5) {
            builder.pos(-scale, scale, -scale).tex(0.0, 1.0).endVertex();
            builder.pos(-scale, scale, scale).tex(0.0, 0.0).endVertex();
            builder.pos(scale, scale, scale).tex(1.0, 0.0).endVertex();
            builder.pos(scale, scale, -scale).tex(1.0, 1.0).endVertex();
        }
        tessellator.draw();
    }
}
