package com.github.gamepiaynmo.custommodel.render;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class CustomTexture extends AbstractTexture {

    private BufferedImage image;

    public CustomTexture(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void loadTexture(IResourceManager var1) throws IOException {
        TextureUtil.uploadTextureImageAllocate(getGlTextureId(), image, false, false);
    }
}
