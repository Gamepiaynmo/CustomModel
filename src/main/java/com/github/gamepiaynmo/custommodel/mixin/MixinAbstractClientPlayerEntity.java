package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.IStatureHandler;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.network.PacketQuery;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandException;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayerEntity extends PlayerEntity implements IStatureHandler {
    public MixinAbstractClientPlayerEntity(ClientWorld clientWorld_1, GameProfile gameProfile_1) {
        super(clientWorld_1, gameProfile_1);
    }

    protected ModelPack currentPack = null;

    @Override
    public void setModelPack(ModelPack modelPack) {
        currentPack = modelPack;
    }

    @Override
    public ModelPack getModelPack() {
        return currentPack;
    }

    @Override
    public void checkModelPack() {
        ModelPack pack = CustomModelClient.manager.getModelForPlayer((AbstractClientPlayerEntity) (PlayerEntity) this);
        if (pack != currentPack) {
            currentPack = pack;
            calculateDimensions();
        }
    }

    @Override
    public float getActiveEyeHeight(EntityPose entityPose, EntityDimensions entityDimensions) {
        if (currentPack != null && CustomModelClient.serverConfig.customEyeHeight) {
            Float eyeHeight = currentPack.getModel().getModelInfo().eyeHeightMap.get(entityPose);
            if (eyeHeight != null) {
                return eyeHeight;
            }
        }

        return super.getActiveEyeHeight(entityPose, entityDimensions);
    }

    @Override
    public EntityDimensions getDimensions(EntityPose entityPose) {
        if (currentPack != null && CustomModelClient.serverConfig.customBoundingBox) {
            EntityDimensions dimensions = currentPack.getModel().getModelInfo().dimensionsMap.get(entityPose);
            if (dimensions != null)
                return dimensions;
        }

        return super.getDimensions(entityPose);
    }
}
