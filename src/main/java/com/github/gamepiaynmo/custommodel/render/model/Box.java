package com.github.gamepiaynmo.custommodel.render.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Cuboid;
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

    public Box(int int_1, int int_2, float float_1, float float_2, float float_3, float int_3, float int_4, float int_5, float float_4, float texWidth, float texHeight) {
        this.xMin = float_1 - int_3;
        this.yMin = float_2 - int_4;
        this.zMin = float_3 - int_5;
        this.xMax = float_1;
        this.yMax = float_2;
        this.zMax = float_3;
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

    }

    public void render(BufferBuilder bufferBuilder_1, float float_1) {
        Quad[] var3 = this.polygons;
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Quad quad_1 = var3[var5];
            quad_1.render(bufferBuilder_1, float_1);
        }

    }
}
