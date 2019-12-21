package com.github.gamepiaynmo.custommodel.render.model;

import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.util.Json;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.Matrix3f;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class Quad {
    public Vertex[] vertices;
    public float xMin;
    public float yMin;
    public float z;
    public float xMax;
    public float yMax;
    public final Vector3f direction;

    protected Quad(Vertex[] vertexs_1, float int_1, float int_2, float int_3, float int_4, float float_1, float float_2, Direction direction) {
        vertexs_1[0] = vertexs_1[0].remap(int_3 / float_1, int_2 / float_2);
        vertexs_1[1] = vertexs_1[1].remap(int_1 / float_1, int_2 / float_2);
        vertexs_1[2] = vertexs_1[2].remap(int_1 / float_1, int_4 / float_2);
        vertexs_1[3] = vertexs_1[3].remap(int_3 / float_1, int_4 / float_2);
        this.vertices = vertexs_1;

        this.direction = direction.getUnitVector();
        this.direction.piecewiseMultiply(-1.0F, 1.0F, 1.0F);
    }

    protected Quad(float xMin, float yMin, float zMin, float width, float height, float uMin, float vMin, float sizeAdd, float texWidth, float texHeight) {
        vertices = new Vertex[4];
        vertices[0] = new Vertex(xMin + sizeAdd, yMin - height - sizeAdd, zMin, (uMin + width) / texWidth, vMin / texHeight);
        vertices[1] = new Vertex(xMin - width - sizeAdd, yMin - height - sizeAdd, zMin, uMin / texWidth, vMin / texHeight);
        vertices[2] = new Vertex(xMin - width - sizeAdd, yMin + sizeAdd, zMin, uMin / texWidth, (vMin + height) / texHeight);
        vertices[3] = new Vertex(xMin + sizeAdd, yMin + sizeAdd, zMin, (uMin + width)  / texWidth, (vMin + height) / texHeight);
        this.xMin = xMin - width - sizeAdd;
        this.yMin = yMin - height - sizeAdd;
        this.z = zMin;
        this.xMax = xMin + sizeAdd;
        this.yMax = yMin + sizeAdd;
        this.direction = Direction.UP.getUnitVector();
    }

    public void render(Matrix4f model, Matrix3f normal, VertexConsumer vertexConsumer, float r, float g, float b, float a, int o, int l) {
        Vector3f vector3f = direction.copy();
        vector3f.transform(normal);
        float nx = vector3f.getX();
        float ny = vector3f.getY();
        float nz = vector3f.getZ();

        for(int i = 0; i < 4; ++i) {
            Vertex vertex = vertices[i];
            float x = (float) (vertex.pos.getX() / 16.0F);
            float y = (float) (vertex.pos.getY() / 16.0F);
            float z = (float) (vertex.pos.getZ() / 16.0F);
            Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
            vector4f.transform(model);
            vertexConsumer.vertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), r, g, b, a, vertex.u, vertex.v, o, l, nx, ny, nz);
        }
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

        return new Quad(xMin, yMin, zMin, width, height, uMin, vMin, size, (float) bone.getTextureSize().x, (float) bone.getTextureSize().y);
    }
}
