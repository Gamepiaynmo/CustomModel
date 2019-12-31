package com.github.gamepiaynmo.custommodel.entity;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import noppes.npcs.ModelDataShared;
import noppes.npcs.entity.EntityCustomNpc;

import java.util.List;

public class CustomModelNpc extends EntityCustomNpc {
    private String currentModel = null;
    private EntityCustomNpc parent;

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
            currentModel = model;
            CustomModel.selectModel(getParent(), model);
        }
    }

    public EntityCustomNpc getParent() {
        return parent == null ? parent = NpcHelper.getParent(this) : parent;
    }
}
