package com.github.gamepiaynmo.custommodel.render;

import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;

public class CustomTexture extends AbstractTexture {

    private NativeImage image;

    public CustomTexture(NativeImage image) {
        this.image = image;
    }

    @Override
    public void load(ResourceManager var1) throws IOException {
        bindTexture();
        TextureUtil.prepareImage(getGlId(), image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, false);
    }
}
