package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.network.PacketQuery;
import com.github.gamepiaynmo.custommodel.render.EntityDimensions;
import com.github.gamepiaynmo.custommodel.render.EntityPose;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.world.World;

public abstract class MixinClientPlayerEntity {
    private ModelPack currentPack = null;

    public void updateBoundingBox() {
        if (CustomModelClient.serverConfig != null) {
            ModelPack pack = CustomModelClient.getModelForPlayer(this);
            if (pack != currentPack) {
                currentPack = pack;
                calculateDimensions();
            }
        }
    }

    public float getActiveEyeHeight(EntityPose entityPose, EntityDimensions entityDimensions) {
        if (currentPack != null && CustomModelClient.serverConfig.customEyeHeight) {
            Float eyeHeight = currentPack.getModel().getModelInfo().eyeHeightMap.get(entityPose);
            if (eyeHeight != null) {
                return eyeHeight;
            }
        }

        return super.getActiveEyeHeight(entityPose, entityDimensions);
    }

    public EntityDimensions getDimensions(EntityPose entityPose) {
        if (currentPack != null && CustomModelClient.serverConfig.customBoundingBox) {
            EntityDimensions dimensions = currentPack.getModel().getModelInfo().dimensionsMap.get(entityPose);
            if (dimensions != null)
                return dimensions;
        }

        return super.getDimensions(entityPose);
    }
}
