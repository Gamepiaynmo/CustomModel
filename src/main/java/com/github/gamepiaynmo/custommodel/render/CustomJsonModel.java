package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.expression.ExpressionParser;
import com.github.gamepiaynmo.custommodel.expression.ModelResolver;
import com.github.gamepiaynmo.custommodel.expression.ParseException;
import com.github.gamepiaynmo.custommodel.render.model.Bone;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.github.gamepiaynmo.custommodel.util.Quaternion;
import com.github.gamepiaynmo.custommodel.util.TranslatableException;
import com.github.gamepiaynmo.custommodel.util.Vector3;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
    public static final String VISIBLE = "visible";
    public static final String BOXES = "boxes";
    public static final String QUADS = "quads";
    public static final String PARTICLES = "particles";
    public static final String ANIMATIONS = "animations";
    public static final String TEXTURE_OFFSET = "textureOffset";
    public static final String COORDINATES = "coordinates";
    public static final String SIZE_ADD = "sizeAdd";
    public static final String PHYSICS = "physics";

    public static final String POS_RANGE = "posRange";
    public static final String DIR_RANGE = "dirRange";
    public static final String ANGLE = "angle";
    public static final String SPEED = "speed";
    public static final String ROT_SPEED = "rotSpeed";
    public static final String LIFE_SPAN = "lifeSpan";
    public static final String DENSITY = "density";
    public static final String ANIMATION = "animation";
    public static final String COLOR_R = "colorR";
    public static final String COLOR_G = "colorG";
    public static final String COLOR_B = "colorB";
    public static final String COLOR_A = "colorA";
    public static final String SIZE = "size";
    public static final String GRAVITY = "gravity";
    public static final String COLLIDE = "collide";

    public static CustomJsonModel fromJson(ModelPack pack, JsonObject jsonObj) throws ParseException {
        CustomJsonModel model = new CustomJsonModel();
        model.baseTexture = pack.getBaseTexture();

        JsonElement hideArray = jsonObj.get(HIDE);
        if (hideArray != null) {
            for (JsonElement element : hideArray.getAsJsonArray()) {
                String boneId = element.getAsString();
                PlayerBones bone = PlayerBones.getById(boneId);
                if (bone == null)
                    throw new TranslatableException("error.custommodel.loadmodelpack.nohidebone", boneId);
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

    private List<PlayerBones> hideList = Lists.newArrayList();
    private Map<PlayerBones, Boolean> visibleBones = Maps.newEnumMap(PlayerBones.class);

    private Supplier<Identifier> baseTexture;

    private Map<String, Bone> id2Bone = Maps.newHashMap();
    private List<Bone> bones = Lists.newArrayList();
    private Map<String, Matrix4> boneMats = Maps.newHashMap();
    private Map<String, Matrix4> lastBoneMats = Maps.newHashMap();
    private Map<String, Matrix4> tmpBoneMats = Maps.newHashMap();

    private ExpressionParser parser = new ExpressionParser(new ModelResolver(this));

    private CustomJsonModel() {
        for (PlayerBones bone : PlayerBones.values())
            visibleBones.put(bone, true);
    }

    public Collection<PlayerBones> getHiddenBones() {
        return hideList;
    }

    public IBone getBone(String id) {
        PlayerBones playerBone = PlayerBones.getById(id);
        if (playerBone == null)
            return id2Bone.get(id);
        return playerBone.getBone();
    }

    public ExpressionParser getParser() {
        return parser;
    }

    public boolean isVisible(IBone bone) {
        while (bone.getParent() != null)
            bone = bone.getParent();
        return visibleBones.get(bone.getPlayerBone());
    }

    public boolean isVisible(PlayerBones bone) {
        return visibleBones.get(bone);
    }

    public void setVisible(PlayerBones bone, boolean visible) {
        visibleBones.replace(bone, visible);
    }

    public void setVisible(boolean visible) {
        for (PlayerBones bone : PlayerBones.values())
            visibleBones.replace(bone, visible);
    }

    public void render(Matrix4 baseMat) {
        AbstractClientPlayerEntity entity = CustomModelClient.currentPlayer;
        RenderParameter params = CustomModelClient.currentParameter;
        CustomPlayerEntityRenderer renderer = CustomModelClient.customRenderer;
        PlayerEntityModel model = CustomModelClient.currentModel;

        if (lastBoneMats.isEmpty())
            CustomModelClient.currentRenderer.tick(CustomModelClient.currentPlayer);
        update(baseMat.cpy());

        float partial = params.partial;
        GlStateManager.pushMatrix();
        GL11.glMultMatrixd(baseMat.inv().val);

        for (Bone bone : bones) {
            renderer.bindTexture(bone.getTexture().get());
            GlStateManager.pushMatrix();
            Matrix4 transform = tmpBoneMats.get(bone.getId());

            GL11.glMultMatrixd(transform.val);
            if (bone.isVisible())
                bone.render();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    private void update(Matrix4 baseMat) {
        AbstractClientPlayerEntity entity = CustomModelClient.currentPlayer;
        PlayerEntityModel model = CustomModelClient.currentModel;
        float partial = CustomModelClient.currentParameter.partial;

        if (entity.isInSneakingPose())
            baseMat.translate(0, 0.2f, 0);

        for (PlayerBones playerBone : PlayerBones.values()) {
            IBone bone = playerBone.getBone();
            tmpBoneMats.put(bone.getId(), bone.getTransform().mulLeft(baseMat));
        }

        for (Bone bone : bones) {
            bone.update();
            IBone parent = bone.getParent();
            Matrix4 curTrans;

            if (bone.isPhysicalized()) {
                Matrix4 lastMat = lastBoneMats.get(bone.getId());
                Matrix4 curMat = boneMats.get(bone.getId());
                curTrans = lastMat.cpy().lerp(curMat, partial);
            } else {
                Matrix4 curParTrans = tmpBoneMats.get(parent.getId());
                curTrans = curParTrans.cpy().mul(bone.getTransform());
            }

            tmpBoneMats.put(bone.getId(), curTrans);
        }
    }

    public void tick(Matrix4 baseMat) {
        AbstractClientPlayerEntity entity = CustomModelClient.currentPlayer;
        PlayerEntityModel model = CustomModelClient.currentModel;

        if (entity.isInSneakingPose())
            baseMat.translate(0, 0.2f, 0);

        if (lastBoneMats.isEmpty()) {
            for (PlayerBones playerBone : PlayerBones.values()) {
                IBone bone = playerBone.getBone();
                Matrix4 trans = bone.getTransform().mulLeft(baseMat);
                boneMats.put(bone.getId(), trans);
                lastBoneMats.put(bone.getId(), trans.cpy());
            }

            for (Bone bone : bones) {
                Matrix4 trans = boneMats.get(bone.getParent().getId()).cpy().mul(bone.getTransform());
                boneMats.put(bone.getId(), trans);
                lastBoneMats.put(bone.getId(), trans.cpy());
            }

        } else {
            for (PlayerBones playerBone : PlayerBones.values()) {
                IBone bone = playerBone.getBone();
                lastBoneMats.put(bone.getId(), boneMats.get(bone.getId()));
                boneMats.put(bone.getId(), bone.getTransform().mulLeft(baseMat));
            }

            for (Bone bone : bones) {
                bone.update();
                Matrix4 lastTrans = boneMats.get(bone.getId());
                lastBoneMats.put(bone.getId(), lastTrans);
                IBone parent = bone.getParent();
                Matrix4 lastParTrans = lastBoneMats.get(parent.getId());
                Matrix4 curParTrans = boneMats.get(parent.getId());
                Matrix4 curTrans = curParTrans.cpy().mul(bone.getTransform());

                if (bone.isPhysicalized()) {
                    Vector3 lastStart = lastParTrans.getTranslation(new Vector3());
                    Vector3 curStart = curParTrans.getTranslation(new Vector3());
                    Vector3 lastEnd = lastTrans.getTranslation(new Vector3());
                    Vector3 targetEnd = curTrans.getTranslation(new Vector3());

                    Vector3 curEnd = lastEnd.cpy().add(bone.velocity.cpy());
                    curEnd.sub(curStart).nor().scl(bone.getLength()).add(curStart);

                    bone.velocity.add(targetEnd.cpy().sub(curEnd).scl(bone.getPhysicsParams()[0]));
                    bone.velocity.add(new Vector3(entity.getVelocity()).scl(-1, -1, -1).scl(bone.getPhysicsParams()[3]));
                    bone.velocity.y -= bone.getPhysicsParams()[4];
                    bone.velocity.scl(bone.getPhysicsParams()[2]);

                    Quaternion direction = new Quaternion().setFromCross(lastEnd.cpy().sub(lastStart).nor(), curEnd.cpy().sub(curStart).nor());
                    direction.mul(lastTrans.cpy().getRotation(new Quaternion())).nor();
//                    Quaternion curDir = curTrans.getRotation(new Quaternion());
//                    direction.setEulerAngles(direction.getYaw(), direction.getPitch(), curDir.getRoll());
                    curTrans = new Matrix4(curStart, direction.slerp(curTrans.getRotation(new Quaternion()), bone.getPhysicsParams()[1]), bone.getScale());
                    curTrans.translate(bone.getPosition().scl(0.0625));
                }

                boneMats.put(bone.getId(), curTrans);
                bone.tick(curTrans);
            }
        }
    }

    public void release() {
        for (Bone bone : bones) {
            bone.release();
        }
    }

}
