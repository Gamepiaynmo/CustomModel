package com.github.gamepiaynmo.custommodel.render.model;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.expression.ExpressionParser;
import com.github.gamepiaynmo.custommodel.expression.IExpressionBool;
import com.github.gamepiaynmo.custommodel.expression.IExpressionFloat;
import com.github.gamepiaynmo.custommodel.expression.ParseException;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.RenderContext;
import com.github.gamepiaynmo.custommodel.util.Json;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Random;

public class ItemPart {
    private IExpressionFloat itemId;
    private IExpressionBool enchanted;

    private int lastId;
    private boolean lastEnchant;
    private ItemStack stack = new ItemStack(Items.AIR);

    TextureManager textureManager;
    public static final ResourceLocation ENCHANTMENT_GLINT_TEX = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    private ItemPart(IExpressionFloat item, IExpressionBool enchanted) {
        this.itemId = item;
        this.enchanted = enchanted;

        lastId = 0;
        lastEnchant = false;

        textureManager = Minecraft.getMinecraft().getTextureManager();
    }

    private void update() {
        stack = new ItemStack(Item.REGISTRY.getObjectById(lastId));
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

        Minecraft.getMinecraft().getItemRenderer().renderItem(context.currentEntity, stack, ItemCameraTransforms.TransformType.FIXED);
    }

    public static ItemPart fromJson(Bone bone, JsonObject jsonObj) throws ParseException {
        ExpressionParser parser = bone.model.getParser();
        IExpressionFloat id = Json.getFloatExpression(jsonObj, CustomJsonModel.ITEM_ID, 0, parser);
        IExpressionBool enchanted = Json.getBooleanExpression(jsonObj, CustomJsonModel.ENCHANTED, false, parser);
        return new ItemPart(id, enchanted);
    }
}
