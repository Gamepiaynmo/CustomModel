package com.github.gamepiaynmo.custommodel.client.render;

import com.github.gamepiaynmo.custommodel.expression.*;
import com.google.gson.JsonElement;

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
        return (c) -> lastBool;
    }

    private IExpressionBool getCurBool() {
        return (c) -> curBool;
    }

    private IExpressionFloat getLastFloat() {
        return (c) -> lastFloat;
    }

    private IExpressionFloat getCurFloat() {
        return (c) -> curFloat;
    }

    private IExpressionFloat getParFloat() {
        return (c) -> lastFloat + c.currentParameter.partial * (curFloat - lastFloat);
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
