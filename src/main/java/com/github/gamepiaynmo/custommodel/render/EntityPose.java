package com.github.gamepiaynmo.custommodel.render;

import com.google.common.collect.Maps;

import java.util.Map;

public enum EntityPose {
    STANDING(0, "standing"),
    FALL_FLYING(1, "fall_flying"),
    SLEEPING(2, "sleeping"),
    SWIMMING(3, "swimming"),
    SPIN_ATTACK(4, "spin_attack"),
    SNEAKING(5, "sneaking"),
    DYING(6, "dying");

    private static final Map<String, EntityPose> poseMap = Maps.newHashMap();

    private final int index;
    private final String name;

    private EntityPose(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public static EntityPose getById(int index) {
        return EntityPose.values()[index];
    }

    public int getId() {
        return index;
    }

    public String getName() {
        return name;
    }

    public static EntityPose getByName(String name) {
        return poseMap.get(name);
    }

    static {
        for (EntityPose pose : EntityPose.values())
            poseMap.put(pose.name, pose);
    }
}
