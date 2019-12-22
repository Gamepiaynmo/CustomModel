package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ModMenu implements IModGuiFactory {

    public static class ConfigGuiScreen extends GuiConfig {
        public ConfigGuiScreen(GuiScreen parentScreen) {
            super(parentScreen, getConfigElements(), CustomModel.MODID, false, false, I18n.format("text.autoconfig.custommodel.title"));
        }

        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> configList = Lists.newArrayList();
            List<IConfigElement> clientList = Lists.newArrayList();
            List<IConfigElement> serverList = Lists.newArrayList();
            List<IConfigElement> permissionList = Lists.newArrayList();

            clientList.add(new DummyConfigElement("hideNearParticles", true, ConfigGuiType.BOOLEAN, "text.autoconfig.custommodel.option.client.hideNearParticles"));

            configList.add(new DummyConfigElement.DummyCategoryElement("client", "text.autoconfig.custommodel.category.client", clientList));

            serverList.add(new DummyConfigElement("customEyeHeight", true, ConfigGuiType.BOOLEAN, "text.autoconfig.custommodel.option.server.customEyeHeight"));
            serverList.add(new DummyConfigElement("customBoundingBox", true, ConfigGuiType.BOOLEAN, "text.autoconfig.custommodel.option.server.customBoundingBox"));
            serverList.add(new DummyConfigElement("defaultModel", "default", ConfigGuiType.STRING, "text.autoconfig.custommodel.option.server.defaultModel"));

            configList.add(new DummyConfigElement.DummyCategoryElement("server", "text.autoconfig.custommodel.category.server", serverList));

            permissionList.add(new DummyConfigElement("reloadSelfPermission", 0, ConfigGuiType.INTEGER, "text.autoconfig.custommodel.option.permission.reloadSelfPermission", 0, 4));
            permissionList.add(new DummyConfigElement("reloadOthersPermission", 1, ConfigGuiType.INTEGER, "text.autoconfig.custommodel.option.permission.reloadOthersPermission", 0, 4));
            permissionList.add(new DummyConfigElement("selectSelfPermission", 1, ConfigGuiType.INTEGER, "text.autoconfig.custommodel.option.permission.selectSelfPermission", 0, 4));
            permissionList.add(new DummyConfigElement("selectOthersPermission", 2, ConfigGuiType.INTEGER, "text.autoconfig.custommodel.option.permission.selectOthersPermission", 0, 4));
            permissionList.add(new DummyConfigElement("listModelsPermission", 1, ConfigGuiType.INTEGER, "text.autoconfig.custommodel.option.permission.listModelsPermission", 0, 4));

            configList.add(new DummyConfigElement.DummyCategoryElement("permission", "text.autoconfig.custommodel.category.permission", permissionList));
            return configList;
        }
    }

    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new ConfigGuiScreen(parentScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
}
