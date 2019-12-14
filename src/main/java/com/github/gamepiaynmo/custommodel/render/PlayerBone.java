package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.github.gamepiaynmo.custommodel.util.Vec2d;
import com.github.gamepiaynmo.custommodel.util.Vector3;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public enum PlayerBone {
    NONE("none", null),
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
    private static final Map<String, PlayerBone> id2Bone = Maps.newHashMap();
    private static final Map<String, Collection<PlayerBone>> boneLists = Maps.newHashMap();
    private final IBone bone;

    private static final Vector3 One = new Vector3(1, 1, 1);
    private static final Vec2d TexSize = new Vec2d(64, 64);

    PlayerBone(String id, Supplier<Cuboid> cuboidGetter) {
        this.id = id;
        this.cuboidGetter = cuboidGetter;
        bone = cuboidGetter == null ? new BlankBone() : new OriginalBone(this, cuboidGetter);
    }

    public String getId() {
        return id;
    }

    public Cuboid getCuboid(PlayerEntityModel model) {
        return cuboidGetter.get();
    }

    public IBone getBone() { return bone; }

    public static PlayerBone getById(String id) {
        return id2Bone.get(id);
    }

    public static Collection<PlayerBone> getListById(String id) {
        PlayerBone bone = getById(id);
        if (bone == null)
            return boneLists.get(id);
        return Lists.newArrayList(bone);
    }

    public static class OriginalBone implements IBone {
        private Supplier<Cuboid> cuboidGetter;
        private PlayerBone playerBone;

        OriginalBone(PlayerBone playerBone, Supplier<Cuboid> cuboidGetter) {
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
        public boolean isVisible() { return CustomModelClient.currentJsonModel.isVisible(playerBone); }

        @Override
        public Vector3 getScale() {
            return One.cpy();
        }

        @Override
        public Vec2d getTextureSize() {
            return TexSize;
        }

        @Override
        public Supplier<Identifier> getTexture() { return ModelPack.defGetter[0]; }

        @Override
        public IBone getParent() { return null; }

        @Override
        public PlayerBone getPlayerBone() { return playerBone; }
    }

    public static class BlankBone implements IBone {

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
            return ModelPack.defGetter[0];
        }

        @Override
        public IBone getParent() {
            return null;
        }

        @Override
        public PlayerBone getPlayerBone() {
            return NONE;
        }

        @Override
        public String getId() {
            return NONE.id;
        }

        @Override
        public boolean isVisible() {
            return true;
        }
    }

    static {
        for (PlayerBone bone : PlayerBone.values())
            id2Bone.put(bone.getId(), bone);
        boneLists.put("head_all", ImmutableList.of(HEAD, HEAD_OVERLAY));
        boneLists.put("body_all", ImmutableList.of(BODY, BODY_OVERLAY));
        boneLists.put("left_arm_all", ImmutableList.of(LEFT_ARM, LEFT_ARM_OVERLAY));
        boneLists.put("right_arm_all", ImmutableList.of(RIGHT_ARM, RIGHT_ARM_OVERLAY));
        boneLists.put("arms_all", ImmutableList.of(LEFT_ARM, LEFT_ARM_OVERLAY, RIGHT_ARM, RIGHT_ARM_OVERLAY));
        boneLists.put("left_leg_all", ImmutableList.of(LEFT_LEG, LEFT_LEG_OVERLAY));
        boneLists.put("right_leg_all", ImmutableList.of(RIGHT_LEG, RIGHT_LEG_OVERLAY));
        boneLists.put("legs_all", ImmutableList.of(LEFT_LEG, LEFT_LEG_OVERLAY, RIGHT_LEG, RIGHT_LEG_OVERLAY));
        boneLists.put("model_all", ImmutableList.of(HEAD, HEAD_OVERLAY, BODY, BODY_OVERLAY, LEFT_ARM, LEFT_ARM_OVERLAY,
                RIGHT_ARM, RIGHT_ARM_OVERLAY, LEFT_LEG, LEFT_LEG_OVERLAY, RIGHT_LEG, RIGHT_LEG_OVERLAY));
    }
}
