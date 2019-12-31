package com.github.gamepiaynmo.custommodel.entity;

import net.minecraft.world.World;

public class CustomModelFemaleNpc extends CustomModelNpc {
    public CustomModelFemaleNpc(World world) {
        super(world);
        this.display.setSkinTexture("customnpcs:textures/entity/alex_skins/alex_2.png");
    }
}
