package com.github.gamepiaynmo.custommodel.render.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Vec3d;

public class Vertex {
    public final Vec3d pos;
    public final float u;
    public final float v;

    public Vertex(float f, float g, float h, float i, float j) {
        this(new Vec3d(f, g, h), i, j);
    }

    public Vertex remap(float f, float g) {
        return new Vertex(this.pos, f, g);
    }

    public Vertex(Vec3d vector3f, float f, float g) {
        this.pos = vector3f;
        this.u = f;
        this.v = g;
    }
}