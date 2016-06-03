package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.google.geocode.GeoCode;
import net.gegy1000.earth.google.streetview.StreetView;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class GuiTeleportPlace extends GuiScreen {
    private GuiTextField placeField;
    private DynamicTexture dynamicTexture;
    private ResourceLocation location;
    private BufferedImage image;

    private String place = "";

    @Override
    public void initGui() {
        this.buttonList.clear();

        ScaledResolution resolution = new ScaledResolution(mc);

        int scaledWidth = resolution.getScaledWidth();
        int scaledHeight = resolution.getScaledHeight();

        this.buttonList.add(new GuiButton(0, (scaledWidth / 2) - 100, scaledHeight - 30, "Teleport To"));
        this.buttonList.add(new GuiButton(1, (scaledWidth / 2) - 100, (scaledHeight / 4) + 10, "Preview"));

        this.placeField = new GuiTextField(9, this.fontRendererObj, this.width / 2 - 200, (scaledHeight / 4) - 20, 400, 20);
        this.placeField.setFocused(true);
        this.placeField.setText(place);
        this.placeField.setMaxStringLength(255);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        super.drawScreen(mouseX, mouseY, partialTicks);

        ScaledResolution resolution = new ScaledResolution(mc);

        int scaledWidth = resolution.getScaledWidth();
        int scaledHeight = resolution.getScaledHeight();

        if (dynamicTexture == null) {
            if (image != null) {
                dynamicTexture = new DynamicTexture(image);
                location = mc.getTextureManager().getDynamicTextureLocation("streetview_image", dynamicTexture);
            }
        } else {
            mc.getTextureManager().bindTexture(location);

            double scaleX = (scaledWidth / 640.0) * 0.4;
            double scaleY = (scaledHeight / 320.0) * 0.4;

            drawTexturedModalRect((int) ((scaledWidth / 2) - ((640.0 * scaleX) / 2)), (int) ((scaledHeight / 2) - ((320.0 * scaleY) / 2)) + 30, 0, 0, 640, 320, 640, 320, scaleX, scaleY);
        }

        drawCenteredString(fontRendererObj, "Teleport to a Place", scaledWidth / 2, 15, 0xFFFFFF);

        this.placeField.drawTextBox();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (this.placeField.isFocused()) {
            this.placeField.textboxKeyTyped(typedChar, keyCode);
        }

        this.place = placeField.getText();

        if (keyCode == 28 || keyCode == 156) {
            this.actionPerformed(this.buttonList.get(0));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.placeField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        try {
            GeoCode geoCode = GeoCode.get(place);

            if (geoCode != null) {
                final double latitude = geoCode.getLatitude();
                final double longitude = geoCode.getLongitude();

                if (button.id == 0) {
                    mc.thePlayer.sendChatMessage("/tplatlong " + latitude + " " + longitude);
                    mc.displayGuiScreen(null);
                } else if (button.id == 1) {
                    image = null;
                    dynamicTexture = null;

                    Thread downloadThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                StreetView streetView = StreetView.get(latitude, longitude, 180, 0);
                                image = streetView.getImage();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    downloadThread.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateScreen() {
        boolean hasText = this.placeField.getText().length() > 0;

        this.buttonList.get(0).enabled = hasText;
        this.buttonList.get(1).enabled = hasText;

        this.placeField.updateCursorCounter();
    }

    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height, int texWidth, int texHeight, double scaleX, double scaleY) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GlStateManager.scale(scaleX, scaleY, 0.0);

        x /= scaleX;
        y /= scaleY;

        float f = 1.0F / texWidth;
        float f1 = 1.0F / texHeight;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, this.zLevel).tex(textureX * f, (textureY + height) * f1).endVertex();
        buffer.pos(x + width, y + height, this.zLevel).tex((textureX + width) * f, (textureY + height) * f1).endVertex();
        buffer.pos(x + width, y, this.zLevel).tex((textureX + width) * f, (textureY * f1)).endVertex();
        buffer.pos(x, y, this.zLevel).tex(textureX * f, textureY * f1).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        mc.getTextureManager().deleteTexture(location);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
