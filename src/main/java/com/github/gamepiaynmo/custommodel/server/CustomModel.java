package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.api.IModelSelector;
import com.github.gamepiaynmo.custommodel.network.PacketModel;
import com.github.gamepiaynmo.custommodel.network.PacketQuery;
import com.github.gamepiaynmo.custommodel.network.PacketQueryConfig;
import com.github.gamepiaynmo.custommodel.network.PacketReplyConfig;
import com.github.gamepiaynmo.custommodel.util.LoadModelException;
import com.github.gamepiaynmo.custommodel.util.ModelNotFoundException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import com.github.gamepiaynmo.custommodel.server.selector.DefaultModelSelector;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class CustomModel {
    public static final String MODID = "custommodel";
    public static final String MODEL_DIR = "custom-models";

    public static final Logger LOGGER = LogManager.getLogger();

    private static final Map<UUID, ModelInfo> modelMap = Maps.newHashMap();
    private static int clearCounter = 0;
    public static MinecraftServer server;
    public static final Map<String, ModelInfo> models = Maps.newHashMap();

    private static final IModelSelector defaultSelector = new DefaultModelSelector();
    private static IModelSelector modelSelector = defaultSelector;

    private static void sendPacket(EntityPlayer player, ResourceLocation id, Packet<?> packet) {
        if (ServerSidePacketRegistry.INSTANCE.canPlayerReceive(player, id)) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            try {
                packet.write(buf);
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, id, buf);
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    private static Packet<?> formPacket(ResourceLocation id, Packet<?> packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        try {
            packet.write(buf);
            return new CustomPayloadS2CPacket(id, buf);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            return packet;
        }
    }

    public static void setModelSelector(IModelSelector modelSelector) {
        CustomModel.modelSelector = modelSelector;
        if (modelSelector == null)
            CustomModel.modelSelector = defaultSelector;
    }

    public static IModelSelector getModelSelector() {
        return CustomModel.modelSelector;
    }

    public static ModelInfo getBoundingBoxForPlayer(EntityPlayer playerEntity) {
        return modelMap.get(EntityPlayer.getUUID(playerEntity.getGameProfile()));
    }

    public static void refreshModelList() {
        models.clear();
        for (File file : new File(CustomModel.MODEL_DIR).listFiles()) {
            try {
                ModelInfo info = ModelInfo.fromFile(file);
                ModelInfo old = models.get(info.modelId);
                if (old == null || info.version.compareTo(old.version) > 0)
                    models.put(info.modelId, info);
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    public static Collection<ITextComponent> getModelInfoList() {
        List<ITextComponent> res = Lists.newArrayList();
        for (Map.Entry<String, ModelInfo> entry : models.entrySet()) {
            ModelInfo info = entry.getValue();
            ITextComponent text = new TextComponentString(info.modelName);
            Style style = text.getStyle();

            TextComponentTranslation hoverText = new TextComponentTranslation("text.custommodel.modelinfo.name", info.modelName);
            hoverText.appendText("\n").appendSibling(new TextComponentTranslation("text.custommodel.modelinfo.id", info.modelId));
            if (info.version.length() > 0)
                hoverText.appendText("\n").appendSibling(new TextComponentTranslation("text.custommodel.modelinfo.version", info.version));
            if (info.author.length() > 0)
                hoverText.appendText("\n").appendSibling(new TextComponentTranslation("text.custommodel.modelinfo.author", info.author));

            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
            style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/" + CustomModel.MODID + " select " + info.modelId));
            res.add(text);
        }
        return res;
    }

    public static Collection<String> getModelIdList() {
        List<String> res = Lists.newArrayList();
        for (Map.Entry<String, ModelInfo> entry : models.entrySet())
            res.add(entry.getKey());
        return res;
    }

    public static void reloadModel(EntityPlayer receiver, boolean broadcast) throws LoadModelException {
        reloadModel(receiver, EntityPlayer.getUUID(receiver.getGameProfile()), broadcast);
    }

    private static void reloadModel(EntityPlayer receiver, UUID uuid, boolean broadcast) throws LoadModelException {
        EntityPlayerMP playerEntity = server.getPlayerList().getPlayerByUUID(uuid);
        GameProfile profile = playerEntity.getGameProfile();
        uuid = EntityPlayer.getUUID(profile);

        String entry = modelSelector.getModelForPlayer(profile);
        ModelInfo info = models.get(entry);

        try {
            if (info == null)
                throw new ModelNotFoundException(entry);
            File modelFile = new File(CustomModel.MODEL_DIR + "/" + info.fileName);

            if (modelFile.exists()) {
                PacketModel packetModel = new PacketModel(modelFile, uuid);
                if (packetModel.success) {
                    modelMap.put(uuid, ModelInfo.fromFile(modelFile));
                    modelSelector.setModelForPlayer(profile, entry);

                    Packet send = formPacket(PacketModel.ID, packetModel);
                    if (broadcast)
                        playerEntity.getServerWorld().method_14178().sendToOtherNearbyPlayers(playerEntity, send);
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(receiver, send);
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            throw new LoadModelException(entry, e);
        }
    }

    public static void selectModel(EntityPlayerMP playerEntity, String model) throws LoadModelException {
        GameProfile profile = playerEntity.getGameProfile();
        UUID uuid = EntityPlayer.getUUID(profile);
        ModelInfo info = models.get(model);
        if (info == null)
            throw new ModelNotFoundException(model);
        File modelFile = new File(CustomModel.MODEL_DIR + "/" + info.fileName);

        try {
            if (modelFile.exists()) {
                PacketModel packetModel = new PacketModel(modelFile, uuid);
                if (packetModel.success) {
                    modelMap.put(uuid, ModelInfo.fromFile(modelFile));
                    modelSelector.setModelForPlayer(profile, model);

                    Packet send = formPacket(PacketModel.ID, packetModel);
                    playerEntity.getServerWorld().method_14178().sendToNearbyPlayers(playerEntity, send);
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            throw new LoadModelException(model, e);
        }
    }

    @Override
    public void onInitialize() {
        new File(MODEL_DIR).mkdirs();

        ServerCommand.register();

        ServerSidePacketRegistry.INSTANCE.register(PacketQuery.ID, (context, buffer) -> {
            PacketQuery packet = new PacketQuery();
            try {
                packet.read(buffer);
                context.getTaskQueue().execute(() -> {
                    try {
                        reloadModel(context.getPlayer(), packet.getPlayerUuid(), false);
                    } catch (LoadModelException e) {
                        if (context.getPlayer().allowsPermissionLevel(ModConfig.getListModelsPermission()))
                            context.getPlayer().sendMessage(new TranslatableText("error.custommodel.loadmodelpack", e.getFileName(), e.getMessage()));
                    }
                });
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        });

        ServerSidePacketRegistry.INSTANCE.register(PacketQueryConfig.ID, (context, buffer) -> {
            sendPacket(context.getPlayer(), PacketReplyConfig.ID, new PacketReplyConfig(ModConfig.getSettings().server));
        });

        ServerStartCallback.EVENT.register(minecraftServer -> server = minecraftServer);
        ServerStopCallback.EVENT.register(minecraftServer -> server = null);

        ServerTickCallback.EVENT.register(minecraftServer -> {
            if (clearCounter++ > 200) {
                clearCounter = 0;
                Set<UUID> uuids = Sets.newHashSet();
                for (ServerPlayerEntity playerEntity : minecraftServer.getPlayerManager().getPlayerList())
                    uuids.add(PlayerEntity.getUuidFromProfile(playerEntity.getGameProfile()));
                for (Iterator<Map.Entry<UUID, ModelInfo>> iter = modelMap.entrySet().iterator(); iter.hasNext();) {
                    if (!uuids.contains(iter.next().getKey()))
                        iter.remove();
                }
            }
        });

        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        refreshModelList();
    }
}
