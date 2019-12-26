package com.github.gamepiaynmo.custommodel.render.feature;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.PlayerFeature;
import com.github.gamepiaynmo.custommodel.render.RenderContext;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.TagHelper;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import java.util.UUID;

public class CustomHead<T extends AbstractClientPlayerEntity, M extends PlayerEntityModel<T>> extends HeadFeatureRenderer<T, M> {
    private final RenderContext context;
    public CustomHead(FeatureRendererContext featureRendererContext_1, RenderContext context) {
        super(featureRendererContext_1);
        this.context = context;
    }

    @Override
    public void render(T livingEntity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6, float float_7) {
        ModelPack pack = CustomModelClient.getModelForPlayer(livingEntity_1);
        CustomJsonModel model = pack == null ? null : pack.getModel();
        if (model == null) {
            super.method_17159(livingEntity_1, float_1, float_2, float_3, float_4, float_5, float_6, float_7);
            return;
        }

        ItemStack itemStack_1 = livingEntity_1.getEquippedStack(EquipmentSlot.HEAD);
        if (!itemStack_1.isEmpty()) {
            Item item_1 = itemStack_1.getItem();

            for (IBone bone : model.getFeatureAttached(PlayerFeature.HEAD_WEARING)) {
                if (!bone.isVisible(context)) continue;

                GlStateManager.pushMatrix();
                GL11.glMultMatrixd(context.currentInvTransform.val);
                GL11.glMultMatrixd(model.getTransform(bone).val);

                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                if (item_1 instanceof BlockItem && ((BlockItem) item_1).getBlock() instanceof AbstractSkullBlock) {
                    GlStateManager.scalef(1.1875F, -1.1875F, -1.1875F);

                    GameProfile gameProfile_1 = null;
                    if (itemStack_1.hasTag()) {
                        CompoundTag compoundTag_1 = itemStack_1.getTag();
                        if (compoundTag_1.containsKey("SkullOwner", 10)) {
                            gameProfile_1 = TagHelper.deserializeProfile(compoundTag_1.getCompound("SkullOwner"));
                        } else if (compoundTag_1.containsKey("SkullOwner", 8)) {
                            String string_1 = compoundTag_1.getString("SkullOwner");
                            if (!StringUtils.isBlank(string_1)) {
                                gameProfile_1 = SkullBlockEntity.loadProperties(new GameProfile((UUID) null, string_1));
                                compoundTag_1.put("SkullOwner", TagHelper.serializeProfile(new CompoundTag(), gameProfile_1));
                            }
                        }
                    }

                    SkullBlockEntityRenderer.INSTANCE.render(-0.5F, 0.0F, -0.5F, (Direction) null, 180.0F, ((AbstractSkullBlock) ((BlockItem) item_1).getBlock()).getSkullType(), gameProfile_1, -1, float_1);
                } else if (!(item_1 instanceof ArmorItem) || ((ArmorItem) item_1).getSlotType() != EquipmentSlot.HEAD) {
                    GlStateManager.translatef(0.0F, -0.25F, 0.0F);
                    GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.scalef(0.625F, -0.625F, -0.625F);

                    MinecraftClient.getInstance().getFirstPersonRenderer().renderItem(livingEntity_1, itemStack_1, ModelTransformation.Type.HEAD);
                }

                GlStateManager.popMatrix();
            }
        }
    }
}
