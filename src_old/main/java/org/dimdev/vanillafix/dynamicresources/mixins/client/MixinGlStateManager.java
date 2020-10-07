package org.dimdev.vanillafix.dynamicresources.mixins.client;

import net.minecraft.client.renderer.GlStateManager;
import org.dimdev.vanillafix.dynamicresources.TextureScaleInfo;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GlStateManager.class)
public abstract class MixinGlStateManager {
    @Shadow @Final private static GlStateManager.TextureState[] textureState;
    @Shadow private static int activeTextureUnit;

    private static boolean bound = false;
    private static int scaledAt = -1;

    @Overwrite // overwrite for efficiency
    public static void bindTexture(int texture) {
        if (texture != textureState[activeTextureUnit].textureName) {
            textureState[activeTextureUnit].textureName = texture;
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

            if (activeTextureUnit == 0) {
                if (texture == TextureScaleInfo.textureId) {
                    bound = true;
                    scaleTextures();
                } else if (bound) {
                    bound = false;
                    unscaleTextures();
                }
            }
        }
    }

    private static void scaleTextures() {
        int oldMatrixMode = GlStateManager.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glScaled(TextureScaleInfo.xScale, TextureScaleInfo.yScale, 0);
        GL11.glMatrixMode(oldMatrixMode);

        scaledAt = GL11.glGetInteger(GL11.GL_TEXTURE_STACK_DEPTH);
    }

    private static void unscaleTextures() {
        if (GL11.glGetInteger(GL11.GL_TEXTURE_STACK_DEPTH) != scaledAt) {
            throw new UnsupportedOperationException();
        }

        int oldMatrixMode = GlStateManager.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPopMatrix();
        GL11.glMatrixMode(oldMatrixMode);
    }
}
