package net.gegy1000.earth.client.gui;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.gegy1000.earth.Earth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;

import java.util.List;
import java.util.Random;

public class EarthMainMenuGUI extends GuiMainMenu {
    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final Sphere EARTH_SPHERE = new Sphere();
    private static final ResourceLocation EARTH_TEXTURE = new ResourceLocation(Earth.MODID, "textures/gui/earth.png");
    private static final ResourceLocation CLOUDS_TEXTURE = new ResourceLocation(Earth.MODID, "textures/gui/clouds.png");
    private static final ResourceLocation MOON_TEXTURE = new ResourceLocation(Earth.MODID, "textures/gui/moon.png");
    private static final ResourceLocation MINECRAFT_TITLE_TEXTURE = new ResourceLocation("textures/gui/title/minecraft.png");

    private boolean earthCompiled;
    private int earthList;

    private boolean starsCompiled;
    private int starList;

    private int timer;

    private boolean updateButtons;

    public EarthMainMenuGUI() {
        super();
    }

    @Override
    public void initGui() {
        super.initGui();
        this.updateButtons = true;
        if (this.earthCompiled) {
            GLAllocation.deleteDisplayLists(this.earthList);
        }
        this.earthCompiled = false;
    }

    @Override
    public void updateScreen() {
        this.timer++;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.updateButtons) {
            this.updateButtons = false;
            int baseY = this.height / 4 + 48;
            for (GuiButton button : this.buttonList) {
                if (button.id == 1 || button.id == 2) {
                    button.xPosition = 10;
                    button.width = 90;
                } else if (button.id == 0 || button.id == 4) {
                    button.yPosition = this.height - 30;
                } else if (button.id == 14 || button.id == 6) {
                    if (button.id == 6) {
                        button.yPosition = baseY;
                    } else {
                        button.yPosition = baseY + 24;
                    }
                    button.xPosition = this.width - 100;
                    button.width = 90;
                } else if (button.id == 5) {
                    button.xPosition = 10;
                    button.yPosition = baseY + 48;
                } else if (button.id == 2116463954) {
                    button.xPosition = 34;
                } else {
                    System.out.println(button.id);
                }
            }
        }
        ScaledResolution resolution = new ScaledResolution(MC);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (!this.earthCompiled) {
            this.earthList = GLAllocation.generateDisplayLists(1);
            this.earthCompiled = true;
            GlStateManager.glNewList(this.earthList, GL11.GL_COMPILE);
            GlStateManager.pushMatrix();
            EARTH_SPHERE.setTextureFlag(true);
            EARTH_SPHERE.draw(resolution.getScaleFactor() * 35.0F, 32, 32);
            GlStateManager.popMatrix();
            GlStateManager.glEndList();
        }
        this.drawGradientRect(0, 0, this.width, this.height, 0xFF000000, 0xFF000000);
        float timer = this.timer + partialTicks;

        Tessellator tesselator = Tessellator.getInstance();
        VertexBuffer buffer = tesselator.getBuffer();

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();

        GlStateManager.disableTexture2D();

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
        GlStateManager.rotate(timer * 0.001F, 0.0F, 1.0F, 0.0F);
        this.renderStars(tesselator, buffer);
        GlStateManager.popMatrix();

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();

        GlStateManager.pushMatrix();
        MC.getTextureManager().bindTexture(EARTH_TEXTURE);
        GlStateManager.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
        GlStateManager.rotate(timer, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.callList(this.earthList);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        MC.getTextureManager().bindTexture(CLOUDS_TEXTURE);
        GlStateManager.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
        GlStateManager.rotate(timer * 1.1F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(1.025F, 1.025F, 1.025F);
        GlStateManager.callList(this.earthList);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        MC.getTextureManager().bindTexture(MOON_TEXTURE);
        GlStateManager.translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
        GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(timer * 2.5F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(200.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.25F, 0.25F, 0.25F);
        GlStateManager.callList(this.earthList);
        GlStateManager.popMatrix();

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2.0F, this.height / 2.0F, -100.0F);
        GlStateManager.scale(0.54F, 0.54F, 0.54F);
        for (int i = 0; i < 80; i++) {
            GlStateManager.scale(1.01F, 1.01F, 1.01F);
            GlStateManager.color(i / 190.0F, 0.6F, 1.0F, 0.11F);
            GlStateManager.callList(this.earthList);
        }
        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.disableDepth();

        GlStateManager.pushMatrix();
        this.mc.getTextureManager().bindTexture(MINECRAFT_TITLE_TEXTURE);
        GlStateManager.translate(0.0F, MathHelper.sin(timer * 0.1F) * 3.5F, 0.0F);
        int titleCenterX = this.width / 2 - 137;
        this.drawTexturedModalRect(titleCenterX, 15, 0, 0, 155, 44);
        this.drawTexturedModalRect(titleCenterX + 155, 15, 0, 45, 155, 44);
        GlStateManager.popMatrix();
        List<String> brandings = Lists.reverse(FMLCommonHandler.instance().getBrandings(true));
        int brandingIndex = 0;
        for (String branding : brandings) {
            if (!Strings.isNullOrEmpty(branding) && !branding.startsWith("Powered by") && !branding.startsWith("MCP")) {
                this.drawString(this.fontRendererObj, branding, 2, brandingIndex * (this.fontRendererObj.FONT_HEIGHT + 1) + 2, 0xFFFFFF);
                brandingIndex++;
            }
        }
        String copyright = "Copyright Mojang AB. Do not distribute!";
        this.drawString(this.fontRendererObj, copyright, this.width - this.fontRendererObj.getStringWidth(copyright) - 2, 2, 0xFFFFFF);
        for (GuiButton button : this.buttonList) {
            button.drawButton(this.mc, mouseX, mouseY);
        }
        for (GuiLabel label : this.labelList) {
            label.drawLabel(this.mc, mouseX, mouseY);
        }
    }

    private void renderStars(Tessellator tesselator, VertexBuffer vertexBuffer) {
        if (!this.starsCompiled) {
            this.starsCompiled = true;
            this.starList = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(this.starList, GL11.GL_COMPILE);
            Random random = new Random(10842L);
            vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            for (int star = 0; star < 10000; ++star) {
                double starX = random.nextFloat() * 2.0F - 1.0F;
                double starY = random.nextFloat() * 2.0F - 1.0F;
                double starZ = random.nextFloat() * 2.0F - 1.0F;
                double starSize = (0.55F + random.nextFloat() * 0.1F);
                double delta = starX * starX + starY * starY + starZ * starZ;
                if (delta < 1.0D && delta > 0.01D) {
                    delta = 1.0D / Math.sqrt(delta);
                    starX = starX * delta;
                    starY = starY * delta;
                    starZ = starZ * delta;
                    double renderStarX = starX * 1000.0D;
                    double renderStarY = starY * 1000.0D;
                    double renderStarZ = starZ * 1000.0D;
                    double angle = Math.atan2(starX, starZ);
                    double d9 = Math.sin(angle);
                    double d10 = Math.cos(angle);
                    double d11 = Math.atan2(Math.sqrt(starX * starX + starZ * starZ), starY);
                    double d12 = Math.sin(d11);
                    double d13 = Math.cos(d11);
                    double randomAngle = random.nextDouble() * Math.PI * 2.0D;
                    double randomAngleSin = Math.sin(randomAngle);
                    double randomAngleCos = Math.cos(randomAngle);
                    for (int vertex = 0; vertex < 4; ++vertex) {
                        double d18 = ((vertex & 2) - 1) * starSize;
                        double d19 = ((vertex + 1 & 2) - 1) * starSize;
                        double d21 = d18 * randomAngleCos - d19 * randomAngleSin;
                        double d22 = d19 * randomAngleCos + d18 * randomAngleSin;
                        double vertexY = d21 * d12 + 0.0D * d13;
                        double d24 = 0.0D * d12 - d21 * d13;
                        double vertexX = d24 * d9 - d22 * d10;
                        double vertexZ = d22 * d9 + d24 * d10;
                        vertexBuffer.pos(renderStarX + vertexX, renderStarY + vertexY, renderStarZ + vertexZ).endVertex();
                    }
                }
            }
            tesselator.draw();
            GlStateManager.glEndList();
        } else {
            GlStateManager.callList(this.starList);
        }
    }
}
