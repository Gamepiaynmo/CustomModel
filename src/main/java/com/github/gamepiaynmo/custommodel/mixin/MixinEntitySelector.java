package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.client.Command;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandSource;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(EntitySelector.class)
public abstract class MixinEntitySelector implements Command.IClientEntitySelector {
    @Shadow
    private boolean checkPermissions;
    @Shadow
    private String playerName;
    @Shadow
    private UUID uuid;
    @Shadow
    private Function<Vec3d, Vec3d> positionOffset;
    @Shadow
    private boolean senderOnly;
    @Shadow
    abstract Predicate<Entity> getPositionPredicate(Vec3d vec3d);
    @Shadow
    abstract <T extends Entity> List<T> getEntities(Vec3d vec3d_1, List<T> list_1);

    private void check(CommandSource commandSource) throws CommandSyntaxException {
        if (this.checkPermissions && !commandSource.hasPermissionLevel(2)) {
            throw EntityArgumentType.NOT_ALLOWED_EXCEPTION.create();
        }
    }

    public List<GameProfile> getPlayers(CommandSource commandSource) throws CommandSyntaxException {
//        this.check(commandSource);
        if (this.playerName != null) {
            PlayerListEntry entry = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(this.playerName);
            return entry != null ? Lists.newArrayList(entry.getProfile()) : Collections.emptyList();
        } else if (this.uuid != null) {
            PlayerListEntry entry = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(this.uuid);
            return entry != null ? Lists.newArrayList(entry.getProfile()) : Collections.emptyList();
        } else {
            Vec3d vec3d_1 = this.positionOffset.apply(MinecraftClient.getInstance().player.getPosVector());
            Predicate<Entity> predicate = this.getPositionPredicate(vec3d_1);
            if (this.senderOnly) {
                PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                if (predicate.test(playerEntity)) {
                    return Lists.newArrayList(playerEntity.getGameProfile());
                }

                return Collections.emptyList();
            } else {
                List<PlayerEntity> list = Lists.newArrayList();
                for (PlayerEntity player : MinecraftClient.getInstance().world.getPlayers())
                    if (predicate.test(player))
                        list.add(player);

                list = this.getEntities(vec3d_1, list);
                List<GameProfile> plist = Lists.newArrayList();
                list.forEach(entity -> plist.add(entity.getGameProfile()));
                return plist;
            }
        }
    }

}
