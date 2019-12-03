package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.render.EntityParameter;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class MixinInventoryScreen {
    @Inject(method = "drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"))
    private static void drawEntityHead(int int_1, int int_2, int int_3, float float_1, float float_2, LivingEntity livingEntity, CallbackInfo info) {
        CustomModelClient.isRenderingInventory = true;
        CustomModelClient.inventoryEntityParameter = new EntityParameter(livingEntity);
    }

    @Inject(method = "drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V", at = @At("TAIL"))
    private static void drawEntityTail(int int_1, int int_2, int int_3, float float_1, float float_2, LivingEntity livingEntity, CallbackInfo info) {
        CustomModelClient.isRenderingInventory = false;
    }
}
