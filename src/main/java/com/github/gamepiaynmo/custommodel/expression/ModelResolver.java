package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.TickVariable;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.google.common.collect.Maps;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.potion.Potion;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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

   private IExpressionFloat toFloat(IExpressionFloat expr) {
      return expr;
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
            IExpressionFloat result = null;
            switch (second) {
               case "mainhand": result = (context) ->
                       !context.isPlayer() ? 0:
                       Registry.ITEM.getRawId(context.getPlayer().inventory.getMainHandStack().getItem()); break;
               case "offhand": result = (context) ->
                       !context.isPlayer() ? 0:
                       Registry.ITEM.getRawId(context.getPlayer().inventory.offHand.get(0).getItem()); break;
               case "helmet": result = (context) ->
                       !context.isPlayer() ? 0:
                       Registry.ITEM.getRawId(context.getPlayer().inventory.getArmorStack(3).getItem()); break;
               case "chestplate": result = (context) ->
                       !context.isPlayer() ? 0:
                       Registry.ITEM.getRawId(context.getPlayer().inventory.getArmorStack(2).getItem()); break;
               case "leggings": result = (context) ->
                       !context.isPlayer() ? 0:
                       Registry.ITEM.getRawId(context.getPlayer().inventory.getArmorStack(1).getItem()); break;
               case "boots": result = (context) ->
                       !context.isPlayer() ? 0:
                       Registry.ITEM.getRawId(context.getPlayer().inventory.getArmorStack(0).getItem()); break;
               default:
                  try {
                     if (second.startsWith("main")) {
                        int index = Integer.parseInt(second.substring(4));
                        if (index >= 0 && index < 36)
                           result = (context) -> !context.isPlayer() ? 0:
                                   Registry.ITEM.getRawId(context.getPlayer().inventory.main.get(index).getItem());
                     }
                  } catch (NumberFormatException e) {
                  }
            }

            return result;
         }
         case "item": {
            int rawid = Registry.ITEM.getRawId(Registry.ITEM.get(new Identifier(second)));
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
         case "pose": {
            EntityPose pose = CustomJsonModel.poseMap.get(second);
            return pose == null ? null : new ConstantFloat(poseId.get(pose));
         }
         case "effect": {
            StatusEffect potion = Registry.STATUS_EFFECT.get(new Identifier(second));
            return potion == null ? null : toFloat(context -> {
               StatusEffectInstance effect = context.currentEntity.getStatusEffect(potion);
               return effect == null ? -1 : effect.getAmplifier();
            });
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

   public static final Map<EntityPose, Integer> poseId = Maps.newEnumMap(EntityPose.class);
   static {
      poseId.put(EntityPose.STANDING, 0);
      poseId.put(EntityPose.FALL_FLYING, 1);
      poseId.put(EntityPose.SLEEPING, 2);
      poseId.put(EntityPose.SWIMMING, 3);
      poseId.put(EntityPose.SPIN_ATTACK, 4);
      poseId.put(EntityPose.SNEAKING, 5);
      poseId.put(EntityPose.DYING, 6);
   }
}
