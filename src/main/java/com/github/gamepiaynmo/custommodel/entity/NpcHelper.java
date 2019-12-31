package com.github.gamepiaynmo.custommodel.entity;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
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
                return ObfuscationReflectionHelper.getPrivateValue(ModelDataShared.class, npc.modelData, "entity");
            }
        }

        return null;
    }

    public static String getModelFromEntity(EntityLivingBase entity) {
        CustomModelNpc npc = getCustomModelEntity(entity);
        if (npc != null)
            return npc.getCurrentModel();
        return null;
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
            double sq = npc.getDistanceSq(entity);
            if (sq < dis) {
                dis = sq;
                res = npc;
            }
        }

        return res;
    }

    public static void setCurrentModel(EntityLivingBase entity, String model) {
        CustomModelNpc npc = getCustomModelEntity(entity);
        if (npc != null)
            npc.setCurrentModel(model);
    }
}
