package com.github.gamepiaynmo.custommodel.render.model;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;

public class CustomSheet implements ParticleTextureSheet {

    public static final CustomSheet CUSTOM_SHEET = new CustomSheet();

    @Override
    public void begin(BufferBuilder var1, TextureManager var2) {
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.alphaFunc(516, 0.003921569F);
        var1.begin(7, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
    }

    @Override
    public void draw(Tessellator var1) {
        var1.draw();
    }

    public String toString() {
        return "CUSTOM_SHEET";
    }
}
