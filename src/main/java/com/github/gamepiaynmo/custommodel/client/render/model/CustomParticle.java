package com.github.gamepiaynmo.custommodel.client.render.model;

import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.util.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CustomParticle extends Particle {

    protected float rotSpeed;
    protected ParticleEmitter emitter;
    protected ResourceLocation texture;

    private float minU, maxU, minV, maxV;

    protected CustomParticle(World world_1, ParticleEmitter emitter, Vector3 pos, Vector3 dir) {
        super(world_1, pos.x, pos.y, pos.z);
        this.motionX = dir.x;
        this.motionY = dir.y;
        this.motionZ = dir.z;
        this.emitter = emitter;
        calcUV();
    }

    @Override
    public int getFXLayer()
    {
        return 3;
    }

    protected float getMinU() {
        return minU;
    }

    protected float getMaxU() {
        return maxU;
    }

    protected float getMinV() {
        return minV;
    }

    protected float getMaxV() {
        return maxV;
    }

    public void setAngle(float angle) { this.particleAngle = angle; }
    public void setRotSpeed(float rotSpeed) { this.rotSpeed = rotSpeed; }
    public void setGravity(float gravity) { this.particleGravity = gravity; }
    public void setCollide(boolean collide) { this.canCollide = collide; }
    public void setSize(float size) { this.particleScale = size; }
    public void setAlpha(float alpha) { this.setAlphaF(alpha); }
    public void setTexture(ResourceLocation texture) { this.texture = texture; }

    public ResourceLocation getTexture() { return texture; }
    public Vec3d getPos() { return new Vec3d(posX, posY, posZ); }

    public boolean isReleased() { return emitter.released; }

    private void calcUV() {
        int cnt = emitter.animation[0] * emitter.animation[1];
        if (cnt <= 1) {
            minU = minV = 0;
            maxU = maxV = 1;
            return;
        }

        float width = 1.0f / emitter.animation[0];
        float height = 1.0f / emitter.animation[1];
        int cur = Math.min(cnt - 1, particleAge * cnt / particleMaxAge);

        int x = cur % emitter.animation[0];
        int y = cur / emitter.animation[1];
        minU = x * width;
        maxU = minU + width;
        minV = y * height;
        maxV = minV + height;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        this.prevParticleAngle = this.particleAngle;
        this.particleAngle += this.rotSpeed;

        calcUV();

        if (isReleased())
            setExpired();
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if (ModConfig.isHideNearParticles() && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0
            && entityIn.getPositionEyes(partialTicks).squareDistanceTo(this.getPos()) < 1)
            return;

        float f4 = this.particleScale;
        float f = this.getMinU();
        float f1 = this.getMaxU();
        float f2 = this.getMinV();
        float f3 = this.getMaxV();

        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        Vec3d[] avec3d = new Vec3d[] {new Vec3d((double)(-rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(-rotationYZ * f4 - rotationXZ * f4)), new Vec3d((double)(-rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(-rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(rotationYZ * f4 - rotationXZ * f4))};

        if (this.particleAngle != 0.0F)
        {
            float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
            float f9 = MathHelper.cos(f8 * 0.5F);
            float f10 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.x;
            float f11 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.y;
            float f12 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.z;
            Vec3d vec3d = new Vec3d((double)f10, (double)f11, (double)f12);

            for (int l = 0; l < 4; ++l)
            {
                avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double)(2.0F * f9)));
            }
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(516, 0.003921569F);

        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos((double)f5 + avec3d[0].x, (double)f6 + avec3d[0].y, (double)f7 + avec3d[0].z).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double)f5 + avec3d[1].x, (double)f6 + avec3d[1].y, (double)f7 + avec3d[1].z).tex((double)f1, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double)f5 + avec3d[2].x, (double)f6 + avec3d[2].y, (double)f7 + avec3d[2].z).tex((double)f, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double)f5 + avec3d[3].x, (double)f6 + avec3d[3].y, (double)f7 + avec3d[3].z).tex((double)f, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        Tessellator.getInstance().draw();
    }

}
