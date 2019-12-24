package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.render.EntityDimensions;
import com.github.gamepiaynmo.custommodel.render.EntityPose;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.server.ModelInfo;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.mojang.authlib.GameProfile;

public abstract class MixinServerPlayerEntity {
    private ModelInfo currentModel = null;

    public void updateBoundingBox() {
        ModelInfo bb = CustomModel.getBoundingBoxForPlayer(this);
        if (bb != currentModel) {
            currentModel = bb;
            calculateDimensions();
        }
    }

    public float getActiveEyeHeight(EntityPose entityPose, EntityDimensions entityDimensions) {
        if (currentModel != null && ModConfig.isCustomEyeHeight()) {
            Float eyeHeight = currentModel.eyeHeightMap.get(entityPose);
            if (eyeHeight != null) {
                return eyeHeight;
            }
        }

        return super.getActiveEyeHeight(entityPose, entityDimensions);
    }

    public EntityDimensions getDimensions(EntityPose entityPose) {
        if (currentModel != null && ModConfig.isCustomBoundingBox()) {
            EntityDimensions dimensions = currentModel.dimensionsMap.get(entityPose);
            if (dimensions != null)
                return dimensions;
        }

        return super.getDimensions(entityPose);
    }
}
