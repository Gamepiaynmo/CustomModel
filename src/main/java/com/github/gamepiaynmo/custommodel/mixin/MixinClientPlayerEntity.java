package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.network.PacketQuery;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.github.gamepiaynmo.custommodel.client.command.CottonClientCommandSource;
import com.github.gamepiaynmo.custommodel.client.command.CommandCache;
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

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
    @Shadow @Final
    protected MinecraftClient client;

    @Shadow @Final
    public ClientPlayNetworkHandler networkHandler;

    public MixinClientPlayerEntity(ClientWorld clientWorld_1, GameProfile gameProfile_1) {
        super(clientWorld_1, gameProfile_1);
    }

    @Shadow
    public abstract void addChatMessage(Text text_1, boolean boolean_1);

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(String msg, CallbackInfo info) {
        if (!ClientSidePacketRegistry.INSTANCE.canServerReceive(PacketQuery.ID)) {
            if (msg.length() < 2 || !msg.startsWith("/")) return;
            if (!CommandCache.hasCommand(msg.substring(1).split(" ")[0])) return;
            boolean cancel = false;
            try {
                // The game freezes when using heavy commands. Run your heavy code somewhere else pls
                int result = CommandCache.execute(
                        msg.substring(1), (CottonClientCommandSource) new ClientCommandSource(networkHandler, client)
                );
                if (result != 0)
                    // Prevent sending the message
                    cancel = true;
            } catch (CommandException e) {
                addChatMessage(e.getMessageText().formatted(Formatting.RED), false);
                cancel = true;
            } catch (CommandSyntaxException e) {
                addChatMessage(new LiteralText(e.getMessage()).formatted(Formatting.RED), false);
                cancel = true;
            } catch (Exception e) {
                addChatMessage(new TranslatableText("command.failed").formatted(Formatting.RED), false);
                cancel = true;
            }

            if (cancel)
                info.cancel();
        }
    }

    private ModelPack currentPack = null;

    @Inject(method = "tick()V", at = @At("TAIL"))
    public void updateBoundingBox(CallbackInfo info) {
        if (CustomModelClient.serverConfig != null) {
            ModelPack pack = CustomModelClient.getModelForPlayer(this);
            if (pack != currentPack) {
                currentPack = pack;
                calculateDimensions();
            }
        }
    }

    @Override
    public float getActiveEyeHeight(EntityPose entityPose, EntityDimensions entityDimensions) {
        if (currentPack != null && CustomModelClient.serverConfig.customEyeHeight) {
            Float eyeHeight = currentPack.getModel().eyeHeightMap.get(entityPose);
            if (eyeHeight != null) {
                return eyeHeight;
            }
        }

        return super.getActiveEyeHeight(entityPose, entityDimensions);
    }

    @Override
    public EntityDimensions getDimensions(EntityPose entityPose) {
        if (currentPack != null && CustomModelClient.serverConfig.customBoundingBox) {
            EntityDimensions dimensions = currentPack.getModel().dimensionsMap.get(entityPose);
            if (dimensions != null)
                return dimensions;
        }

        return super.getDimensions(entityPose);
    }
}
