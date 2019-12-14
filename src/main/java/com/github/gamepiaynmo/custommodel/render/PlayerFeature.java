package com.github.gamepiaynmo.custommodel.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public enum PlayerFeature {
    HELMET_HEAD("helmet_head", PlayerBone.HEAD),
    HELMET_HEAD_OVERLAY("helmet_head_overlay", PlayerBone.HEAD),
    CHESTPLATE_BODY("chestplate_body", PlayerBone.BODY),
    CHESTPLATE_LEFT_ARM("chestplate_left_arm", PlayerBone.LEFT_ARM),
    CHESTPLATE_RIGHT_ARM("chestplate_right_arm", PlayerBone.RIGHT_ARM),
    LEGGINGS_BODY("leggings_body", PlayerBone.BODY),
    LEGGINGS_LEFT_LEG("leggings_left_leg", PlayerBone.LEFT_LEG),
    LEGGINGS_RIGHT_LEG("leggings_right_leg", PlayerBone.RIGHT_LEG),
    BOOTS_LEFT_LEG("boots_left_leg", PlayerBone.LEFT_LEG),
    BOOTS_RIGHT_LEG("boots_right_leg", PlayerBone.RIGHT_LEG),

    HELD_ITEM_LEFT("held_item_left", PlayerBone.LEFT_ARM),
    HELD_ITEM_RIGHT("held_item_right", PlayerBone.RIGHT_ARM),

    CAPE("cape", PlayerBone.BODY),
    HEAD_WEARING("head_wearing", PlayerBone.HEAD),
    ELYTRA("elytra", PlayerBone.BODY),

    SHOULDER_PARROT_LEFT("shoulder_parrot_left", PlayerBone.BODY),
    SHOULDER_PARROT_RIGHT("shoulder_parrot_right", PlayerBone.BODY);

    private final String id;
    private final PlayerBone attached;
    private static final Map<String, PlayerFeature> id2Feature = Maps.newHashMap();
    private static final Map<String, List<PlayerFeature>> featureLists = Maps.newHashMap();

    PlayerFeature(String id, PlayerBone attached) {
        this.id = id;
        this.attached = attached;
    }

    public String getId() { return id; }

    public static PlayerFeature getById(String id) {
        return id2Feature.get(id);
    }

    public static Collection<PlayerFeature> getListById(String id) {
        PlayerFeature feature = getById(id);
        if (feature == null)
            return featureLists.get(id);
        return Lists.newArrayList(feature);
    }

    public PlayerBone getAttachedBone() { return attached; }

    static {
        for (PlayerFeature feature : PlayerFeature.values())
            id2Feature.put(feature.id, feature);
        featureLists.put("helmet_all", Lists.newArrayList(HELMET_HEAD, HELMET_HEAD_OVERLAY));
        featureLists.put("chestplate_all", Lists.newArrayList(CHESTPLATE_BODY, CHESTPLATE_LEFT_ARM, CHESTPLATE_RIGHT_ARM));
        featureLists.put("leggings_all", Lists.newArrayList(LEGGINGS_BODY, LEGGINGS_LEFT_LEG, LEGGINGS_RIGHT_LEG));
        featureLists.put("boots_all", Lists.newArrayList(BOOTS_LEFT_LEG, BOOTS_RIGHT_LEG));
        featureLists.put("armor_body_all", Lists.newArrayList(CHESTPLATE_BODY, LEGGINGS_BODY));
        featureLists.put("armor_arms_all", Lists.newArrayList(CHESTPLATE_LEFT_ARM, CHESTPLATE_RIGHT_ARM));
        featureLists.put("armor_left_leg_all", Lists.newArrayList(LEGGINGS_LEFT_LEG, BOOTS_LEFT_LEG));
        featureLists.put("armor_right_leg_all", Lists.newArrayList(LEGGINGS_RIGHT_LEG, BOOTS_RIGHT_LEG));
        featureLists.put("armor_legs_all", Lists.newArrayList(LEGGINGS_LEFT_LEG, LEGGINGS_RIGHT_LEG, BOOTS_LEFT_LEG, BOOTS_RIGHT_LEG));
        featureLists.put("armor_all", Lists.newArrayList(HELMET_HEAD, HELMET_HEAD_OVERLAY, CHESTPLATE_BODY, CHESTPLATE_LEFT_ARM,
                CHESTPLATE_RIGHT_ARM, LEGGINGS_BODY, LEGGINGS_LEFT_LEG, LEGGINGS_RIGHT_LEG, BOOTS_LEFT_LEG, BOOTS_RIGHT_LEG));

        featureLists.put("held_item_all", Lists.newArrayList(HELD_ITEM_LEFT, HELD_ITEM_RIGHT));

        featureLists.put("shoulder_parrot_all", Lists.newArrayList(SHOULDER_PARROT_LEFT, SHOULDER_PARROT_RIGHT));

        featureLists.put("feature_all", Lists.newArrayList(HELMET_HEAD, HELMET_HEAD_OVERLAY, CHESTPLATE_BODY, CHESTPLATE_LEFT_ARM,
                CHESTPLATE_RIGHT_ARM, LEGGINGS_LEFT_LEG, LEGGINGS_RIGHT_LEG, BOOTS_LEFT_LEG, BOOTS_RIGHT_LEG, HELD_ITEM_LEFT,
                HELD_ITEM_RIGHT, CAPE, HEAD_WEARING, ELYTRA, SHOULDER_PARROT_LEFT, SHOULDER_PARROT_RIGHT));
    }
}
