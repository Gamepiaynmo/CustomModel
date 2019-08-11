package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.CustomModel;
import com.github.gamepiaynmo.custommodel.CustomModelClient;
import com.github.gamepiaynmo.custommodel.render.CustomPlayerEntityRenderer;
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

    @Inject(method = "<init>(Lnet/minecraft/client/texture/TextureManager;Lnet/minecraft/client/render/item/ItemRenderer;Lnet/minecraft/resource/ReloadableResourceManager;)V", at = @At("RETURN"))
    public void init(TextureManager textureManager, ItemRenderer itemRenderer, ReloadableResourceManager manager, CallbackInfo info) {
        CustomModelClient.customRenderer = new CustomPlayerEntityRenderer((EntityRenderDispatcher) (Object) this);
        this.playerRenderer = CustomModelClient.customRenderer;
        this.modelRenderers.put("default", this.playerRenderer);
        this.modelRenderers.put("slim", new CustomPlayerEntityRenderer((EntityRenderDispatcher) (Object) this, true));
        CustomModelClient.textureManager = textureManager;
    }
}
