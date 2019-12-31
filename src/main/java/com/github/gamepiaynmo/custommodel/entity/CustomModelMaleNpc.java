package com.github.gamepiaynmo.custommodel.entity;

import net.minecraft.world.World;

public class CustomModelMaleNpc extends CustomModelNpc {
    public CustomModelMaleNpc(World world) {
        super(world);
        this.display.setSkinTexture("textures/entity/steve.png");
    }
}
