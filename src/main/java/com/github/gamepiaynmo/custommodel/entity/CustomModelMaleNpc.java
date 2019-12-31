package com.github.gamepiaynmo.custommodel.entity;

import net.minecraft.world.World;

public class CustomModelMaleNpc extends CustomModelNpc {
    public CustomModelMaleNpc(World world) {
        super(world);
        this.display.setSkinTexture("customnpcs:textures/entity/humanmale/steve.png");
    }
}
