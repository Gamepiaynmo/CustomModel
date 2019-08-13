package com.github.gamepiaynmo.custommodel.render.model;

import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.util.*;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class ParticleEmitter {
    private static double EPS = 1e-4;
    private static float R2D = 180 / (float) Math.PI;
    private static float D2R = (float) Math.PI / 180;

    protected Vector3 posRange = Vector3.Zero;
    protected double dirRange;
    protected double[] angle;
    protected double[] speed;
    protected double[] rotSpeed;
    protected double[] lifeSpan;
    protected double density;
    protected int[] animation;
    protected double[][] color;
    protected double[] size;
    protected double gravity;
    protected boolean collide;

    protected double timer;
    private Random random = new Random();
    private ModelPack.TextureGetter texture;
    protected final Bone bone;
    protected boolean released = false;

    private ParticleEmitter(Bone parent) { bone = parent; }

    public static ParticleEmitter getParticleFromJson(Bone bone, JsonObject jsonObj) {
        ParticleEmitter emitter = new ParticleEmitter(bone);

        emitter.posRange = new Vector3(Json.parseDoubleArray(jsonObj.get(CustomJsonModel.POS_RANGE), 3, new double[] {0, 0, 0}));
        emitter.dirRange = Json.getDouble(jsonObj, CustomJsonModel.DIR_RANGE, 0);
        emitter.angle = Json.parseDoubleArray(jsonObj.get(CustomJsonModel.ANGLE), 2, new double[] {0, 0});
        emitter.speed = Json.parseDoubleArray(jsonObj.get(CustomJsonModel.SPEED), 2, new double[] {0, 0});
        emitter.rotSpeed = Json.parseDoubleArray(jsonObj.get(CustomJsonModel.ROT_SPEED), 2, new double[] {0, 0});
        emitter.lifeSpan = Json.parseDoubleArray(jsonObj.get(CustomJsonModel.LIFE_SPAN), 2, new double[] {1, 1});
        emitter.density = Json.getDouble(jsonObj, CustomJsonModel.DENSITY, 1);
        emitter.animation = Json.parseIntArray(jsonObj.get(CustomJsonModel.ANIMATION), 2, new int[] {1, 1});
        emitter.color = new double[4][];
        emitter.color[0] = Json.parseDoubleArray(jsonObj.get(CustomJsonModel.COLOR_R), 2, new double[] {1, 1});
        emitter.color[1] = Json.parseDoubleArray(jsonObj.get(CustomJsonModel.COLOR_G), 2, new double[] {1, 1});
        emitter.color[2] = Json.parseDoubleArray(jsonObj.get(CustomJsonModel.COLOR_B), 2, new double[] {1, 1});
        emitter.color[3] = Json.parseDoubleArray(jsonObj.get(CustomJsonModel.COLOR_A), 2, new double[] {1, 1});
        emitter.size = Json.parseDoubleArray(jsonObj.get(CustomJsonModel.SIZE), 2, new double[] {1, 1});
        emitter.gravity = Json.getDouble(jsonObj, CustomJsonModel.GRAVITY, 0);
        emitter.collide = Json.getBoolean(jsonObj, CustomJsonModel.COLLIDE, false);
        emitter.texture = bone.getTexture();

        return emitter;
    }

    public void tick(AbstractClientPlayerEntity playerEntity, Matrix4 transform) {
        MinecraftClient client = MinecraftClient.getInstance();
        ParticleManager manager = client.particleManager;
        ClientWorld world = client.world;

        if (density > EPS) {
            timer += 1;
            if (timer > 0) {
                Vector3 epos = new Vector3();
                epos.x = transform.val[12];
                epos.y = transform.val[13];
                epos.z = transform.val[14];

                Vector3 edir[] = new Vector3[3];
                for (int i = 0; i < 3; i++) {
                    edir[i] = new Vector3();
                    edir[i].x = transform.val[i * 4];
                    edir[i].y = transform.val[i * 4 + 1];
                    edir[i].z = transform.val[i * 4 + 2];
                }

                while (timer > 0) {
                    timer -= 1 / density;
                    Vector3 pos = epos.cpy();
                    pos.add(edir[0].cpy().scl(MathHelper.lerp(random.nextDouble(), -posRange.x, posRange.x)));
                    pos.add(edir[1].cpy().scl(MathHelper.lerp(random.nextDouble(), -posRange.y, posRange.y)));
                    pos.add(edir[2].cpy().scl(MathHelper.lerp(random.nextDouble(), -posRange.z, posRange.z)));

                    Vector3 dir = edir[2].cpy();
                    dir.rotate(edir[0], MathHelper.lerp(random.nextDouble(), -dirRange, dirRange));
                    dir.rotate(edir[1], MathHelper.lerp(random.nextDouble(), -dirRange, dirRange));
                    dir.scl(MathHelper.lerp(random.nextDouble(), speed[0], speed[1]));

                    CustomParticle particle = new CustomParticle(world, this, pos, dir);
                    particle.setAngle(D2R * (float) MathHelper.lerp(random.nextDouble(), angle[0], angle[1]));
                    particle.setRotSpeed((float) MathHelper.lerp(random.nextDouble(), rotSpeed[0], rotSpeed[1]));
                    particle.setMaxAge((int) MathHelper.lerp(random.nextDouble(), lifeSpan[0], lifeSpan[1]));
                    particle.setSize((float) MathHelper.lerp(random.nextDouble(), size[0], size[1]));
                    particle.setColor((float) MathHelper.lerp(random.nextDouble(), color[0][0], color[0][1]),
                            (float) MathHelper.lerp(random.nextDouble(), color[1][0], color[1][1]),
                            (float) MathHelper.lerp(random.nextDouble(), color[2][0], color[2][1]));
                    particle.setAlpha((float) MathHelper.lerp(random.nextDouble(), color[3][0], color[3][1]));
                    particle.setGravity((float) gravity);
                    particle.setCollide(collide);
                    particle.setTexture(bone.getTexture().getTexture(playerEntity));

                    manager.addParticle(particle);
                }
            }
        }
    }

    public void release() {
        released = true;
    }

}
