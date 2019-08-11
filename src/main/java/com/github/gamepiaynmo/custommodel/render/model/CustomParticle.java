package com.github.gamepiaynmo.custommodel.render.model;

import com.github.gamepiaynmo.custommodel.util.Vector3;
import com.ibm.icu.text.MessagePattern;
import com.sun.corba.se.spi.ior.IdentifiableFactory;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CustomParticle extends BillboardParticle {

    protected float rotSpeed;
    protected ParticleEmitter emitter;
    protected Identifier texture;

    private float minU, maxU, minV, maxV;

    protected CustomParticle(World world_1, ParticleEmitter emitter, Vector3 pos, Vector3 dir) {
        super(world_1, pos.x, -pos.y + 1.501, -pos.z);
        this.velocityX = dir.x;
        this.velocityY = -dir.y;
        this.velocityZ = -dir.z;
        this.emitter = emitter;
        calcUV();
    }

    @Override
    protected float getMinU() {
        return minU;
    }

    @Override
    protected float getMaxU() {
        return maxU;
    }

    @Override
    protected float getMinV() {
        return minV;
    }

    @Override
    protected float getMaxV() {
        return maxV;
    }

    @Override
    public ParticleTextureSheet getType() {
        return CustomSheet.CUSTOM_SHEET;
    }

    public void setAngle(float angle) { this.angle = angle; }
    public void setRotSpeed(float rotSpeed) { this.rotSpeed = rotSpeed; }
    public void setGravity(float gravity) { this.gravityStrength = gravity; }
    public void setCollide(boolean collide) { this.collidesWithWorld = collide; }
    public void setSize(float size) { this.scale = size; }
    public void setAlpha(float alpha) { this.setColorAlpha(alpha); }
    public void setTexture(Identifier texture) { this.texture = texture; }

    public Identifier getTexture() { return texture; }
    public Vec3d getPos() { return new Vec3d(x, y, z); }

    private void calcUV() {
        int cnt = emitter.animation[0] * emitter.animation[1];
        if (cnt <= 1) {
            minU = minV = 0;
            maxU = maxV = 1;
            return;
        }

        float width = 1.0f / emitter.animation[0];
        float height = 1.0f / emitter.animation[1];
        int cur = Math.min(cnt - 1, age * cnt / maxAge);

        int x = cur % emitter.animation[0];
        int y = cur / emitter.animation[1];
        minU = x * width;
        maxU = minU + width;
        minV = y * height;
        maxV = minV + height;
    }

    @Override
    public void tick() {
        super.tick();

        this.prevAngle = this.angle;
        this.angle += this.rotSpeed;

        calcUV();
    }
}
