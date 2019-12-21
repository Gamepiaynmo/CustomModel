package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.render.model.CustomParticle;
import com.github.gamepiaynmo.custommodel.render.model.CustomSheet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
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

    @Inject(method = "renderParticles", at = @At("HEAD"))
    public void renderParticles(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate, LightmapTextureManager lightmapTextureManager, Camera camera, float f, CallbackInfo info) {
        lightmapTextureManager.enable();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.enableFog();
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrixStack.peek().getModel());

        Queue<Particle> queue = particles.get(CustomSheet.CUSTOM_SHEET);
        if (queue != null) {
            ParticleTextureSheet particleTextureSheet = CustomSheet.CUSTOM_SHEET;
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            particleTextureSheet.begin(bufferBuilder, this.textureManager);
            boolean hideNear = ModConfig.isHideNearParticles();

            for (Particle particle_1 : this.particles.get(particleTextureSheet)) {
                CustomParticle particle = (CustomParticle) particle_1;
                try {
                    if (hideNear && camera.getPos().squaredDistanceTo(particle.getPos()) < 1)
                        continue;
                    if (!particle.isReleased()) {
                        textureManager.bindTexture(particle.getTexture());
                        particle.buildGeometry(bufferBuilder, camera, f);
                    }
                } catch (Throwable var16) {
                    CrashReport crashReport = CrashReport.create(var16, "Rendering Particle");
                    CrashReportSection crashReportSection = crashReport.addElement("Particle being rendered");
                    crashReportSection.add("Particle", particle::toString);
                    crashReportSection.add("Particle Type", particleTextureSheet::toString);
                    throw new CrashException(crashReport);
                }
            }

            particleTextureSheet.draw(tessellator);
        }

        RenderSystem.popMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultAlphaFunc();
        lightmapTextureManager.disable();
        RenderSystem.disableFog();
    }
}
