package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.github.gamepiaynmo.custommodel.util.ModelPack;
import com.github.gamepiaynmo.custommodel.util.Vector3;
import com.google.common.collect.Maps;
import com.sun.javafx.geom.Vec2d;
import net.fabricmc.loader.util.sat4j.core.Vec;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.math.Vec2f;

import java.util.Map;

public enum PlayerBones {
    HEAD("head", model -> model.head),
    HEAD_OVERLAY("head_overlay", model -> model.headwear),
    BODY("body", model->model.body),
    BODY_OVERLAY("body_overlay", model -> model.bodyOverlay),
    LEFT_ARM("left_arm", model -> model.leftArm),
    LEFT_ARM_OVERLAY("left_arm_overlay", model -> model.leftArmOverlay),
    RIGHT_ARM("right_arm", model -> model.rightArm),
    RIGHT_ARM_OVERLAY("right_arm_overlay", model -> model.rightArmOverlay),
    LEFT_LEG("left_leg", model -> model.leftLeg),
    LEFT_LEG_OVERLAY("left_leg_overlay", model -> model.leftLegOverlay),
    RIGHT_LEG("right_leg", model -> model.rightLeg),
    RIGHT_LEG_OVERLAY("right_leg_overlay", model -> model.rightLegOverlay);

    private final String id;
    private final PlayerCuboidGetter cuboidGetter;
    private static final Map<String, PlayerBones> id2Bone = Maps.newHashMap();

    private PlayerBones(String id, PlayerCuboidGetter cuboidGetter) {
        this.id = id;
        this.cuboidGetter = cuboidGetter;
    }

    public String getId() {
        return id;
    }

    public Cuboid getCuboid(PlayerEntityModel model) {
        return cuboidGetter.getCuboid(model);
    }

    public IBone getBone() {
        return new OriginalBone(this, cuboidGetter);
    }

    public static PlayerBones getById(String id) {
        return id2Bone.get(id);
    }

    public static interface PlayerCuboidGetter {
        public Cuboid getCuboid(PlayerEntityModel model);
    }

    public static class OriginalBone implements IBone {
        private PlayerCuboidGetter cuboidGetter;
        private PlayerBones playerBone;

        private static final Vector3 One = new Vector3(1, 1, 1);
        private static final Vec2d TexSize = new Vec2d(64, 64);

        public OriginalBone(PlayerBones playerBone, PlayerCuboidGetter cuboidGetter) {
            this.playerBone = playerBone;
            this.cuboidGetter = cuboidGetter;
        }

        @Override
        public String getId() {
            return playerBone.id;
        }

        @Override
        public Vector3 getPosition(PlayerEntityModel model) {
            Cuboid cuboid = cuboidGetter.getCuboid(model);
            return new Vector3(cuboid.rotationPointX, cuboid.rotationPointY, cuboid.rotationPointZ);
        }

        @Override
        public Vector3 getRotation(PlayerEntityModel model) {
            Cuboid cuboid = cuboidGetter.getCuboid(model);
            return new Vector3(cuboid.yaw, cuboid.pitch, cuboid.roll);
        }

        @Override
        public Matrix4 getTransform(PlayerEntityModel model) {
            return new Matrix4().translate(getPosition(model).scl(0.0625)).rotate(getQuaternion(model));
        }

        @Override
        public Vector3 getScale(PlayerEntityModel model) {
            return One.cpy();
        }

        @Override
        public Vec2d getTextureSize() {
            return TexSize;
        }

        @Override
        public ModelPack.TextureGetter getTexture() { return ModelPack.skinGetter; }

        @Override
        public IBone getParent() { return null; }

        @Override
        public PlayerBones getPlayerBone() { return playerBone; }
    }

    static {
        for (PlayerBones bone : PlayerBones.values())
            id2Bone.put(bone.getId(), bone);
    }
}
