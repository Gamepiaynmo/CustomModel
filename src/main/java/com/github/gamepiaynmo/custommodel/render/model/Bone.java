package com.github.gamepiaynmo.custommodel.render.model;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.expression.IExpressionBool;
import com.github.gamepiaynmo.custommodel.expression.IExpressionFloat;
import com.github.gamepiaynmo.custommodel.expression.ParseException;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.PlayerBones;
import com.github.gamepiaynmo.custommodel.util.*;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class Bone implements IBone {
    private CustomJsonModel model;

    private String id;
    private IBone parent;

    private Vector3 position = Vector3.Zero.cpy();
    private Vector3 rotation = Vector3.Zero.cpy();
    private Vector3 scale = new Vector3(1, 1, 1);
    private boolean visible;
    private double[] physicsParams;

    private IExpressionFloat[] positionExpr;
    private IExpressionFloat[] rotationExpr;
    private IExpressionFloat[] scaleExpr;
    private IExpressionBool visibleExpr;
    private IExpressionFloat[] physicsExpr;

    private List<Box> boxes = Lists.newArrayList();
    private List<Quad> quads = Lists.newArrayList();
    private List<ParticleEmitter> particles = Lists.newArrayList();

    private boolean physicalize = false;
    public Vector3 velocity = Vector3.Zero.cpy();
    private double length;

    private Supplier<Identifier> texture = null;
    private Vec2d textureSize;

    public static Bone getBoneFromJson(ModelPack pack, CustomJsonModel model, JsonObject jsonObj) throws ParseException {
        Bone bone = new Bone(model);

        bone.id = Json.getString(jsonObj, CustomJsonModel.ID);
        if (bone.id == null)
            throw new TranslatableException("error.custommodel.loadmodelpack.noboneid");

        String parentId = Json.getString(jsonObj, CustomJsonModel.PARENT);
        if (parentId == null)
            bone.parent = PlayerBones.BODY.getBone();
        else bone.parent = model.getBone(parentId);
        if (bone.parent == null)
            throw new TranslatableException("error.custommodel.loadmodelpack.noparentid", parentId);

        String textureId = Json.getString(jsonObj, CustomJsonModel.TEXTURE);
        if (textureId != null) {
            bone.texture = pack.getTexture(textureId);
            if (bone.texture == null)
                throw new TranslatableException("error.custommodel.loadmodelpack.notexture", textureId);

            JsonElement texSizeArray = jsonObj.get(CustomJsonModel.TEXTURE_SIZE);
            if (texSizeArray == null)
                throw new TranslatableException("error.custommodel.loadmodelpack.notexturesize", textureId);
            double[] texSize = Json.parseDoubleArray(texSizeArray, 2);
            bone.textureSize = new Vec2d(texSize[0], texSize[1]);
        }

        bone.positionExpr = Json.parseFloatExpressionArray(jsonObj.get(CustomJsonModel.POSITION), 3, new float[] { 0, 0, 0 }, model.getParser());
        bone.rotationExpr = Json.parseFloatExpressionArray(jsonObj.get(CustomJsonModel.ROTATION), 3, new float[] { 0, 0, 0 }, model.getParser());
        bone.scaleExpr = Json.parseFloatExpressionArray(jsonObj.get(CustomJsonModel.SCALE), 3, new float[] { 1, 1, 1 }, model.getParser());
        bone.visibleExpr = Json.getBooleanExpression(jsonObj, CustomJsonModel.VISIBLE, true, model.getParser());
        JsonElement physical = jsonObj.get(CustomJsonModel.PHYSICS);
        if (physical != null) {
            bone.physicalize = true;
            bone.physicsExpr = Json.parseFloatExpressionArray(physical, 5, new float[] { 0, 0, 0, 0, 0 }, model.getParser());
            bone.physicsParams = new double[bone.physicsExpr.length];
        }

        JsonElement boxArray = jsonObj.get(CustomJsonModel.BOXES);
        if (boxArray != null) {
            for (JsonElement element : boxArray.getAsJsonArray())
                bone.boxes.add(Box.getBoxFromJson(bone, element.getAsJsonObject()));
        }

        JsonElement quadArray = jsonObj.get(CustomJsonModel.QUADS);
        if (quadArray != null) {
            for (JsonElement element : quadArray.getAsJsonArray())
                bone.quads.add(Quad.getQuadFromJson(bone, element.getAsJsonObject()));
        }

        JsonElement partArray = jsonObj.get(CustomJsonModel.PARTICLES);
        if (partArray != null) {
            for (JsonElement element : partArray.getAsJsonArray())
                bone.particles.add(ParticleEmitter.getParticleFromJson(bone, element.getAsJsonObject()));
        }

        return bone;
    }

    private Bone(CustomJsonModel model) {
        this.model = model;
    }

    public CustomJsonModel getModel() { return model; }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Vector3 getPosition() {
        return position.cpy();
    }

    @Override
    public Vector3 getRotation() {
        return rotation.cpy();
    }

    @Override
    public Vector3 getScale() {
        return scale.cpy();
    }

    @Override
    public boolean isVisible() { return visible; }

    @Override
    public Vec2d getTextureSize() {
        if (textureSize == null) {
            return textureSize = parent.getTextureSize();
        }
        return textureSize;
    }

    @Override
    public Supplier<Identifier> getTexture() {
        if (texture == null) {
            return texture = parent.getTexture();
        }
        return texture;
    }

    @Override
    public IBone getParent() {
        return parent;
    }

    @Override
    public PlayerBones getPlayerBone() {
        return null;
    }

    public boolean isPhysicalized() { return physicalize; }
    public double[] getPhysicsParams() { return physicsParams; }
    public double getLength() { return length; }

    public void render() {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBufferBuilder();
        float scaleFactor = CustomModelClient.currentParameter.scale;

        for (Box box : boxes)
            box.render(bufferBuilder, scaleFactor);
        for (Quad quad : quads)
            quad.render(bufferBuilder, scaleFactor);
    }

    public void update() {
        position.set(positionExpr[0].eval(), positionExpr[1].eval(), positionExpr[2].eval()).scl(-1, -1, 1);
        double degToRad = Math.PI / 180;
        rotation.set(rotationExpr[0].eval(), rotationExpr[1].eval(), rotationExpr[2].eval()).scl(degToRad);
        scale.set(scaleExpr[0].eval(), scaleExpr[1].eval(), scaleExpr[2].eval());
        visible = parent.isVisible() && visibleExpr.eval();
        length = position.len() * 0.0625;
        if (physicalize)
            for (int i = 0; i < physicsParams.length; i++)
                physicsParams[i] = physicsExpr[i].eval();
    }

    public void tick(Matrix4 transform) {
        for (ParticleEmitter emitter : particles)
            emitter.tick(transform);
    }

    public void release() {
        for (ParticleEmitter emitter : particles)
            emitter.release();
    }

    public List<Box> getBoxes() { return boxes; }

    public List<Quad> getQuads() { return quads; }

    public List<ParticleEmitter> getParticles() { return particles; }
}
