package com.github.gamepiaynmo.custommodel.entity;

import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import noppes.npcs.ModelDataShared;
import noppes.npcs.entity.EntityCustomNpc;

import java.util.List;
import java.util.UUID;

public class NpcHelper {
    public static CustomModelNpc getCustomModelEntity(EntityLivingBase entity) {
        if (entity instanceof EntityCustomNpc) {
            EntityCustomNpc npc = (EntityCustomNpc) entity;
            if (npc.modelData.entityClass != null && CustomModelNpc.class.isAssignableFrom(npc.modelData.entityClass)) {
                return (CustomModelNpc) npc.modelData.getEntity(npc);
            }
        }

        return null;
    }

    public static String getModelFromEntity(EntityLivingBase entity) {
        CustomModelNpc npc = getCustomModelEntity(entity);
        if (npc != null)
            return npc.getCurrentModel();
        return ModConfig.getDefaultModel();
    }

    public static EntityCustomNpc getParent(CustomModelNpc entity) {
        return entity.world.getEntities(EntityCustomNpc.class, npc -> getCustomModelEntity(npc) == entity).get(0);
    }

    public static EntityLivingBase getParent(EntityLivingBase entity) {
        return entity instanceof CustomModelNpc ? ((CustomModelNpc) entity).getParent() : null;
    }

    public static List<EntityLivingBase> getNpcEntities(World world) {
        return world.getEntities(EntityCustomNpc.class, entity -> true);
    }

    public static List<EntityLivingBase> getCustomModelNpcs(World world) {
        List<EntityLivingBase> res = Lists.newArrayList();
        world.getEntities(EntityCustomNpc.class, entity -> true).forEach(entity -> {
            CustomModelNpc npc = getCustomModelEntity(entity);
            if (npc != null) res.add(npc);
        });
        return res;
    }

    public static List<UUID> getNpcUUIDs(World world) {
        List<UUID> res = Lists.newArrayList();
        getNpcEntities(world).forEach(entity -> res.add(entity.getUniqueID()));
        return res;
    }

    public static void updateCustomModelNpcs(World world) {
        getNpcEntities(world).forEach(entity -> {
            CustomModelNpc npc = getCustomModelEntity(entity);
            if (npc != null)
                npc.onUpdate();
        });
    }

    public static EntityLivingBase getNearestNpc(World world, Entity entity) {
        EntityLivingBase res = null;
        double dis = 25;
        for (EntityLivingBase npc : getNpcEntities(world)) {
            if (getCustomModelEntity(npc) != null) {
                double sq = npc.getDistanceSq(entity);
                if (sq < dis) {
                    dis = sq;
                    res = npc;
                }
            }
        }

        return res;
    }

    public static void setCurrentModel(EntityLivingBase entity, String model) {
        CustomModelNpc npc = getCustomModelEntity(entity);
        if (npc != null)
            npc.setCurrentModel(model);
    }

    public static int getSlot(EntityLivingBase entity, EntityEquipmentSlot slot) {
        if (entity instanceof EntityCustomNpc) {
            EntityCustomNpc npc = (EntityCustomNpc) entity;
            switch (slot) {
                case MAINHAND: return Item.REGISTRY.getIDForObject(npc.inventory.getRightHand().getMCItemStack().getItem());
                case OFFHAND: return Item.REGISTRY.getIDForObject(npc.inventory.getLeftHand().getMCItemStack().getItem());
                case HEAD: return Item.REGISTRY.getIDForObject(npc.inventory.getArmor(0).getMCItemStack().getItem());
                case CHEST: return Item.REGISTRY.getIDForObject(npc.inventory.getArmor(1).getMCItemStack().getItem());
                case LEGS: return Item.REGISTRY.getIDForObject(npc.inventory.getArmor(2).getMCItemStack().getItem());
                case FEET: return Item.REGISTRY.getIDForObject(npc.inventory.getArmor(3).getMCItemStack().getItem());
            }
        }
        return 0;
    }

    public static EntityLivingBase getNpcByUUID(World world, UUID uuid) {
        if (world instanceof WorldServer) {
            Entity entity = ((WorldServer) world).getEntityFromUuid(uuid);
            if (entity instanceof EntityCustomNpc)
                return getCustomModelEntity((EntityLivingBase) entity);
        } else {
            for (EntityLivingBase npc : getNpcEntities(world)) {
                if (npc.getUniqueID().equals(uuid))
                    return getCustomModelEntity(npc);
            }
        }

        return null;
    }
}
