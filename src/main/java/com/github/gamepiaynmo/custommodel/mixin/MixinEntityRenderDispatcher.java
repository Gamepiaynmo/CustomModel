package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    @Shadow
    private Map<String, PlayerEntityRenderer> modelRenderers;
    @Shadow
    private PlayerEntityRenderer playerRenderer;

    @Inject(method = "registerRenderers", at = @At("RETURN"))
    public void init(ItemRenderer itemRenderer, ReloadableResourceManager reloadableResourceManager, CallbackInfo info) {
        CustomModelClient.textureManager = MinecraftClient.getInstance().getTextureManager();
        CustomModelClient.playerRenderers = this.modelRenderers;
    }
}
