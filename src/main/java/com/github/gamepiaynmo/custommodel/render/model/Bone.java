package com.github.gamepiaynmo.custommodel.render.model;

import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.PlayerBones;
import com.github.gamepiaynmo.custommodel.util.*;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.Vec3d;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;

import java.util.List;

public class Bone implements IBone {
    private static float DegToRad = (float) Math.PI / 180;
    private CustomJsonModel model;

    private String id;
    private IBone parent;

    public Vector3 position = Vector3.Zero.cpy();
    public Vector3 rotation = Vector3.Zero.cpy();
    public Vector3 scale = new Vector3(1, 1, 1);

    private List<Box> boxes = Lists.newArrayList();
    private List<Quad> quads = Lists.newArrayList();
    private List<Particle> particles = Lists.newArrayList();

    private boolean physicalize = false;
    private float[] physicsParams;
    public Vector3 velocity = Vector3.Zero.cpy();
    private float length;
    public Quaternion direction = new Quaternion(), lastDirection = new Quaternion();

    private ModelPack.TextureGetter texture = null;
    private Vec2f textureSize;

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
            float[] texSize = Json.parseFloatArray(texSizeArray, 2);
            bone.textureSize = new Vec2f(texSize[0], texSize[1]);
        }

        JsonElement positionArray = jsonObj.get(CustomJsonModel.POSITION);
        if (positionArray != null)
            bone.position = new Vector3(Json.parseFloatArray(positionArray, 3)).scl(-1, -1, 1);
        bone.length = bone.position.len() * 0.0625f;

        JsonElement rotationArray = jsonObj.get(CustomJsonModel.ROTATION);
        if (rotationArray != null)
            bone.rotation = new Vector3(Json.parseFloatArray(rotationArray, 3)).scl(DegToRad);

        JsonElement scaleArray = jsonObj.get(CustomJsonModel.SCALE);
        if (scaleArray != null)
            bone.scale = new Vector3(Json.parseFloatArray(scaleArray, 3));

        JsonElement boxArray = jsonObj.get(CustomJsonModel.BOXES);
        if (boxArray != null) {
            for (JsonElement element : boxArray.getAsJsonArray())
                bone.boxes.add(getBoxFromJson(bone, element.getAsJsonObject()));
        }

        JsonElement quadArray = jsonObj.get(CustomJsonModel.QUADS);
        if (quadArray != null) {
            for (JsonElement element : quadArray.getAsJsonArray())
                bone.quads.add(getQuadFromJson(bone, element.getAsJsonObject()));
        }

        JsonElement physical = jsonObj.get(CustomJsonModel.PHYSICS);
        if (physical != null) {
            bone.physicalize = true;
            bone.physicsParams = Json.parseFloatArray(physical, 3);
        }

        return bone;
    }

    public static Box getBoxFromJson(Bone bone, JsonObject jsonObj) {
        int uMin = 0, vMin = 0;
        float xMin = 0, yMin = 0, zMin = 0, width = 0, height = 0, depth = 0, size = 0;

        JsonElement uvArray = jsonObj.get(CustomJsonModel.TEXTURE_OFFSET);
        if (uvArray != null) {
            int[] arr = Json.parseIntArray(uvArray, 2);
            uMin = arr[0];
            vMin = arr[1];
        }

        JsonElement coordArray = jsonObj.get(CustomJsonModel.COORDINATES);
        if (coordArray != null) {
            float[] arr = Json.parseFloatArray(coordArray, 6);
            xMin = -arr[0];
            yMin = -arr[1];
            zMin = arr[2];
            width = arr[3];
            height = arr[4];
            depth = arr[5];
        }

        JsonElement sizeVal = jsonObj.get(CustomJsonModel.SIZE_ADD);
        if (sizeVal != null)
            size = sizeVal.getAsFloat();

        return new Box(uMin, vMin, xMin, yMin, zMin, width, height, depth, size, bone.getTextureSize().x, bone.getTextureSize().y);
    }

    public static Quad getQuadFromJson(Bone bone, JsonObject jsonObj) {
        int uMin = 0, vMin = 0;
        float xMin = 0, yMin = 0, zMin = 0, width = 0, height = 0, size = 0;

        JsonElement uvArray = jsonObj.get(CustomJsonModel.TEXTURE_OFFSET);
        if (uvArray != null) {
            int[] arr = Json.parseIntArray(uvArray, 2);
            uMin = arr[0];
            vMin = arr[1];
        }

        JsonElement coordArray = jsonObj.get(CustomJsonModel.COORDINATES);
        if (coordArray != null) {
            float[] arr = Json.parseFloatArray(coordArray, 5);
            xMin = -arr[0];
            yMin = -arr[1];
            zMin = arr[2];
            width = arr[3];
            height = arr[4];
        }

        JsonElement sizeVal = jsonObj.get(CustomJsonModel.SIZE_ADD);
        if (sizeVal != null)
            size = sizeVal.getAsFloat();

        return new Quad(xMin, yMin, zMin, width, height, uMin, vMin, size, bone.getTextureSize().x, bone.getTextureSize().y);
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
    public Vec2f getTextureSize() {
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
    public float[] getPhysicsParams() { return physicsParams; }
    public float getLength() { return length; }

    public Matrix4 getTransform(PlayerEntityModel model, float partial) {
        return getSelfTransform(model, partial).translate(getPosition(model).scl(0.0625f));
    }

    public Matrix4 getSelfTransform(PlayerEntityModel model, float partial) {
        Matrix4 res = new Matrix4().setToScaling(getScale(model));
        if (physicalize)
            res.rotate(lastDirection.cpy().slerp(direction, partial));
        return res.rotate(getQuaternion(model));
    }

    public void render(AbstractClientPlayerEntity playerEntity, PlayerEntityModel playerModel, float scale, float partial) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBufferBuilder();
        for (Box box : boxes)
            box.render(bufferBuilder, scale);
        for (Quad quad : quads)
            quad.render(bufferBuilder, scale);
    }
}
