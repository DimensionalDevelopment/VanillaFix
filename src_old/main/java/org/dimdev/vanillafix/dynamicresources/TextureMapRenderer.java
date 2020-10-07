package org.dimdev.vanillafix.dynamicresources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;

public class TextureMapRenderer {
    private static final DynamicStitcher stitcher;
    static {
        try {
            DynamicTextureMap map = (DynamicTextureMap) Minecraft.getMinecraft().getTextureMapBlocks();
            Field stitcherField = DynamicTextureMap.class.getDeclaredField("stitcher");
            stitcherField.setAccessible(true);
            stitcher = (DynamicStitcher) stitcherField.get(map);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public static void draw() {
        float alpha = 1;
        if (GuiScreen.isCtrlKeyDown()) {
            alpha = 0.5f;
        }

        // Bind texture atlas and disable scaling for debug render (see MixinGlStateManager)
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        pushTextureIdentity();

        Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableColorLogic();
        GlStateManager.color(1, 1, 1, alpha);
        drawTexture(5, 5, 0, 0, 230 * stitcher.getImageWidth() / stitcher.getImageHeight(), 230, 1, 1);

        popTextureMatrix();
    }

    private static void drawTexture(int x, int y, int textureX, int textureY, int width, int height, int textureWidth, int textureHeight) {
        double zLevel = 0;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, zLevel).tex(textureX, textureY + textureHeight).endVertex();
        buffer.pos(x + width, y + height, zLevel).tex(textureX + textureWidth, textureY + textureHeight).endVertex();
        buffer.pos(x + width, y, zLevel).tex(textureX + textureWidth, textureY).endVertex();
        buffer.pos(x, y, zLevel).tex(textureX, textureY).endVertex();
        tessellator.draw();
    }

    public static void pushTextureIdentity() {
        int oldMatrixMode = GlStateManager.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glMatrixMode(oldMatrixMode);
    }

    public static void popTextureMatrix() {
        int oldMatrixMode;
        oldMatrixMode = GlStateManager.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPopMatrix();
        GL11.glMatrixMode(oldMatrixMode);
    }
}
