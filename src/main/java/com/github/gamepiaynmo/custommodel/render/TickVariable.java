package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.expression.*;
import com.github.gamepiaynmo.custommodel.util.Json;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.MathHelper;

public class TickVariable {
    private boolean lastBool = false;
    private boolean curBool = false;
    private float lastFloat = 0;
    private float curFloat = 0;
    private final ExpressionType type;
    private IExpression expression;

    public TickVariable(ExpressionType type) {
        this.type = type;
    }

    public void setInitValue(JsonElement element) {
        if (type == ExpressionType.BOOL)
            lastBool = curBool = element.getAsBoolean();
        else lastFloat = curFloat = element.getAsFloat();
    }

    public void setExpression(IExpression expression) {
        if (type == ExpressionType.BOOL)
            this.expression = (IExpressionBool) expression;
        else this.expression = (IExpressionFloat) expression;
    }

    public void tick(RenderContext context) {
        if (type == ExpressionType.BOOL) {
            lastBool = curBool;
            curBool = ((IExpressionBool) expression).eval(context);
        } else {
            lastFloat = curFloat;
            curFloat = ((IExpressionFloat) expression).eval(context);
        }
    }

    private IExpressionBool getLastBool() {
        return (context) -> lastBool;
    }

    private IExpressionBool getCurBool() {
        return (context) -> curBool;
    }

    private IExpressionFloat getLastFloat() {
        return (context) -> lastFloat;
    }

    private IExpressionFloat getCurFloat() {
        return (context) -> curFloat;
    }

    private IExpressionFloat getParFloat() {
        return (context) -> MathHelper.lerp(context.currentParameter.partial, lastFloat, curFloat);
    }

    public IExpression getLastValue() {
        if (type == ExpressionType.BOOL)
            return getLastBool();
        else return getLastFloat();
    }

    public IExpression getCurValue() {
        if (type == ExpressionType.BOOL)
            return getCurBool();
        else return getCurFloat();
    }

    public IExpression getParValue() {
        if (type == ExpressionType.BOOL)
            return getCurBool();
        else return getParFloat();
    }

}
