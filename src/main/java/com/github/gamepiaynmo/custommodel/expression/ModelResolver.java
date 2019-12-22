package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.TickVariable;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class ModelResolver implements IExpressionResolver {
   final ModelPack pack;
   final CustomJsonModel model;

   public ModelResolver(ModelPack pack, CustomJsonModel model) {
      this.pack = pack;
      this.model = model;
   }

   public IExpression getParameter(String name) {
      RenderEntityParameterBool parBool = RenderEntityParameterBool.parse(name);
      if (parBool != null) {
         return parBool;
      } else {
         RenderEntityParameterFloat parFloat = RenderEntityParameterFloat.parse(name);
         return parFloat;
      }
   }

   public IExpression getExpression(String name) {
      IExpression mv = this.getModelVariable(name);
      if (mv != null) {
         return mv;
      } else {
         IExpression param = getParameter(name);
         return param;
      }
   }

   public IBone getModelRenderer(String name) {
      return model.getBone(name);
   }

   public static String[] tokenize(String str, String delim) {
      return str.split(delim);
   }

   public IExpression getModelVariable(String name) {
      String[] parts = tokenize(name, "\\.");
      if (parts.length != 2)
         return null;

      String first = parts[0];
      String second = parts[1];

      switch (first) {
         case "tex": {
            return pack.getTexture(second);
         }
         case "inv": {
            InventoryPlayer inventory = CustomModelClient.currentPlayer.inventory;
            IExpressionFloat result = null;
            switch (second) {
               case "mainhand": result = () ->
                       Item.REGISTRY.getIDForObject(inventory.getCurrentItem().getItem()); break;
               case "offhand": result = () ->
                       Item.REGISTRY.getIDForObject(inventory.offHandInventory.get(0).getItem()); break;
               case "helmet": result = () ->
                       Item.REGISTRY.getIDForObject(inventory.armorItemInSlot(3).getItem()); break;
               case "chestplate": result = () ->
                       Item.REGISTRY.getIDForObject(inventory.armorItemInSlot(2).getItem()); break;
               case "leggings": result = () ->
                       Item.REGISTRY.getIDForObject(inventory.armorItemInSlot(1).getItem()); break;
               case "boots": result = () ->
                       Item.REGISTRY.getIDForObject(inventory.armorItemInSlot(0).getItem()); break;
               default:
                  try {
                     if (second.startsWith("main")) {
                        int index = Integer.parseInt(second.substring(4));
                        if (index >= 0 && index < inventory.mainInventory.size())
                           result = () -> Item.REGISTRY.getIDForObject(inventory.mainInventory.get(index).getItem());
                     }
                  } catch (NumberFormatException e) {
                  }
            }

            return result;
         }
         case "item": {
            int rawid = Item.REGISTRY.getIDForObject(Item.REGISTRY.getObject(new ResourceLocation(second)));
            if (rawid < 0) return null;
            return new ConstantFloat(rawid);
         }
         case "var": {
            return model.getVariable(second);
         }
         case "tvl": {
            TickVariable var = model.getTickVar(second);
            return var == null ? null : var.getLastValue();
         }
         case "tvc": {
            TickVariable var = model.getTickVar(second);
            return var == null ? null : var.getCurValue();
         }
         case "tvp": {
            TickVariable var = model.getTickVar(second);
            return var == null ? null : var.getParValue();
         }
         default: {
            IBone mr = this.getModelRenderer(first);
            if (mr == null) {
               return null;
            } else {
               ModelVariableType varType = ModelVariableType.parse(second);
               return varType == null ? null : new ModelVariableFloat(name, mr, varType);
            }
         }
      }
   }
}
