package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.expression.*;
import com.github.gamepiaynmo.custommodel.mixin.RenderPlayerHandler;
import com.github.gamepiaynmo.custommodel.render.model.Bone;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.github.gamepiaynmo.custommodel.server.ModelInfo;
import com.github.gamepiaynmo.custommodel.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

public class CustomJsonModel {
    public static final String MODEL_ID = "modelId";
    public static final String MODEL_NAME = "modelName";
    public static final String AUTHOR = "author";
    public static final String VERSION = "version";

    public static final String HIDE = "hide";
    public static final String VARIABLES = "variables";
    public static final String TICK_VARS = "tickVars";
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
    public static final String COLOR = "color";
    public static final String ALPHA = "alpha";
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

    public static CustomJsonModel fromJson(ModelPack pack, JsonObject jsonObj, RenderContext context) throws ParseException {
        CustomJsonModel model = new CustomJsonModel(pack);
        model.baseTexture = pack.getBaseTexture();
        model.modelInfo = ModelInfo.fromJson(jsonObj);

        Json.parseJsonArray(jsonObj.get(HIDE), element -> {
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
        });

        Json.parseJsonObject(jsonObj.get(TICK_VARS), (name, element) -> {
            String typeStr = element.getAsJsonArray().get(0).getAsString();
            ExpressionType type = typeStr.equals("float") ? ExpressionType.FLOAT : ExpressionType.BOOL;
            TickVariable variable = new TickVariable(type);
            model.tickVars.add(Pair.of(name, variable));
            model.tickVarMap.put(name, variable);
        });
        Json.parseJsonObject(jsonObj.get(TICK_VARS), (name, element) -> {
            TickVariable variable = model.tickVarMap.get(name);
            JsonArray array = element.getAsJsonArray();
            variable.setInitValue(array.get(1));
            variable.setExpression(Json.getExpression(array.get(2), 0, model.getParser()));
        });

        Json.parseJsonObject(jsonObj.get(VARIABLES), (name, element) -> {
            model.variables.put(name, model.getParser().parse(element.getAsString()));
        });

        Json.parseJsonObject(jsonObj.get(SKELETON), (key, value) -> {
            Collection<PlayerBone> bones = PlayerBone.getListById(key);
            IExpressionFloat[] vector = Json.parseFloatExpressionArray(value, 3, new float[]{0, 0, 0}, model.getParser());
            if (bones == null)
                throw new TranslatableException("error.custommodel.loadmodelpack.nohidebone", key);
            for (PlayerBone bone : bones)
                model.skeleton.put(bone, vector);
        });

        for (PlayerBone bone : PlayerBone.values())
            model.children.put(bone.getId(), Lists.newArrayList());

        context.currentJsonModel = model;
        Json.parseJsonArray(jsonObj.get(BONES), element -> {
            JsonObject boneObj = element.getAsJsonObject();
            Bone bone = Bone.getBoneFromJson(pack, model, boneObj, context);
            model.id2Bone.put(bone.getId(), bone);
            model.bones.add(bone);

            model.children.put(bone.getId(), Lists.newArrayList());
            model.children.get(bone.getParent().getId()).add(bone);
        });

        JsonElement leftArray = jsonObj.get(FP_LEFT);
        if (leftArray != null) {
            model.firstPersonList.put(EnumHandSide.LEFT, Lists.newArrayList());
            for (JsonElement element : leftArray.getAsJsonArray()) {
                Bone bone = model.id2Bone.get(element.getAsString());
                if (bone != null)
                    model.firstPersonList.get(EnumHandSide.LEFT).add(bone);
            }
        }

        JsonElement rightArray = jsonObj.get(FP_RIGHT);
        if (rightArray != null) {
            model.firstPersonList.put(EnumHandSide.RIGHT, Lists.newArrayList());
            for (JsonElement element : rightArray.getAsJsonArray()) {
                Bone bone = model.id2Bone.get(element.getAsString());
                if (bone != null)
                    model.firstPersonList.get(EnumHandSide.RIGHT).add(bone);
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
    private Map<EnumHandSide, List<Bone>> firstPersonList = Maps.newEnumMap(EnumHandSide.class);

    private Function<RenderContext, ResourceLocation> baseTexture;
    private Map<String, IExpression> variables = Maps.newHashMap();
    private List<Pair<String, TickVariable>> tickVars = Lists.newArrayList();
    private Map<String, TickVariable> tickVarMap = Maps.newHashMap();

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

    public IExpression getVariable(String name) { return variables.get(name); }
    public TickVariable getTickVar(String name) { return tickVarMap.get(name); }

    public Collection<PlayerBone> getHiddenBones() {
        return boneHideList;
    }
    public Collection<PlayerFeature> getHiddenFeatures() { return featureHideList; }

    public boolean isHidden(PlayerBone bone) { return boneHideList.contains(bone); }
    public boolean isHidden(PlayerFeature feature) { return featureHideList.contains(feature); }

    public Collection<Bone> getFirstPersonList(EnumHandSide arm) {
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

    public void updateSkeleton(RenderContext context) {
        for (PlayerBone bone : PlayerBone.values()) {
            if (bone != PlayerBone.NONE) {
                IExpressionFloat[] vec = skeleton.get(bone);
                if (vec != null) {
                    bone.getCuboid(context.currentModel).setRotationPoint(
                            (float) -vec[0].eval(context), (float) -vec[1].eval(context), (float) vec[2].eval(context));
                }
            }
        }
    }

    public void render(Matrix4 baseMat, RenderContext context) {
        RenderParameter params = context.currentParameter;
        ModelPlayer model = context.currentModel;

        update(baseMat, context);

        float partial = params.partial;
        GlStateManager.pushMatrix();
        GL11.glMultMatrix(baseMat.cpy().inv().toBuffer());

        TextureManager textureManager = Minecraft.getMinecraft().renderEngine;

        for (Bone bone : bones) {
            if (bone.isVisible(context)) {
                textureManager.bindTexture(bone.getTexture(context).apply(context));
                GlStateManager.pushMatrix();
                Matrix4 transform = tmpBoneMats.get(bone.getId());

                GL11.glMultMatrix(transform.toBuffer());
                bone.render(context);
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.popMatrix();
    }

    public void renderArm(Matrix4 baseMat, EnumHandSide arm, RenderContext context) {
        RenderParameter params = context.currentParameter;
        ModelPlayer model = context.currentModel;

        update(baseMat, context);

        float partial = params.partial;
        GlStateManager.pushMatrix();
        GL11.glMultMatrix(baseMat.cpy().inv().toBuffer());
        TextureManager textureManager = Minecraft.getMinecraft().renderEngine;

        for (Bone bone : firstPersonList.get(arm)) {
            if (bone.isVisible(context)) {
                textureManager.bindTexture(bone.getTexture(context).apply(context));
                GlStateManager.pushMatrix();
                Matrix4 transform = tmpBoneMats.get(bone.getId());

                GL11.glMultMatrix(transform.toBuffer());
                bone.render(context);
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.popMatrix();
    }

    public void update(Matrix4 baseMat, RenderContext context) {
        EntityLivingBase entity = context.currentEntity;
        ModelPlayer model = context.currentModel;
        float partial = context.currentParameter.partial;

        if (lastBoneMats.isEmpty())
            RenderPlayerHandler.tick(entity);

        if (entity.isSneaking() && !CustomModelClient.isRenderingFirstPerson)
            baseMat = baseMat.cpy().translate(0, 0.2f, 0);

        Map<String, Matrix4> curBoneMats = Maps.newHashMap();
        for (PlayerBone playerBone : PlayerBone.values()) {
            IBone bone = playerBone.getBone();
            curBoneMats.put(bone.getId(), bone.getTransform(context).mulLeft(baseMat));
        }

        for (Bone bone : bones) {
            bone.update(context);
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
                curTrans = curParTrans.cpy().mul(bone.getTransform(context));
            }

            curBoneMats.put(bone.getId(), curTrans);
        }

        tmpBoneMats = curBoneMats;
    }

    public void tick(Matrix4 baseMat, RenderContext context) {
        EntityLivingBase entity = context.currentEntity;
        ModelPlayer model = context.currentModel;

        for (Pair<String, TickVariable> pair : tickVars) {
            pair.second().tick(context);
        }

        if (entity.isSneaking())
            baseMat.translate(0, 0.2f, 0);

        if (lastBoneMats.isEmpty()) {
            for (PlayerBone playerBone : PlayerBone.values()) {
                IBone bone = playerBone.getBone();
                Matrix4 trans = bone.getTransform(context).mulLeft(baseMat);
                boneMats.put(bone.getId(), trans);
                lastBoneMats.put(bone.getId(), trans.cpy());
            }

            for (Bone bone : bones) {
                Matrix4 trans = boneMats.get(bone.getParent().getId()).cpy().mul(bone.getTransform(context));
                boneMats.put(bone.getId(), trans);
                lastBoneMats.put(bone.getId(), trans.cpy());
            }

        } else {
            for (PlayerBone playerBone : PlayerBone.values()) {
                IBone bone = playerBone.getBone();
                lastBoneMats.put(bone.getId(), boneMats.get(bone.getId()));
                boneMats.put(bone.getId(), bone.getTransform(context).mulLeft(baseMat));
            }

            for (Bone bone : bones) {
                bone.update(context);
                Matrix4 lastTrans = boneMats.get(bone.getId());
                lastBoneMats.put(bone.getId(), lastTrans);
                IBone parent = bone.getParent();
                Matrix4 lastParTrans = lastBoneMats.get(parent.getId());
                Matrix4 curParTrans = boneMats.get(parent.getId());
                Matrix4 curTrans = curParTrans.cpy().mul(bone.getTransform(context));

                if (bone.isPhysicalized()) {
                    Vector3 lastStart = lastParTrans.getTranslation(new Vector3());
                    Vector3 curStart = curParTrans.getTranslation(new Vector3());
                    Vector3 lastEnd = lastTrans.getTranslation(new Vector3());
                    Vector3 targetEnd = curTrans.getTranslation(new Vector3());

                    Vector3 curEnd = lastEnd.cpy().add(bone.velocity.cpy());
                    curEnd.sub(curStart).nor().scl(bone.getLength()).add(curStart);

                    bone.velocity.add(targetEnd.cpy().sub(curEnd).scl(bone.getPhysicsParams()[0]));
                    bone.velocity.add(new Vector3(entity.motionX, entity.motionY, entity.motionZ).scl(-1, -1, -1).scl(bone.getPhysicsParams()[3]));
                    bone.velocity.y -= bone.getPhysicsParams()[4];
                    bone.velocity.scl(bone.getPhysicsParams()[2]);

                    Quaternion direction = new Quaternion().setFromCross(lastEnd.cpy().sub(lastStart).nor(), curEnd.cpy().sub(curStart).nor());
                    direction.mul(lastTrans.cpy().getRotation(new Quaternion())).nor();
//                    Quaternion curDir = curTrans.getRotation(new Quaternion());
//                    direction.setEulerAngles(direction.getYaw(), direction.getPitch(), curDir.getRoll());
                    curTrans = new Matrix4(curStart, direction.slerp(curTrans.getRotation(new Quaternion()), bone.getPhysicsParams()[1]), bone.getScale(context));
                    curTrans.translate(bone.getPosition(context).scl(0.0625));
                }

                boneMats.put(bone.getId(), curTrans);
                bone.tick(curTrans, context);
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
