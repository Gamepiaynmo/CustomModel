package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.render.EntityParameter;
import net.minecraft.entity.EntityLivingBase;

public class DrawEntityInventoryHandler {

    public static void preDrawEntityInventory(EntityLivingBase entity) {
        CustomModelClient.isRenderingInventory = true;
        CustomModelClient.inventoryEntityParameter = new EntityParameter(entity);
    }

    public static void postDrawEntityInventory() {
        CustomModelClient.isRenderingInventory = false;
    }
}
