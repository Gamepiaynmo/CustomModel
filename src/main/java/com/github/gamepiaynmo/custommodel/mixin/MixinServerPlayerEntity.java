package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.server.ModelInfo;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {

    private ModelInfo currentModel = null;

    public MixinServerPlayerEntity(World world_1, GameProfile gameProfile_1) {
        super(world_1, gameProfile_1);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    public void updateBoundingBox(CallbackInfo info) {
        ModelInfo bb = CustomModel.getBoundingBoxForPlayer(this);
        if (bb != currentModel) {
            currentModel = bb;
            calculateDimensions();
        }
    }

    @Override
    public float getActiveEyeHeight(EntityPose entityPose, EntityDimensions entityDimensions) {
        if (currentModel != null && ModConfig.isCustomEyeHeight()) {
            Float eyeHeight = currentModel.eyeHeightMap.get(entityPose);
            if (eyeHeight != null) {
                return eyeHeight;
            }
        }

        return super.getActiveEyeHeight(entityPose, entityDimensions);
    }

    @Override
    public EntityDimensions getDimensions(EntityPose entityPose) {
        if (currentModel != null && ModConfig.isCustomBoundingBox()) {
            EntityDimensions dimensions = currentModel.dimensionsMap.get(entityPose);
            if (dimensions != null)
                return dimensions;
        }

        return super.getDimensions(entityPose);
    }
}
