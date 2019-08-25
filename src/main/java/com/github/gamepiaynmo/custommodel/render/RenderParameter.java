package com.github.gamepiaynmo.custommodel.render;

public class RenderParameter {
    public float limbSwing;
    public float limbSwingAmount;
    public float age;
    public float headYaw;
    public float headPitch;
    public float scale;
    public float partial;
    public RenderParameter(float... args) {
        if (args.length == 7) {
            limbSwing = args[0];
            limbSwingAmount = args[1];
            age = args[2];
            headYaw = args[3];
            headPitch = args[4];
            scale = args[5];
            partial = args[6];
        }
    }
}
