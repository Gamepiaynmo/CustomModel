package com.github.gamepiaynmo.custommodel.render.model;

import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.PlayerBones;
import com.github.gamepiaynmo.custommodel.util.*;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.icu.text.MessagePattern;
import com.sun.javafx.geom.Vec2d;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.math.Vec2f;

import java.util.List;

public class Bone implements IBone {
    private static double DegToRad = (double) Math.PI / 180;
    private CustomJsonModel model;

    private String id;
    private IBone parent;

    public Vector3 position = Vector3.Zero.cpy();
    public Vector3 rotation = Vector3.Zero.cpy();
    public Vector3 scale = new Vector3(1, 1, 1);

    private List<Box> boxes = Lists.newArrayList();
    private List<Quad> quads = Lists.newArrayList();
    private List<ParticleEmitter> particles = Lists.newArrayList();

    private boolean physicalize = false;
    private double[] physicsParams;
    public Vector3 velocity = Vector3.Zero.cpy();
    private double length;

    private ModelPack.TextureGetter texture = null;
    private Vec2d textureSize;

    public static Bone getBoneFromJson(ModelPack pack, CustomJsonModel model, JsonObject jsonObj) {
        Bone bone = new Bone(model);

        bone.id = Json.getString(jsonObj, CustomJsonModel.ID);
        if (bone.id == null)
            throw new RuntimeException("ID is required.");

        String parentId = Json.getString(jsonObj, CustomJsonModel.PARENT);
        if (parentId == null)
            throw new RuntimeException("Parent is required.");
        bone.parent = model.getBone(parentId);
        if (bone.parent == null)
            throw new RuntimeException("Bone " + parentId + " not found.");

        String textureId = Json.getString(jsonObj, CustomJsonModel.TEXTURE);
        if (textureId != null) {
            bone.texture = pack.getTexture(textureId);
            if (bone.texture == null)
                throw new RuntimeException("Texture " + textureId + " not found.");

            JsonElement texSizeArray = jsonObj.get(CustomJsonModel.TEXTURE_SIZE);
            if (texSizeArray == null)
                throw new RuntimeException("Texture size is required.");
            double[] texSize = Json.parseDoubleArray(texSizeArray, 2);
            bone.textureSize = new Vec2d(texSize[0], texSize[1]);
        }

        JsonElement positionArray = jsonObj.get(CustomJsonModel.POSITION);
        if (positionArray != null)
            bone.position = new Vector3(Json.parseDoubleArray(positionArray, 3)).scl(-1, -1, 1);
        bone.length = bone.position.len() * 0.0625;

        JsonElement rotationArray = jsonObj.get(CustomJsonModel.ROTATION);
        if (rotationArray != null)
            bone.rotation = new Vector3(Json.parseDoubleArray(rotationArray, 3)).scl(DegToRad);

        JsonElement scaleArray = jsonObj.get(CustomJsonModel.SCALE);
        if (scaleArray != null)
            bone.scale = new Vector3(Json.parseDoubleArray(scaleArray, 3));

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

        JsonElement physical = jsonObj.get(CustomJsonModel.PHYSICS);
        if (physical != null) {
            bone.physicalize = true;
            bone.physicsParams = Json.parseDoubleArray(physical, 5);
        }

        return bone;
    }

    private Bone(CustomJsonModel model) {
        this.model = model;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Vector3 getPosition(PlayerEntityModel model) {
        return position.cpy();
    }

    @Override
    public Vector3 getRotation(PlayerEntityModel model) {
        return rotation.cpy();
    }

    @Override
    public Vector3 getScale(PlayerEntityModel model) {
        return scale.cpy();
    }

    @Override
    public Vec2d getTextureSize() {
        if (textureSize == null) {
            return textureSize = parent.getTextureSize();
        }
        return textureSize;
    }

    @Override
    public ModelPack.TextureGetter getTexture() {
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

    public void render(AbstractClientPlayerEntity playerEntity, PlayerEntityModel playerModel, float scale, float partial) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBufferBuilder();
        for (Box box : boxes)
            box.render(bufferBuilder, scale);
        for (Quad quad : quads)
            quad.render(bufferBuilder, scale);
    }

    public void tick(AbstractClientPlayerEntity playerEntity, Matrix4 transform) {
        for (ParticleEmitter emitter : particles)
            emitter.tick(playerEntity, transform);
    }
}
