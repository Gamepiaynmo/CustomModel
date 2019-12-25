package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.util.LoadModelException;
import com.github.gamepiaynmo.custommodel.util.ModelNotFoundException;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.IClientCommand;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Command implements IClientCommand {
    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }

    @Override
    public String getName() {
        return CustomModel.MODID;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.custommodel.usage";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        if ("reload".equalsIgnoreCase(args[0]) || "clear".equalsIgnoreCase(args[0]))
            return index == 1;

        if ("select".equalsIgnoreCase(args[0]))
            return index == 2;

        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        try {
            if (args.length < 1)
                throw new WrongUsageException("command.custommodel.usage");

            if (args[0].equals("refresh")) {
                CustomModel.refreshModelList();
                sender.sendMessage(new TextComponentTranslation("command.custommodel.listmodels", CustomModel.models.size()));
                return;
            }

            if (args[0].equals("list")) {
                sender.sendMessage(new TextComponentTranslation("command.custommodel.listmodels", CustomModel.models.size()));
                for (ITextComponent text : CustomModel.getModelInfoList())
                    sender.sendMessage(text);
                return;
            }

            if (args[0].equals("reload")) {
                if (args.length > 1) {
                    Collection<GameProfile> players = getPlayers(args[1]);
                    for (GameProfile player : players)
                        CustomModelClient.reloadModel(player);
                    sender.sendMessage(new TextComponentTranslation("command.custommodel.reload", players.size()));
                } else {
                    CustomModelClient.reloadModel(Minecraft.getMinecraft().player.getGameProfile());
                    sender.sendMessage(new TextComponentTranslation("command.custommodel.reload", 1));
                }
                return;
            }

            if (args[0].equals("clear")) {
                if (args.length > 1) {
                    Collection<GameProfile> players = getPlayers(args[1]);
                    for (GameProfile player : players)
                        CustomModelClient.clearModel(player);
                    sender.sendMessage(new TextComponentTranslation("command.custommodel.clear", players.size()));
                } else {
                    CustomModelClient.clearModel(Minecraft.getMinecraft().player.getGameProfile());
                    sender.sendMessage(new TextComponentTranslation("command.custommodel.clear", 1));
                }
                return;
            }

            if (args.length < 2)
                throw new WrongUsageException("command.custommodel.usage");

            if (args[0].equals("select")) {
                if (args.length > 2) {
                    Collection<GameProfile> players = getPlayers(args[2]);
                    for (GameProfile player : players)
                        CustomModelClient.selectModel(player, args[1]);
                    sender.sendMessage(new TextComponentTranslation("command.custommodel.select", players.size(), args[1]));
                } else {
                    CustomModelClient.selectModel(Minecraft.getMinecraft().player.getGameProfile(), args[1]);
                    sender.sendMessage(new TextComponentTranslation("command.custommodel.select", 1, args[1]));
                }
                return;
            }
        } catch (LoadModelException e) {
            ITextComponent text = new TextComponentTranslation("error.custommodel.loadmodelpack", e.getFileName(), e.getMessage());
            text.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(text);
            return;
        } catch (ModelNotFoundException e) {
            ITextComponent text = new TextComponentString(e.getMessage());
            text.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(text);
            return;
        }

        throw new WrongUsageException("command.custommodel.usage");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (!CustomModelClient.isServerModded) {
            if (args.length == 1)
                return CommandBase.getListOfStringsMatchingLastWord(args, "refresh", "list", "reload", "select", "clear");

            switch (args[0]) {
                case "reload":
                case "clear":
                    return args.length == 2 ? CommandBase.getListOfStringsMatchingLastWord(args, getPlayerNames()) : Collections.emptyList();
                case "select":
                    return args.length == 2 ? CommandBase.getListOfStringsMatchingLastWord(args, CustomModel.getModelIdList()) :
                            args.length == 3 ? CommandBase.getListOfStringsMatchingLastWord(args, getPlayerNames()) : Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    private List<String> getPlayerNames() {
        List<String> res = Lists.newArrayList();
        for (NetworkPlayerInfo info : Minecraft.getMinecraft().getConnection().getPlayerInfoMap())
            res.add(info.getGameProfile().getName());
        return res;
    }

    private List<GameProfile> getPlayers(String predict) {
        List<GameProfile> res = Lists.newArrayList();
        for (NetworkPlayerInfo info : Minecraft.getMinecraft().getConnection().getPlayerInfoMap()) {
            if (info.getGameProfile().getName().startsWith(predict))
                res.add(info.getGameProfile());
        }
        return res;
    }
}
