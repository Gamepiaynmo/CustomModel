package com.github.gamepiaynmo.custommodel.client.render.model;

import com.github.gamepiaynmo.custommodel.expression.ExpressionParser;
import com.github.gamepiaynmo.custommodel.expression.IExpressionBool;
import com.github.gamepiaynmo.custommodel.expression.IExpressionFloat;
import com.github.gamepiaynmo.custommodel.expression.ParseException;
import com.github.gamepiaynmo.custommodel.client.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.client.render.RenderContext;
import com.github.gamepiaynmo.custommodel.util.Json;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemPart {
    private IExpressionFloat itemId;
    private IExpressionBool enchanted;

    private int lastId;
    private boolean lastEnchant;
    private ItemStack stack = new ItemStack(Items.AIR);

    TextureManager textureManager;
    public static final Identifier ENCHANTMENT_GLINT_TEX = new Identifier("textures/misc/enchanted_item_glint.png");

    private ItemPart(IExpressionFloat item, IExpressionBool enchanted) {
        this.itemId = item;
        this.enchanted = enchanted;

        lastId = 0;
        lastEnchant = false;

        textureManager = MinecraftClient.getInstance().getTextureManager();
    }

    private void update() {
        stack = new ItemStack(Registry.ITEM.get(lastId));
        if (lastEnchant)
            stack.addEnchantment(Enchantments.UNBREAKING, 0);
    }

    public void render(RenderContext context) {
        int curId = (int) itemId.eval(context);
        boolean curEnchant = enchanted.eval(context);
        if (curId != lastId || curEnchant != lastEnchant) {
            lastId = curId;
            lastEnchant = curEnchant;
            update();
        }

        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Type.FIXED);
    }

    public static ItemPart fromJson(Bone bone, JsonObject jsonObj) throws ParseException {
        ExpressionParser parser = bone.model.getParser();
        IExpressionFloat id = Json.getFloatExpression(jsonObj, CustomJsonModel.ITEM_ID, 0, parser);
        IExpressionBool enchanted = Json.getBooleanExpression(jsonObj, CustomJsonModel.ENCHANTED, false, parser);
        return new ItemPart(id, enchanted);
    }
}
