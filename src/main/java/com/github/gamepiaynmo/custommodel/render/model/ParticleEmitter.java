package com.github.gamepiaynmo.custommodel.render.model;

import com.github.gamepiaynmo.custommodel.expression.ExpressionParser;
import com.github.gamepiaynmo.custommodel.expression.IExpressionBool;
import com.github.gamepiaynmo.custommodel.expression.IExpressionFloat;
import com.github.gamepiaynmo.custommodel.expression.ParseException;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.util.Json;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.github.gamepiaynmo.custommodel.util.Vector3;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Random;
import java.util.function.Supplier;

public class ParticleEmitter {
    private static double EPS = 1e-4;
    private static float R2D = 180 / (float) Math.PI;
    private static float D2R = (float) Math.PI / 180;

    private IExpressionFloat[] posRangeExpr;
    private IExpressionFloat dirRangeExpr;
    private IExpressionFloat[] angleExpr;
    private IExpressionFloat[] speedExpr;
    private IExpressionFloat[] rotSpeedExpr;
    private IExpressionFloat[] lifeSpanExpr;
    private IExpressionFloat densityExpr;
    private IExpressionFloat[][] colorExpr;
    private IExpressionFloat[] sizeExpr;
    private IExpressionFloat gravityExpr;
    private IExpressionBool collideExpr;

    protected Vector3 posRange = Vector3.Zero.cpy();
    protected float dirRange;
    protected float[] angle = new float[2];
    protected float[] speed = new float[2];
    protected float[] rotSpeed = new float[2];
    protected float[] lifeSpan = new float[2];
    protected float density;
    protected int[] animation;
    protected float[][] color = new float[4][];
    protected float[] size = new float[2];
    protected float gravity;
    protected boolean collide;

    protected double timer;
    private Random random = new Random();
    protected final Bone bone;
    protected boolean released = false;

    private ParticleEmitter(Bone parent) {
        bone = parent;
        for (int i = 0; i < 4; i++)
            color[i] = new float[2];
    }

    public static ParticleEmitter getParticleFromJson(Bone bone, JsonObject jsonObj) throws ParseException {
        ParticleEmitter emitter = new ParticleEmitter(bone);
        ExpressionParser parser = bone.getModel().getParser();

        emitter.posRangeExpr = Json.parseFloatExpressionArray(jsonObj.get(CustomJsonModel.POS_RANGE), 3, new float[] {0, 0, 0}, parser);
        emitter.dirRangeExpr = Json.getFloatExpression(jsonObj, CustomJsonModel.DIR_RANGE, 0, parser);
        emitter.angleExpr = Json.parseFloatExpressionArray(jsonObj.get(CustomJsonModel.ANGLE), 2, new float[] {0, 0}, parser);
        emitter.speedExpr = Json.parseFloatExpressionArray(jsonObj.get(CustomJsonModel.SPEED), 2, new float[] {0, 0}, parser);
        emitter.rotSpeedExpr = Json.parseFloatExpressionArray(jsonObj.get(CustomJsonModel.ROT_SPEED), 2, new float[] {0, 0}, parser);
        emitter.lifeSpanExpr = Json.parseFloatExpressionArray(jsonObj.get(CustomJsonModel.LIFE_SPAN), 2, new float[] {1, 1}, parser);
        emitter.densityExpr = Json.getFloatExpression(jsonObj, CustomJsonModel.DENSITY, 1, parser);
        emitter.animation = Json.parseIntArray(jsonObj.get(CustomJsonModel.ANIMATION), 2, new int[] {1, 1});
        emitter.colorExpr = new IExpressionFloat[4][];
        emitter.colorExpr[0] = Json.parseFloatExpressionArray(jsonObj.get(CustomJsonModel.COLOR_R), 2, new float[] {1, 1}, parser);
        emitter.colorExpr[1] = Json.parseFloatExpressionArray(jsonObj.get(CustomJsonModel.COLOR_G), 2, new float[] {1, 1}, parser);
        emitter.colorExpr[2] = Json.parseFloatExpressionArray(jsonObj.get(CustomJsonModel.COLOR_B), 2, new float[] {1, 1}, parser);
        emitter.colorExpr[3] = Json.parseFloatExpressionArray(jsonObj.get(CustomJsonModel.COLOR_A), 2, new float[] {1, 1}, parser);
        emitter.sizeExpr = Json.parseFloatExpressionArray(jsonObj.get(CustomJsonModel.SIZE), 2, new float[] {1, 1}, parser);
        emitter.gravityExpr = Json.getFloatExpression(jsonObj, CustomJsonModel.GRAVITY, 0, parser);
        emitter.collideExpr = Json.getBooleanExpression(jsonObj, CustomJsonModel.COLLIDE, false, parser);

        return emitter;
    }

    private void evalFloatArray(float[] arr, IExpressionFloat[] expr) {
        for (int i = 0; i < arr.length; i++)
            arr[i] = expr[i].eval();
    }

    public void tick(Matrix4 transform) {
        MinecraftClient client = MinecraftClient.getInstance();
        ParticleManager manager = client.particleManager;
        ClientWorld world = client.world;

        if (bone.isVisible()) {
            timer += 1;
            density = densityExpr.eval();
            if (density > EPS && timer > 0) {
                posRange = Vector3.Zero.set(posRangeExpr[0].eval(), posRangeExpr[1].eval(), posRangeExpr[2].eval());
                dirRange = dirRangeExpr.eval();
                evalFloatArray(angle, angleExpr);
                evalFloatArray(speed, speedExpr);
                evalFloatArray(rotSpeed, rotSpeedExpr);
                evalFloatArray(lifeSpan, lifeSpanExpr);
                density = densityExpr.eval();
                for (int i = 0; i < 4; i++)
                    evalFloatArray(color[i], colorExpr[i]);
                evalFloatArray(size, sizeExpr);
                gravity = gravityExpr.eval();
                collide = collideExpr.eval();

                Vector3 epos = new Vector3();
                epos.x = transform.val[12];
                epos.y = transform.val[13];
                epos.z = transform.val[14];

                Vector3[] edir = new Vector3[3];
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
                    particle.setAngle(D2R * MathHelper.lerp(random.nextFloat(), angle[0], angle[1]));
                    particle.setRotSpeed(MathHelper.lerp(random.nextFloat(), rotSpeed[0], rotSpeed[1]));
                    particle.setMaxAge((int) MathHelper.lerp(random.nextFloat(), lifeSpan[0], lifeSpan[1]));
                    particle.setSize(MathHelper.lerp(random.nextFloat(), size[0], size[1]));
                    particle.setColor(MathHelper.lerp(random.nextFloat(), color[0][0], color[0][1]),
                            MathHelper.lerp(random.nextFloat(), color[1][0], color[1][1]),
                            MathHelper.lerp(random.nextFloat(), color[2][0], color[2][1]));
                    particle.setAlpha(MathHelper.lerp(random.nextFloat(), color[3][0], color[3][1]));
                    particle.setGravity(gravity);
                    particle.setCollide(collide);
                    particle.setTexture(bone.getTexture().get());

                    manager.addParticle(particle);
                }
            }
        }
    }

    public void release() {
        released = true;
    }

}
