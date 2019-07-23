package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.render.model.Bone;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.github.gamepiaynmo.custommodel.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.loader.util.sat4j.core.Vec;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomJsonModel {
    public static final String HIDE = "hide";
    public static final String BONES = "bones";
    public static final String ID = "id";
    public static final String PARENT = "parent";
    public static final String TEXTURE = "texture";
    public static final String TEXTURE_SIZE = "textureSize";
    public static final String POSITION = "position";
    public static final String ROTATION = "rotation";
    public static final String SCALE = "scale";
    public static final String BOXES = "boxes";
    public static final String QUADS = "quads";
    public static final String PARTICLES = "particles";
    public static final String ANIMATIONS = "animations";
    public static final String TEXTURE_OFFSET = "textureOffset";
    public static final String COORDINATES = "coordinates";
    public static final String SIZE_ADD = "sizeAdd";
    public static final String PHYSICS = "physics";

    public static CustomJsonModel fromJson(ModelPack pack, JsonObject jsonObj) {
        CustomJsonModel model = new CustomJsonModel();
        model.baseTexture = pack.getBaseTexture();

        JsonElement hideArray = jsonObj.get(HIDE);
        if (hideArray != null) {
            for (JsonElement element : hideArray.getAsJsonArray()) {
                String boneId = element.getAsString();
                PlayerBones bone = PlayerBones.getById(boneId);
                if (bone == null)
                    throw new RuntimeException("Hide element " + boneId + " not found.");
                model.hideList.add(bone);
            }
        }

        JsonElement boneArray = jsonObj.get(BONES);
        if (boneArray != null) {
            for (JsonElement element : boneArray.getAsJsonArray()) {
                JsonObject boneObj = element.getAsJsonObject();
                Bone bone = Bone.getBoneFromJson(pack, model, boneObj);
                model.id2Bone.put(bone.getId(), bone);
                model.bones.add(bone);
            }
        }

        return model;
    }

    private static FloatBuffer buffer = GlAllocationUtils.allocateFloatBuffer(16);

    private List<PlayerBones> hideList = Lists.newArrayList();
    private Map<PlayerBones, Boolean> visibleBones = Maps.newEnumMap(PlayerBones.class);

    private ModelPack.TextureGetter baseTexture;

    private Map<String, Bone> id2Bone = Maps.newHashMap();
    private List<Bone> bones = Lists.newArrayList();
    private Map<String, Matrix4> boneMats = Maps.newHashMap();
    private Map<String, Matrix4> lastBoneMats = Maps.newHashMap();
    private Map<String, Matrix4> tmpBoneMats = Maps.newHashMap();

    private String modelType = "";

    private CustomJsonModel() {}

    public Collection<PlayerBones> getHiddenBones() {
        return hideList;
    }

    public IBone getBone(String id) {
        PlayerBones playerBone = PlayerBones.getById(id);
        if (playerBone == null)
            return id2Bone.get(id);
        return playerBone.getBone();
    }

    public boolean isVisible(IBone bone) {
        while (bone.getParent() != null)
            bone = bone.getParent();
        return visibleBones.get(bone.getPlayerBone());
    }

    public void setVisible(PlayerBones bone, boolean visible) {
        visibleBones.replace(bone, visible);
    }

    public void setVisible(boolean visible) {
        for (PlayerBones bone : PlayerBones.values())
            visibleBones.replace(bone, visible);
    }

    public void render(AbstractClientPlayerEntity entity, CustomPlayerEntityRenderer renderer, PlayerEntityModel model, float scale, float partial) {
        Matrix4 baseMat = new Matrix4();
        if (entity.isInSneakingPose())
            baseMat.translate(0, 0.2f, 0);

        for (PlayerBones playerBone : PlayerBones.values()) {
            IBone bone = playerBone.getBone();
            tmpBoneMats.put(playerBone.getId(), bone.getTransform(model).mulLeft(baseMat));
        }

        for (Bone bone : bones) {
            renderer.bindTexture(bone.getTexture().getTexture(entity));
            GlStateManager.pushMatrix();
            Matrix4 transform = tmpBoneMats.get(bone.getParent().getId()).cpy();
            transform.mul(bone.getSelfTransform(model, partial));
            buffer.put(transform.val);
            buffer.rewind();
            GlStateManager.multMatrix(buffer);
            bone.render(entity, model, scale, partial);
            GlStateManager.popMatrix();
            tmpBoneMats.put(bone.getId(), tmpBoneMats.get(bone.getParent().getId()).cpy()
                    .mul(bone.getTransform(model, partial)));
        }
    }

    public void tick(AbstractClientPlayerEntity entity, PlayerEntityModel model) {
        Matrix4 baseMat = new Matrix4();
        if (entity.isInSneakingPose())
            baseMat.translate(0, 0.2f, 0);
        baseMat.translate((float) entity.x, (float) entity.y, (float) entity.z);
        baseMat.rotate(Vector3.Y, -entity.field_6283);

        if (lastBoneMats.isEmpty()) {
            for (PlayerBones playerBone : PlayerBones.values()) {
                IBone bone = playerBone.getBone();
                Matrix4 trans = bone.getTransform(model).mulLeft(baseMat);
                boneMats.put(bone.getId(), trans);
                lastBoneMats.put(bone.getId(), trans.cpy());
            }

            for (Bone bone : bones) {
                Matrix4 trans = boneMats.get(bone.getParent().getId()).cpy().mul(bone.getTransform(model));
                boneMats.put(bone.getId(), trans);
                lastBoneMats.put(bone.getId(), trans.cpy());
            }

        } else {
            for (PlayerBones playerBone : PlayerBones.values()) {
                IBone bone = playerBone.getBone();
                lastBoneMats.put(bone.getId(), boneMats.get(bone.getId()));
                boneMats.put(bone.getId(), bone.getTransform(model).mulLeft(baseMat));
            }

            for (Bone bone : bones) {
                Matrix4 lastTrans = boneMats.get(bone.getId());
                lastBoneMats.put(bone.getId(), lastTrans);
                IBone parent = bone.getParent();
                Matrix4 lastParTrans = lastBoneMats.get(parent.getId());
                Matrix4 curParTrans = boneMats.get(parent.getId());
                Matrix4 curTrans = curParTrans.cpy().mul(bone.getTransform(model, 1));

                if (bone.isPhysicalized()) {
                    Vector3 lastStart = lastParTrans.getTranslation(new Vector3());
                    Vector3 curStart = curParTrans.getTranslation(new Vector3());
                    Vector3 lastEnd = lastTrans.getTranslation(new Vector3());
                    Vector3 targetEnd = curTrans.getTranslation(new Vector3());

                    Vector3 force = curStart.cpy().sub(lastStart).scl(bone.getPhysicsParams()[0]);
                    force.add(targetEnd.cpy().sub(curStart).nor().scl(bone.getPhysicsParams()[1]));
                    force.sub(bone.velocity.cpy().scl(bone.getPhysicsParams()[2]));
                    bone.velocity.add(force.scl(1 / 20.0f));

                    Vector3 curEnd = lastEnd.cpy().add(bone.velocity.cpy().scl(1 / 20.0f));
                    curEnd.sub(curStart).nor().scl(bone.getLength()).add(curStart);
                    bone.lastDirection = bone.direction.cpy();
                    //bone.direction = new Quaternion().setFromCross(curEnd.sub(curStart).nor(), lastEnd.sub(lastStart).nor()).mul(bone.direction);
                    bone.direction = new Quaternion().setFromCross(curEnd.sub(curStart).nor(), bone.position.cpy().mul(curParTrans.cpy().mul(bone.getSelfTransform(model, 1))).sub(curStart).nor());
                    curTrans = curParTrans.cpy().mul(bone.getTransform(model, 1));

                    System.out.println(lastStart.toString() + curStart.toString() + lastEnd.toString() + curEnd.toString());
                }

                boneMats.put(bone.getId(), curTrans);
            }
        }
    }

}
