package com.github.gamepiaynmo.custommodel.client.render.model;

import com.github.gamepiaynmo.custommodel.client.render.PlayerBone;
import com.github.gamepiaynmo.custommodel.client.render.RenderContext;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.github.gamepiaynmo.custommodel.util.Quaternion;
import com.github.gamepiaynmo.custommodel.util.Vec2d;
import com.github.gamepiaynmo.custommodel.util.Vector3;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public interface IBone {
    Vector3 getPosition(RenderContext context);
    Vector3 getRotation(RenderContext context);
    Vector3 getScale(RenderContext context);
    Vec2d getTextureSize(RenderContext context);
    Function<RenderContext, Identifier> getTexture(RenderContext context);
    IBone getParent();
    PlayerBone getPlayerBone();
    String getId();
    boolean isVisible(RenderContext context);

    default Quaternion getQuaternion(RenderContext context) {
        Vector3 rotation = getRotation(context);
        return new Quaternion().setEulerAnglesRad(rotation.x, rotation.y, 0).mulLeft(new Quaternion().setFromAxisRad(0, 0, 1, rotation.z));
    }
    default Matrix4 getTransform(RenderContext context) {
        return new Matrix4().setToScaling(getScale(context)).rotate(getQuaternion(context)).translate(getPosition(context).scl(0.0625));
    }
}
