package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.render.model.CustomParticle;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

@Mixin(ParticleManager.class)
public class MixinParticleManager {
    @Shadow
    private Map<ParticleTextureSheet, Queue<Particle>> particles;
    @Shadow
    private TextureManager textureManager;

    @Inject(method = "renderParticles(Lnet/minecraft/client/render/Camera;F)V", at = @At("HEAD"))
    public void renderParticles(Camera camera_1, float float_1, CallbackInfo info) {
        Queue<Particle> queue = particles.get(CustomSheet.CUSTOM_SHEET);
        if (queue != null) {
            float float_2 = MathHelper.cos(camera_1.getYaw() * 0.017453292F);
            float float_3 = MathHelper.sin(camera_1.getYaw() * 0.017453292F);
            float float_4 = -float_3 * MathHelper.sin(camera_1.getPitch() * 0.017453292F);
            float float_5 = float_2 * MathHelper.sin(camera_1.getPitch() * 0.017453292F);
            float float_6 = MathHelper.cos(camera_1.getPitch() * 0.017453292F);
            Particle.cameraX = camera_1.getPos().x;
            Particle.cameraY = camera_1.getPos().y;
            Particle.cameraZ = camera_1.getPos().z;

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Tessellator tessellator_1 = Tessellator.getInstance();
            BufferBuilder bufferBuilder_1 = tessellator_1.getBufferBuilder();
            Iterator var13 = queue.iterator();

            GuiLighting.disable();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.alphaFunc(516, 0.003921569F);
            boolean hideNear = ModConfig.isHideNearParticles();

            while(var13.hasNext()) {
                CustomParticle particle_1 = (CustomParticle)var13.next();
                if (hideNear && camera_1.getPos().squaredDistanceTo(particle_1.getPos()) < 1)
                    continue;

                try {
                    if (!particle_1.isReleased()) {
                        textureManager.bindTexture(particle_1.getTexture());
                        bufferBuilder_1.begin(7, VertexFormats.POSITION_UV_COLOR_LMAP);
                        particle_1.buildGeometry(bufferBuilder_1, camera_1, float_1, float_2, float_6, float_3, float_4, float_5);
                        tessellator_1.draw();
                    }
                } catch (Throwable var18) {
                    CrashReport crashReport_1 = CrashReport.create(var18, "Rendering Particle");
                    CrashReportSection crashReportSection_1 = crashReport_1.addElement("Particle being rendered");
                    crashReportSection_1.add("Particle", particle_1::toString);
                    crashReportSection_1.add("Particle Type", CustomSheet.CUSTOM_SHEET::toString);
                    throw new CrashException(crashReport_1);
                }
            }
        }
    }
}
