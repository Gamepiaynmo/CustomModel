package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.entity.NpcHelper;
import com.github.gamepiaynmo.custommodel.util.LoadModelException;
import com.github.gamepiaynmo.custommodel.util.ModelNotFoundException;
import net.minecraft.command.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ServerCommand extends CommandBase {
    @Override
    public String getName() {
        return CustomModel.MODID;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.custommodel.usage";
    }

    private void checkPermission(ICommandSender sender, int level) throws CommandException {
        if (level > 0 && !sender.canUseCommand(level, getName()))
            throw new CommandException("commands.generic.permission");
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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        try {
            if (args.length < 1)
                throw new WrongUsageException("command.custommodel.usage");

            if (args[0].equals("refresh")) {
                checkPermission(sender, ModConfig.getListModelsPermission());
                CustomModel.manager.refreshModelList();
                sender.sendMessage(new TextComponentTranslation("command.custommodel.listmodels", CustomModel.manager.models.size()));
                return;
            }

            if (args[0].equals("list")) {
                checkPermission(sender, ModConfig.getListModelsPermission());
                sender.sendMessage(new TextComponentTranslation("command.custommodel.listmodels", CustomModel.manager.models.size()));
                for (ITextComponent text : CustomModel.manager.getServerModelInfoList())
                    sender.sendMessage(text);
                return;
            }

            if (args[0].equals("reload")) {
                if (args.length > 1) {
                    checkPermission(sender, ModConfig.getReloadOthersPermission());
                    Collection<EntityPlayerMP> players = getPlayers(server, sender, args[1]);
                    for (EntityPlayerMP player : players)
                        CustomModel.manager.reloadModel(player, true);
                    notifyCommandListener(sender, this, "command.custommodel.reload", players.size());
                } else {
                    checkPermission(sender, ModConfig.getReloadSelfPermission());
                    CustomModel.manager.reloadModel(getCommandSenderAsPlayer(sender), true);
                    notifyCommandListener(sender, this, "command.custommodel.reload", 1);
                }
                return;
            }

            if (args[0].equals("clear")) {
                if (args.length > 1) {
                    checkPermission(sender, ModConfig.getSelectOthersPermission());
                    Collection<EntityPlayerMP> players = getPlayers(server, sender, args[1]);
                    for (EntityPlayerMP player : players)
                        CustomModel.manager.clearModel(player);
                    notifyCommandListener(sender, this, "command.custommodel.clear", players.size());
                } else {
                    checkPermission(sender, ModConfig.getSelectSelfPermission());
                    CustomModel.manager.clearModel(getCommandSenderAsPlayer(sender));
                    notifyCommandListener(sender, this, "command.custommodel.clear", 1);
                }
                return;
            }

            if (args.length < 2)
                throw new WrongUsageException("command.custommodel.usage");

            if (args[0].equals("select")) {
                if (args.length > 2) {
                    checkPermission(sender, ModConfig.getSelectOthersPermission());
                    Collection<EntityPlayerMP> players = getPlayers(server, sender, args[2]);
                    for (EntityPlayerMP player : players)
                        CustomModel.manager.selectModel(getCommandSenderAsPlayer(sender), player, args[1]);
                    notifyCommandListener(sender, this, "command.custommodel.select", players.size(), args[1]);
                } else {
                    checkPermission(sender, ModConfig.getSelectSelfPermission());
                    CustomModel.manager.selectModel(getCommandSenderAsPlayer(sender), getCommandSenderAsPlayer(sender), args[1]);
                    notifyCommandListener(sender, this, "command.custommodel.select", 1, args[1]);
                }
                return;
            }

            if (CustomModel.hasnpc && args[0].equals("npc")) {
                checkPermission(sender, ModConfig.getSelectOthersPermission());
                EntityPlayerMP player = getCommandSenderAsPlayer(sender);
                EntityLivingBase entity = NpcHelper.getNearestNpc(player.world, player);
                if (entity != null)
                    NpcHelper.setCurrentModel(entity, args[1]);
                notifyCommandListener(sender, this, "command.custommodel.select", 1, args[1]);
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
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "refresh", "list", "reload", "select", "clear", "npc");

        switch (args[0]) {
            case "reload":
            case "clear":
                return args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.emptyList();
            case "select":
                return args.length == 2 ? getListOfStringsMatchingLastWord(args, CustomModel.manager.getServerModelIdList()) :
                args.length == 3 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.emptyList();
            case "npc":
                return getListOfStringsMatchingLastWord(args, CustomModel.manager.getServerModelIdList());
        }

        return Collections.emptyList();
    }
}
