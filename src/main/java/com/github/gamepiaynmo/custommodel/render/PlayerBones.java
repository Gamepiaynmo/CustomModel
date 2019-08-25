package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.util.Vector3;
import com.google.common.collect.Maps;
import com.sun.javafx.geom.Vec2d;
import net.fabricmc.loader.util.sat4j.core.Vec;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public enum PlayerBones {
    NONE("none", () -> null),
    HEAD("head", () -> CustomModelClient.currentModel.head),
    HEAD_OVERLAY("head_overlay", () -> CustomModelClient.currentModel.headwear),
    BODY("body", () -> CustomModelClient.currentModel.body),
    BODY_OVERLAY("body_overlay", () -> CustomModelClient.currentModel.bodyOverlay),
    LEFT_ARM("left_arm", () -> CustomModelClient.currentModel.leftArm),
    LEFT_ARM_OVERLAY("left_arm_overlay", () -> CustomModelClient.currentModel.leftArmOverlay),
    RIGHT_ARM("right_arm", () -> CustomModelClient.currentModel.rightArm),
    RIGHT_ARM_OVERLAY("right_arm_overlay", () -> CustomModelClient.currentModel.rightArmOverlay),
    LEFT_LEG("left_leg", () -> CustomModelClient.currentModel.leftLeg),
    LEFT_LEG_OVERLAY("left_leg_overlay", () -> CustomModelClient.currentModel.leftLegOverlay),
    RIGHT_LEG("right_leg", () -> CustomModelClient.currentModel.rightLeg),
    RIGHT_LEG_OVERLAY("right_leg_overlay", () -> CustomModelClient.currentModel.rightLegOverlay);

    private final String id;
    private final Supplier<Cuboid> cuboidGetter;
    private static final Map<String, PlayerBones> id2Bone = Maps.newHashMap();
    private final IBone bone;

    private static final Vector3 One = new Vector3(1, 1, 1);
    private static final Vec2d TexSize = new Vec2d(64, 64);

    private PlayerBones(String id, Supplier<Cuboid> cuboidGetter) {
        this.id = id;
        this.cuboidGetter = cuboidGetter;
        bone = id.equals("none") ? new BaseBone() : new OriginalBone(this, cuboidGetter);
    }

    public String getId() {
        return id;
    }

    public Cuboid getCuboid(PlayerEntityModel model) {
        return cuboidGetter.get();
    }

    public IBone getBone() { return bone; }

    public static PlayerBones getById(String id) {
        return id2Bone.get(id);
    }

    public static class BaseBone implements IBone {

        @Override
        public Vector3 getPosition() {
            return Vector3.Zero.cpy();
        }

        @Override
        public Vector3 getRotation() {
            return Vector3.Zero.cpy();
        }

        @Override
        public Vector3 getScale() {
            return One.cpy();
        }

        @Override
        public Vec2d getTextureSize() {
            return TexSize;
        }

        @Override
        public Supplier<Identifier> getTexture() {
            return ModelPack.skinGetter;
        }

        @Override
        public IBone getParent() {
            return null;
        }

        @Override
        public PlayerBones getPlayerBone() {
            return NONE;
        }

        @Override
        public String getId() {
            return "none";
        }
    }

    public static class OriginalBone implements IBone {
        private Supplier<Cuboid> cuboidGetter;
        private PlayerBones playerBone;

        public OriginalBone(PlayerBones playerBone, Supplier<Cuboid> cuboidGetter) {
            this.playerBone = playerBone;
            this.cuboidGetter = cuboidGetter;
        }

        @Override
        public String getId() {
            return playerBone.id;
        }

        @Override
        public Vector3 getPosition() {
            Cuboid cuboid = cuboidGetter.get();
            return new Vector3(cuboid.rotationPointX, cuboid.rotationPointY, cuboid.rotationPointZ);
        }

        @Override
        public Vector3 getRotation() {
            Cuboid cuboid = cuboidGetter.get();
            return new Vector3(cuboid.yaw, cuboid.pitch, cuboid.roll);
        }

        @Override
        public Matrix4 getTransform() {
            return new Matrix4().translate(getPosition().scl(0.0625)).rotate(getQuaternion());
        }

        @Override
        public Vector3 getScale() {
            return One.cpy();
        }

        @Override
        public Vec2d getTextureSize() {
            return TexSize;
        }

        @Override
        public Supplier<Identifier> getTexture() { return ModelPack.skinGetter; }

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
