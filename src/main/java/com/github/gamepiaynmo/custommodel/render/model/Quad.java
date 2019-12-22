package com.github.gamepiaynmo.custommodel.render.model;

import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.util.Json;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

public class Quad {
    public PositionTextureVertex[] vertices;
    public float xMin;
    public float yMin;
    public float z;
    public float xMax;
    public float yMax;

    protected Quad(PositionTextureVertex[] vertexs_1, float int_1, float int_2, float int_3, float int_4, float float_1, float float_2) {
        vertexs_1[0] = vertexs_1[0].setTexturePosition(int_3 / float_1, int_2 / float_2);
        vertexs_1[1] = vertexs_1[1].setTexturePosition(int_1 / float_1, int_2 / float_2);
        vertexs_1[2] = vertexs_1[2].setTexturePosition(int_1 / float_1, int_4 / float_2);
        vertexs_1[3] = vertexs_1[3].setTexturePosition(int_3 / float_1, int_4 / float_2);
        this.vertices = vertexs_1;
    }

    protected Quad(float xMin, float yMin, float zMin, float width, float height, float uMin, float vMin, float sizeAdd, float texWidth, float texHeight) {
        vertices = new PositionTextureVertex[4];
        vertices[0] = new PositionTextureVertex(xMin + sizeAdd, yMin - height - sizeAdd, zMin, (uMin + width) / texWidth, vMin / texHeight);
        vertices[1] = new PositionTextureVertex(xMin - width - sizeAdd, yMin - height - sizeAdd, zMin, uMin / texWidth, vMin / texHeight);
        vertices[2] = new PositionTextureVertex(xMin - width - sizeAdd, yMin + sizeAdd, zMin, uMin / texWidth, (vMin + height) / texHeight);
        vertices[3] = new PositionTextureVertex(xMin + sizeAdd, yMin + sizeAdd, zMin, (uMin + width)  / texWidth, (vMin + height) / texHeight);
        this.xMin = xMin - width - sizeAdd;
        this.yMin = yMin - height - sizeAdd;
        this.z = zMin;
        this.xMax = xMin + sizeAdd;
        this.yMax = yMin + sizeAdd;
    }

    public void render(BufferBuilder bufferBuilder_1, float float_1) {
        Vec3d vec3d_1 = this.vertices[1].vector3D.subtractReverse(this.vertices[0].vector3D);
        Vec3d vec3d_2 = this.vertices[1].vector3D.subtractReverse(this.vertices[2].vector3D);
        Vec3d vec3d_3 = vec3d_2.crossProduct(vec3d_1).normalize();
        float float_2 = (float)vec3d_3.x;
        float float_3 = (float)vec3d_3.y;
        float float_4 = (float)vec3d_3.z;

        bufferBuilder_1.begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);

        for(int int_1 = 0; int_1 < 4; ++int_1) {
            PositionTextureVertex vertex_1 = this.vertices[int_1];
            bufferBuilder_1.pos(vertex_1.vector3D.x * (double)float_1, vertex_1.vector3D.y * (double)float_1, vertex_1.vector3D.z * (double)float_1).tex(vertex_1.texturePositionX, vertex_1.texturePositionY).normal(float_2, float_3, float_4).endVertex();
        }

        Tessellator.getInstance().draw();
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
