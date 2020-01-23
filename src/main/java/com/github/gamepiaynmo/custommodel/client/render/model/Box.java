package com.github.gamepiaynmo.custommodel.client.render.model;

import com.github.gamepiaynmo.custommodel.client.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.client.render.RenderContext;
import com.github.gamepiaynmo.custommodel.util.Json;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Vertex;
import net.minecraft.client.render.BufferBuilder;

@Environment(EnvType.CLIENT)
public class Box {
    private final Vertex[] vertices;
    private final Quad[] polygons;
    public final float xMin;
    public final float yMin;
    public final float zMin;
    public final float xMax;
    public final float yMax;
    public final float zMax;

    private Box(int int_1, int int_2, float float_1, float float_2, float float_3, float int_3, float int_4, float int_5, float float_4, float texWidth, float texHeight, boolean mirror) {
        this.xMin = float_1 - int_3 - float_4;
        this.yMin = float_2 - int_4 - float_4;
        this.zMin = float_3 - int_5 - float_4;
        this.xMax = float_1 + float_4;
        this.yMax = float_2 + float_4;
        this.zMax = float_3 + float_4;
        this.vertices = new Vertex[8];
        this.polygons = new Quad[6];
        float float_5 = float_1;
        float float_6 = float_2;
        float float_7 = float_3;
        float_1 -= int_3 + float_4;
        float_2 -= int_4 + float_4;
        float_3 -= int_5 + float_4;
        float_5 += float_4;
        float_6 += float_4;
        float_7 += float_4;

        if (mirror) {
            float f3 = float_5;
            float_5 = float_1;
            float_1 = f3;
        }

        Vertex vertex_1 = new Vertex(float_1, float_2, float_3, 0.0F, 0.0F);
        Vertex vertex_2 = new Vertex(float_5, float_2, float_3, 0.0F, 8.0F);
        Vertex vertex_3 = new Vertex(float_5, float_6, float_3, 8.0F, 8.0F);
        Vertex vertex_4 = new Vertex(float_1, float_6, float_3, 8.0F, 0.0F);
        Vertex vertex_5 = new Vertex(float_1, float_2, float_7, 0.0F, 0.0F);
        Vertex vertex_6 = new Vertex(float_5, float_2, float_7, 0.0F, 8.0F);
        Vertex vertex_7 = new Vertex(float_5, float_6, float_7, 8.0F, 8.0F);
        Vertex vertex_8 = new Vertex(float_1, float_6, float_7, 8.0F, 0.0F);
        this.vertices[0] = vertex_1;
        this.vertices[1] = vertex_2;
        this.vertices[2] = vertex_3;
        this.vertices[3] = vertex_4;
        this.vertices[4] = vertex_5;
        this.vertices[5] = vertex_6;
        this.vertices[6] = vertex_7;
        this.vertices[7] = vertex_8;
        this.polygons[0] = new Quad(new Vertex[]{vertex_6, vertex_2, vertex_3, vertex_7}, int_1 + int_5 + int_3, int_2 + int_5, int_1 + int_5 + int_3 + int_5, int_2 + int_5 + int_4, texWidth, texHeight);
        this.polygons[1] = new Quad(new Vertex[]{vertex_1, vertex_5, vertex_8, vertex_4}, int_1, int_2 + int_5, int_1 + int_5, int_2 + int_5 + int_4, texWidth, texHeight);
        this.polygons[2] = new Quad(new Vertex[]{vertex_6, vertex_5, vertex_1, vertex_2}, int_1 + int_5, int_2, int_1 + int_5 + int_3, int_2 + int_5, texWidth, texHeight);
        this.polygons[3] = new Quad(new Vertex[]{vertex_3, vertex_4, vertex_8, vertex_7}, int_1 + int_5 + int_3, int_2 + int_5, int_1 + int_5 + int_3 + int_3, int_2, texWidth, texHeight);
        this.polygons[4] = new Quad(new Vertex[]{vertex_2, vertex_1, vertex_4, vertex_3}, int_1 + int_5, int_2 + int_5, int_1 + int_5 + int_3, int_2 + int_5 + int_4, texWidth, texHeight);
        this.polygons[5] = new Quad(new Vertex[]{vertex_5, vertex_6, vertex_7, vertex_8}, int_1 + int_5 + int_3 + int_5, int_2 + int_5, int_1 + int_5 + int_3 + int_5 + int_3, int_2 + int_5 + int_4, texWidth, texHeight);

        if (mirror) {
            for (Quad texturedquad : this.polygons) {
                texturedquad.flipFace();
            }
        }
    }

    public void render(BufferBuilder bufferBuilder_1, float float_1) {
        Quad[] var3 = this.polygons;
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Quad quad_1 = var3[var5];
            quad_1.render(bufferBuilder_1, float_1);
        }

    }

    public static Box getBoxFromJson(Bone bone, JsonObject jsonObj, RenderContext context) {
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

        boolean mirror = Json.getBoolean(jsonObj, CustomJsonModel.MIRROR, false);
        return new Box(uMin, vMin, xMin, yMin, zMin, width, height, depth, size, (float) bone.getTextureSize(context).x, (float) bone.getTextureSize(context).y, mirror);
    }
}
