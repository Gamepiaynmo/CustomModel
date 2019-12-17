package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.expression.ExpressionParser;
import com.github.gamepiaynmo.custommodel.expression.IExpressionFloat;
import com.github.gamepiaynmo.custommodel.expression.ModelResolver;
import com.github.gamepiaynmo.custommodel.expression.ParseException;
import com.github.gamepiaynmo.custommodel.render.model.Bone;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.github.gamepiaynmo.custommodel.server.ModelInfo;
import com.github.gamepiaynmo.custommodel.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Supplier;

public class CustomJsonModel {
    public static final String MODEL_ID = "modelId";
    public static final String MODEL_NAME = "modelName";
    public static final String AUTHOR = "author";
    public static final String VERSION = "version";

    public static final String HIDE = "hide";
    public static final String SKELETON = "skeleton";
    public static final String EYE_HEIGHT = "eyeHeight";
    public static final String BOUNDING_BOX = "boundingBox";
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
    public static final String ITEMS = "items";
    public static final String TEXTURE_OFFSET = "textureOffset";
    public static final String COORDINATES = "coordinates";
    public static final String SIZE_ADD = "sizeAdd";
    public static final String PHYSICS = "physics";
    public static final String ATTACHED = "attached";
    public static final String FP_LEFT = "fpLeft";
    public static final String FP_RIGHT = "fpRight";

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
    public static final String ITEM_ID = "itemId";
    public static final String ENCHANTED = "enchanted";

    public static final Map<String, EntityPose> poseMap = Maps.newHashMap();

    public static CustomJsonModel fromJson(ModelPack pack, JsonObject jsonObj) throws ParseException {
        CustomJsonModel model = new CustomJsonModel(pack);
        model.baseTexture = pack.getBaseTexture();

        model.modelInfo = ModelInfo.fromJson(jsonObj);

        JsonElement hideArray = jsonObj.get(HIDE);
        if (hideArray != null) {
            for (JsonElement element : hideArray.getAsJsonArray()) {
                String id = element.getAsString();
                Collection<PlayerBone> bones = PlayerBone.getListById(id);
                if (bones == null) {
                    Collection<PlayerFeature> features = PlayerFeature.getListById(id);
                    if (features == null)
                        throw new TranslatableException("error.custommodel.loadmodelpack.nohidebone", id);
                    for (PlayerFeature feature : features)
                        model.featureHideList.add(feature);
                } else {
                    for (PlayerBone bone : bones)
                        model.boneHideList.add(bone);
                }
            }
        }

        JsonElement skeletonObj = jsonObj.get(SKELETON);
        if (skeletonObj != null) {
            for (Map.Entry<String, JsonElement> entry : skeletonObj.getAsJsonObject().entrySet()) {
                Collection<PlayerBone> bones = PlayerBone.getListById(entry.getKey());
                IExpressionFloat[] vector = Json.parseFloatExpressionArray(entry.getValue(), 3, new float[]{0, 0, 0}, model.getParser());
                if (bones == null)
                    throw new TranslatableException("error.custommodel.loadmodelpack.nohidebone", entry.getKey());
                for (PlayerBone bone : bones)
                    model.skeleton.put(bone, vector);
            }
        }

        for (PlayerBone bone : PlayerBone.values())
            model.children.put(bone.getId(), Lists.newArrayList());

        JsonElement boneArray = jsonObj.get(BONES);
        if (boneArray != null) {
            for (JsonElement element : boneArray.getAsJsonArray()) {
                JsonObject boneObj = element.getAsJsonObject();
                Bone bone = Bone.getBoneFromJson(pack, model, boneObj);
                model.id2Bone.put(bone.getId(), bone);
                model.bones.add(bone);

                model.children.put(bone.getId(), Lists.newArrayList());
                model.children.get(bone.getParent().getId()).add(bone);
            }
        }

        JsonElement leftArray = jsonObj.get(FP_LEFT);
        if (leftArray != null) {
            model.firstPersonList.put(Arm.LEFT, Lists.newArrayList());
            for (JsonElement element : leftArray.getAsJsonArray()) {
                Bone bone = model.id2Bone.get(element.getAsString());
                if (bone != null)
                    model.firstPersonList.get(Arm.LEFT).add(bone);
            }
        }

        JsonElement rightArray = jsonObj.get(FP_RIGHT);
        if (rightArray != null) {
            model.firstPersonList.put(Arm.RIGHT, Lists.newArrayList());
            for (JsonElement element : rightArray.getAsJsonArray()) {
                Bone bone = model.id2Bone.get(element.getAsString());
                if (bone != null)
                    model.firstPersonList.get(Arm.RIGHT).add(bone);
            }
        }

        for (PlayerFeature feature : PlayerFeature.values())
            model.features.put(feature, model.isHidden(feature) ? Lists.newArrayList()
                    : Lists.newArrayList(feature.getAttachedBone().getBone()));
        for (Bone bone : model.bones)
            for (PlayerFeature feature : bone.attachments)
                model.features.get(feature).add(bone);

        return model;
    }

    public final ModelPack pack;

    private ModelInfo modelInfo;
    private List<PlayerBone> boneHideList = Lists.newArrayList();
    private List<PlayerFeature> featureHideList = Lists.newArrayList();
    private Map<PlayerBone, Boolean> visibleBones = Maps.newEnumMap(PlayerBone.class);
    private Map<PlayerBone, IExpressionFloat[]> skeleton = Maps.newEnumMap(PlayerBone.class);
    private Map<Arm, List<Bone>> firstPersonList = Maps.newEnumMap(Arm.class);

    private Supplier<Identifier> baseTexture;

    private Map<String, Bone> id2Bone = Maps.newHashMap();
    private List<Bone> bones = Lists.newArrayList();
    private Map<PlayerFeature, List<IBone>> features = Maps.newEnumMap(PlayerFeature.class);
    private Map<String, Matrix4> boneMats = Maps.newHashMap();
    private Map<String, Matrix4> lastBoneMats = Maps.newHashMap();
    private Map<String, Matrix4> tmpBoneMats = Maps.newHashMap();

    private Map<String, List<Bone>> children = Maps.newHashMap();

    private final ExpressionParser parser;

    private CustomJsonModel(ModelPack pack) {
        for (PlayerBone bone : PlayerBone.values())
            visibleBones.put(bone, true);
        this.pack = pack;
        parser = new ExpressionParser(new ModelResolver(pack, this));
    }

    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    public Collection<PlayerBone> getHiddenBones() {
        return boneHideList;
    }
    public Collection<PlayerFeature> getHiddenFeatures() { return featureHideList; }

    public boolean isHidden(PlayerBone bone) { return boneHideList.contains(bone); }
    public boolean isHidden(PlayerFeature feature) { return featureHideList.contains(feature); }

    public Collection<Bone> getFirstPersonList(Arm arm) {
        return firstPersonList.get(arm);
    }

    public Collection<Bone> getBones() { return bones; }

    public Collection<IBone> getFeatureAttached(PlayerFeature feature) { return features.get(feature); }

    public IBone getBone(String id) {
        PlayerBone playerBone = PlayerBone.getById(id);
        if (playerBone == null)
            return id2Bone.get(id);
        return playerBone.getBone();
    }

    public ExpressionParser getParser() {
        return parser;
    }

    public boolean isVisible(PlayerBone bone) {
        return visibleBones.get(bone);
    }

    public void setVisible(PlayerBone bone, boolean visible) {
        visibleBones.replace(bone, visible);
    }

    public void setVisible(boolean visible) {
        for (PlayerBone bone : PlayerBone.values())
            visibleBones.replace(bone, visible);
    }

    public void clearTransform() {
        tmpBoneMats = null;
    }

    public Matrix4 getTransform(IBone bone) {
        return tmpBoneMats.get(bone.getId());
    }

    public void updateSkeleton() {
        for (PlayerBone bone : PlayerBone.values()) {
            if (bone != PlayerBone.NONE) {
                IExpressionFloat[] vec = skeleton.get(bone);
                if (vec != null) {
                    bone.getCuboid(CustomModelClient.currentModel).setRotationPoint(
                            (float) -vec[0].eval(), (float) -vec[1].eval(), (float) vec[2].eval());
                }
            }
        }
    }

    public void render(Matrix4 baseMat) {
        AbstractClientPlayerEntity entity = CustomModelClient.currentPlayer;
        RenderParameter params = CustomModelClient.currentParameter;
        PlayerEntityModel model = CustomModelClient.currentModel;

        update(baseMat);

        float partial = params.partial;
        GlStateManager.pushMatrix();
        GL11.glMultMatrixd(baseMat.cpy().inv().val);

        for (Bone bone : bones) {
            CustomModelClient.textureManager.bindTexture(bone.getTexture().get());
            GlStateManager.pushMatrix();
            Matrix4 transform = tmpBoneMats.get(bone.getId());

            GL11.glMultMatrixd(transform.val);
            if (bone.isVisible())
                bone.render();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    public void renderArm(Matrix4 baseMat, Arm arm) {
        AbstractClientPlayerEntity entity = CustomModelClient.currentPlayer;
        RenderParameter params = CustomModelClient.currentParameter;
        PlayerEntityModel model = CustomModelClient.currentModel;

        update(baseMat);

        float partial = params.partial;
        GlStateManager.pushMatrix();
        GL11.glMultMatrixd(baseMat.cpy().inv().val);

        for (Bone bone : firstPersonList.get(arm)) {
            CustomModelClient.textureManager.bindTexture(bone.getTexture().get());
            GlStateManager.pushMatrix();
            Matrix4 transform = tmpBoneMats.get(bone.getId());

            GL11.glMultMatrixd(transform.val);
            if (bone.isVisible())
                bone.render();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    public void update(Matrix4 baseMat) {
        AbstractClientPlayerEntity entity = CustomModelClient.currentPlayer;
        PlayerEntityModel model = CustomModelClient.currentModel;
        float partial = CustomModelClient.currentParameter.partial;

        if (lastBoneMats.isEmpty())
            ((ICustomPlayerRenderer) CustomModelClient.currentRenderer).tick(CustomModelClient.currentPlayer);

        if (entity.isInSneakingPose())
            baseMat = baseMat.cpy().translate(0, 0.2f, 0);

        Map<String, Matrix4> curBoneMats = Maps.newHashMap();
        for (PlayerBone playerBone : PlayerBone.values()) {
            IBone bone = playerBone.getBone();
            curBoneMats.put(bone.getId(), bone.getTransform().mulLeft(baseMat));
        }

        for (Bone bone : bones) {
            bone.update();
            IBone parent = bone.getParent();
            Matrix4 curTrans;

            if (bone.isPhysicalized()) {
                Matrix4 lastMat = lastBoneMats.get(bone.getId());
                Matrix4 curMat = boneMats.get(bone.getId());
                curTrans = lastMat.cpy().lerp(curMat, partial);
                if (tmpBoneMats != null) {
                    String pid = parent.getId();
                    curTrans.mulLeft(tmpBoneMats.get(pid).cpy().inv());
                    curTrans.mulLeft(curBoneMats.get(pid));
                }
            } else {
                Matrix4 curParTrans = curBoneMats.get(parent.getId());
                curTrans = curParTrans.cpy().mul(bone.getTransform());
            }

            curBoneMats.put(bone.getId(), curTrans);
        }

        tmpBoneMats = curBoneMats;
    }

    public void tick(Matrix4 baseMat) {
        AbstractClientPlayerEntity entity = CustomModelClient.currentPlayer;
        PlayerEntityModel model = CustomModelClient.currentModel;

        if (entity.isInSneakingPose())
            baseMat.translate(0, 0.2f, 0);

        if (lastBoneMats.isEmpty()) {
            for (PlayerBone playerBone : PlayerBone.values()) {
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
            for (PlayerBone playerBone : PlayerBone.values()) {
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

    static {
        poseMap.put("standing", EntityPose.STANDING);
        poseMap.put("fall_flying", EntityPose.FALL_FLYING);
        poseMap.put("sleeping", EntityPose.SLEEPING);
        poseMap.put("swimming", EntityPose.SWIMMING);
        poseMap.put("spin_attack", EntityPose.SPIN_ATTACK);
        poseMap.put("sneaking", EntityPose.SNEAKING);
        poseMap.put("dying", EntityPose.DYING);
    }

}
