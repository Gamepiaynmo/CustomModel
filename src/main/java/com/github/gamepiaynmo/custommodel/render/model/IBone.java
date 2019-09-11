package com.github.gamepiaynmo.custommodel.render.model;

import com.github.gamepiaynmo.custommodel.render.PlayerBones;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.github.gamepiaynmo.custommodel.util.Quaternion;
import com.github.gamepiaynmo.custommodel.util.Vec2d;
import com.github.gamepiaynmo.custommodel.util.Vector3;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public interface IBone {
    Vector3 getPosition();
    Vector3 getRotation();
    Vector3 getScale();
    Vec2d getTextureSize();
    Supplier<Identifier> getTexture();
    IBone getParent();
    PlayerBones getPlayerBone();
    String getId();
    boolean isVisible();

    default Quaternion getQuaternion() {
        Vector3 rotation = getRotation();
        return new Quaternion().setEulerAnglesRad(rotation.x, rotation.y, rotation.z);
    }
    default Matrix4 getTransform() {
        return new Matrix4().setToScaling(getScale()).rotate(getQuaternion()).translate(getPosition().scl(0.0625));
    }
}
