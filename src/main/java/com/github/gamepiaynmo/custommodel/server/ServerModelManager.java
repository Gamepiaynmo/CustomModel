package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.api.IModelSelector;
import com.github.gamepiaynmo.custommodel.entity.NpcHelper;
import com.github.gamepiaynmo.custommodel.network.NetworkHandler;
import com.github.gamepiaynmo.custommodel.network.PacketModel;
import com.github.gamepiaynmo.custommodel.network.PacketQuery;
import com.github.gamepiaynmo.custommodel.server.selector.DefaultModelSelector;
import com.github.gamepiaynmo.custommodel.util.LoadModelException;
import com.github.gamepiaynmo.custommodel.util.ModelNotFoundException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ServerModelManager {
    private final Map<UUID, ModelInfo> modelMap = Maps.newHashMap();
    private int clearCounter = 0;
    public final Map<String, ModelLoadInfo> models = Maps.newHashMap();

    private final IModelSelector defaultSelector = new DefaultModelSelector();
    private IModelSelector modelSelector = defaultSelector;

    public void setModelSelector(IModelSelector modelSelector) {
        modelSelector = modelSelector;
        if (modelSelector == null)
            modelSelector = defaultSelector;
    }

    public IModelSelector getModelSelector() {
        return modelSelector;
    }

    public ModelInfo getModelForPlayer(EntityPlayer playerEntity) {
        return modelMap.get(playerEntity.getUniqueID());
    }

    public ModelInfo getModelForEntity(UUID uuid) {
        return modelMap.get(uuid);
    }

    public void refreshModelList() {
        models.clear();
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            for (File file : new File(CustomModel.MODEL_DIR).listFiles()) {
                try {
                    ModelInfo info = ModelInfo.fromFile(file);
                    ModelLoadInfo old = models.get(info.modelId);
                    if (old == null || info.version.compareTo(old.info.version) > 0)
                        models.put(info.modelId, new ModelLoadInfo(info, false));
                } catch (Exception e) {
                    CustomModel.LOGGER.warn(e.getMessage(), e);
                }
            }
        }

        for (ModelInfo info : modelMap.values()) {
            models.putIfAbsent(info.modelId, new ModelLoadInfo(info, true));
            models.get(info.modelId).refCnt++;
        }
    }

    public void addModelInfo(ModelInfo info, EntityPlayerMP sender, UUID receiverUuid) {
        ModelLoadInfo old = models.get(info.modelId);
        if (ModConfig.isReceiveModels() && (old == null || sender.getUniqueID().equals(old.info.sender))) {
            ModelLoadInfo newInfo = new ModelLoadInfo(info, true);
            if (old != null)
                newInfo.refCnt = old.refCnt;
            models.put(info.modelId, newInfo);

            EntityPlayerMP receiver = CustomModel.server.getPlayerList().getPlayerByUUID(receiverUuid);
            if (receiver != null)
                selectModel(sender, receiver, info.modelId);
        }
    }

    private EntityLivingBase getEntityByUuid(EntityPlayerMP player, UUID uuid) {
        Entity result = CustomModel.server.getPlayerList().getPlayerByUUID(uuid);
        if (result == null && player != null)
            result = player.getServerWorld().getEntityFromUuid(uuid);
        return result instanceof EntityLivingBase ? (EntityLivingBase) result : null;
    }

    public Collection<ITextComponent> getServerModelInfoList() {
        return models.values().stream().filter(info -> !info.isClient).map(info -> info.text).collect(Collectors.toList());
    }

    public Collection<String> getServerModelIdList() {
        return models.values().stream().filter(info -> !info.isClient).map(info -> info.info.modelId).collect(Collectors.toList());
    }

    public Collection<String> getModelIdList() {
        return models.values().stream().map(info -> info.info.modelId).collect(Collectors.toList());
    }

    public void clearModel(EntityPlayerMP playerEntity) {
        GameProfile profile = playerEntity.getGameProfile();
        UUID uuid = playerEntity.getUniqueID();
        ModelInfo old = modelMap.remove(uuid);
        if (old != null)
            decRefCnt(old);
        modelSelector.clearModelForPlayer(profile);
        PacketModel packetModel = new PacketModel(uuid);
        NetworkHandler.CHANNEL.sendToAllTracking(packetModel, playerEntity);
        NetworkHandler.CHANNEL.sendTo(packetModel, playerEntity);
    }

    private void decRefCnt(ModelInfo modelInfo) {
        ModelLoadInfo info = models.get(modelInfo.modelId);
        info.refCnt--;
        if (info.refCnt < 0)
            CustomModel.LOGGER.warn(info.info.modelId + " reference count < 0");
        if (info.refCnt == 0 && info.isClient)
            models.remove(info.info.modelId);
    }

    private void setModel(UUID uuid, ModelLoadInfo info) {
        info.refCnt++;
        ModelInfo old = modelMap.put(uuid, info.info);
        if (old != null)
            decRefCnt(old);
    }

    public void onPlayerExit(EntityPlayerMP player) {
        ModelInfo info = modelMap.remove(player.getUniqueID());
        if (info != null)
            decRefCnt(info);
    }

    public void reloadModel(EntityPlayerMP receiver, boolean broadcast) throws LoadModelException {
        reloadModel(receiver, receiver.getUniqueID(), broadcast);
    }

    public void reloadModel(EntityPlayerMP receiver, UUID uuid, boolean broadcast) throws LoadModelException {
        Entity entity = getEntityByUuid(receiver, uuid);
        if (entity instanceof EntityLivingBase) {
            String entry = null;
            if (CustomModel.hasnpc)
                entry = NpcHelper.getModelFromEntity((EntityLivingBase) entity);
            EntityPlayerMP playerEntity = null;
            GameProfile profile = null;

            if (entity instanceof EntityPlayerMP) {
                playerEntity = (EntityPlayerMP) entity;
                profile = playerEntity.getGameProfile();
                uuid = playerEntity.getUniqueID();
                entry = modelSelector.getModelForPlayer(profile);
            }

            if (entry == null) return;
            ModelLoadInfo info = models.get(entry);
            if (info == null || receiver.getUniqueID().equals(info.info.sender)) {
                if (ModConfig.isReceiveModels()) {
                    NetworkHandler.CHANNEL.sendTo(new PacketQuery(uuid, entry), receiver);
                    return;
                }
                throw new ModelNotFoundException(entry);
            }

            try {
                PacketModel packetModel = info.info.getPacket(uuid);
                if (packetModel.success) {
                    setModel(uuid, info);
                    if (profile != null)
                        modelSelector.setModelForPlayer(profile, entry);

                    if (!broadcast)
                        NetworkHandler.CHANNEL.sendTo(packetModel, receiver);
                    else {
                        NetworkHandler.CHANNEL.sendToAllTracking(packetModel, entity);
                        if (playerEntity != null)
                            NetworkHandler.CHANNEL.sendTo(packetModel, playerEntity);
                    }
                }
            } catch (Exception e) {
                CustomModel.LOGGER.warn(e.getMessage(), e);
                throw new LoadModelException(entry, e);
            }
        }
    }

    public void selectModel(EntityLivingBase entity, String model) throws LoadModelException {
        selectModel(null, entity, model);
    }

    public void selectModel(EntityPlayerMP sender, EntityLivingBase entity, String model) throws LoadModelException {
        UUID uuid = entity.getUniqueID();
        ModelLoadInfo info = models.get(model);
        if (info == null && sender != null) {
            if (ModConfig.isReceiveModels()) {
                NetworkHandler.CHANNEL.sendTo(new PacketQuery(uuid, model), sender);
                return;
            }
            throw new ModelNotFoundException(model);
        }

        try {
            PacketModel packetModel = info.info.getPacket(uuid);
            if (packetModel.success) {
                setModel(uuid, info);
                NetworkHandler.CHANNEL.sendToAllTracking(packetModel, entity);
                if (entity instanceof EntityPlayerMP) {
                    EntityPlayerMP playerMP = (EntityPlayerMP) entity;
                    modelSelector.setModelForPlayer(playerMP.getGameProfile(), model);
                    NetworkHandler.CHANNEL.sendTo(packetModel, playerMP);
                }
            }
        } catch (Exception e) {
            CustomModel.LOGGER.warn(e.getMessage(), e);
            throw new LoadModelException(model, e);
        }
    }

    public void tick() {
        if (clearCounter++ > 1000) {
            clearCounter = 0;
            Set<UUID> uuids = Sets.newHashSet();
            for (EntityPlayerMP playerEntity : CustomModel.server.getPlayerList().getPlayers())
                uuids.add(playerEntity.getUniqueID());
            if (CustomModel.hasnpc)
                for (WorldServer worldServer : CustomModel.server.worlds)
                    uuids.addAll(NpcHelper.getNpcUUIDs(worldServer));
            for (Iterator<Map.Entry<UUID, ModelInfo>> iter = modelMap.entrySet().iterator(); iter.hasNext(); ) {
                if (!uuids.contains(iter.next().getKey()))
                    iter.remove();
            }
        }
    }
}
