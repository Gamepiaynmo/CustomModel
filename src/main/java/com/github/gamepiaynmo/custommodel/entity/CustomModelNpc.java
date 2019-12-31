package com.github.gamepiaynmo.custommodel.entity;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.mixin.PlayerStatureHandler;
import com.github.gamepiaynmo.custommodel.render.EntityDimensions;
import com.github.gamepiaynmo.custommodel.render.EntityPose;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.server.ModelInfo;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import noppes.npcs.ModelDataShared;
import noppes.npcs.entity.EntityCustomNpc;

import java.util.List;

public class CustomModelNpc extends EntityCustomNpc {
    private String currentModel = null;
    private EntityCustomNpc parent;
    private float eyeHeight;

    public CustomModelNpc(World world) {
        super(world);
    }

    public ResourceLocation getTextureSkin() {
        return new ResourceLocation(display.getSkinTexture());
    }

    public ResourceLocation getTextureCape() {
        return new ResourceLocation(display.getCapeTexture());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        currentModel = compound.getString("CustomModel");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setString("CustomModel", currentModel);
    }

    public String getCurrentModel() {
        return currentModel;
    }
    public void setCurrentModel(String model) {
        if (model != currentModel) {
            CustomModel.selectModel(getParent(), model);
            getParent().modelData.extra.setString("CustomModel", model);
            currentModel = model;
        }
    }

    @Override
    public void onUpdate() {
        EntityPose pose = PlayerStatureHandler.getPose(this);

        if (world.isRemote) {
            ModelPack pack = CustomModelClient.getModelForEntity(this);
            if (pack != null) {
                if (CustomModelClient.serverConfig.customEyeHeight) {
                    Float eyeHeight = pack.getModel().getModelInfo().eyeHeightMap.get(pose);
                    if (eyeHeight != null)
                        this.eyeHeight = eyeHeight;
                    else this.eyeHeight = 1.62f;
                }

                if (CustomModelClient.serverConfig.customBoundingBox) {
                    EntityDimensions dimensions = pack.getModel().getModelInfo().dimensionsMap.get(pose);
                    if (dimensions == null)
                        dimensions = PlayerStatureHandler.defaultDimensions.get(pose);
                    if (dimensions != null)
                        PlayerStatureHandler.setSize(this, dimensions);
                }
            }
        } else {
            ModelInfo pack = CustomModel.getBoundingBoxForEntity(this.getUniqueID());
            if (pack != null) {
                if (ModConfig.isCustomEyeHeight()) {
                    Float eyeHeight = pack.eyeHeightMap.get(pose);
                    if (eyeHeight != null)
                        this.eyeHeight = eyeHeight;
                    else this.eyeHeight = 1.62f;
                }

                if (ModConfig.isCustomBoundingBox()) {
                    EntityDimensions dimensions = pack.dimensionsMap.get(pose);
                    if (dimensions == null)
                        dimensions = PlayerStatureHandler.defaultDimensions.get(pose);
                    if (dimensions != null)
                        PlayerStatureHandler.setSize(this, dimensions);
                }
            }
        }
    }

    public EntityCustomNpc getParent() {
        return parent == null ? parent = NpcHelper.getParent(this) : parent;
    }
}
