package com.github.gamepiaynmo.custommodel.client.gui;

import com.github.gamepiaynmo.custommodel.api.ModelPackInfo;
import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.server.ModelLoadInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GuiModelSelection extends Screen {
    private final List<ModelPackInfo> infoList = Lists.newArrayList();
    private final List<List<String>> infoStr = Lists.newArrayList();
    private final int serverModelCount;
    private final List<Integer> entries = Lists.newArrayList();
    private TextFieldWidget searchInput;

    private int scrollPos = 0;
    private int selected = -1;
    private String searchText = "";
    private int scrollHeight, itemCount;

    private int left, right, top, bottom;
    private int entryWidth, entryHeight;
    private int itemHeight = 10, scrollWidth = 9;

    public GuiModelSelection() {
        this(Collections.emptyList());
    }

    public GuiModelSelection(List<ModelPackInfo> serverInfo) {
        super(NarratorManager.EMPTY);

        infoList.addAll(serverInfo);
        Set<String> modelIds = Sets.newHashSet();
        for (ModelPackInfo info : serverInfo)
            modelIds.add(info.modelId);

        serverModelCount = modelIds.size();
        if (!CustomModelClient.isServerModded() || (ModConfig.isSendModels() && CustomModelClient.serverConfig.receiveModels)) {
            CustomModel.manager.refreshModelList();
            for (ModelLoadInfo info : CustomModel.manager.models.values())
                if (modelIds.add(info.info.modelId))
                    infoList.add(info.getInfo());
        }

        for (int i = 0; i < infoList.size(); i++) {
            ModelPackInfo info = infoList.get(i);
            List<String> str = Lists.newArrayListWithCapacity(5);
            str.add("§6" + I18n.translate("text.custommodel.modelinfo.id", info.modelId));
            str.add("§e" + I18n.translate("text.custommodel.modelinfo.name", info.modelName));
            if (!info.version.isEmpty())
                str.add("§a" + I18n.translate("text.custommodel.modelinfo.version", info.version));
            if (!info.author.isEmpty())
                str.add("§9" + I18n.translate("text.custommodel.modelinfo.author", info.author));
            if (i >= serverModelCount)
                str.add("§7" + I18n.translate("text.custommodel.modelinfo.client"));
            else if (info.fromClient)
                str.add("§7" + I18n.translate("text.custommodel.modelinfo.otherclient"));
            infoStr.add(str);
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public void init() {
        left = width / 2 - 200;
        right = width / 2 - 40;
        top = height / 2 - 100;
        bottom = height / 2 + 90;

        entryWidth = right - left;
        entryHeight = bottom - top;
        itemCount = entryHeight / itemHeight;

        searchInput = new TextFieldWidget(minecraft.textRenderer, left, bottom + 2, entryWidth + scrollWidth, itemHeight, "");
        searchInput.setMaxLength(256);

        updateSearchEntry();
    }

    @Override
    public boolean mouseScrolled(double x, double y, double wheel) {
        if (wheel != 0) {
            int dWheel = wheel < 0 ? 1 : -1;
            scrollPos = Math.max(0, Math.min(entries.size() - 1, scrollPos + dWheel));
        }

        return super.mouseScrolled(x, y, wheel);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.blitOffset = -1000;
        renderBackground();

        if (selected >= 0) {
            ModelPackInfo info = infoList.get(entries.get(selected));
            int entityX = width / 2 + 100, entityY = height / 2, deltaY = 0;
            InventoryScreen.drawEntity(entityX, entityY + 60, 60, entityX - mouseX, entityY - mouseY, minecraft.player);
            for (String str : infoStr.get(entries.get(selected)))
                drawCenteredString(minecraft.textRenderer, str, entityX, entityY + 60 + (deltaY += 10), 0xffffffff);
        }

        searchInput.render(mouseX, mouseY, partialTicks);
        GlStateManager.disableDepthTest();
        super.render(mouseX, mouseY, partialTicks);
        fill(left, top, right, bottom, 0xbf3f3f3f);

        fill(right, top, right + scrollWidth, bottom, 0xbf000000);
        int scrollTop = top + (entries.isEmpty() ? 0 : entryHeight * scrollPos / entries.size());
        fill(right, scrollTop, right + scrollWidth, scrollTop + scrollHeight, 0xffffffff);
        fill(right + scrollWidth * 2 / 3, scrollTop, right + scrollWidth, scrollTop + scrollHeight, 0xffbfbfbf);

        for (int i = Math.min(scrollPos + itemCount, entries.size()) - 1; i >= scrollPos; i--) {
            int index = entries.get(i);
            int itemTop = top + (i - scrollPos) * itemHeight;
            if (selected == i) {
                fill(left, itemTop, left + entryWidth, itemTop + itemHeight, 0xffffffff);
                fill(left + 1, itemTop + 1, left + entryWidth - 1, itemTop + itemHeight - 1, 0xff000000);
            }
            ModelPackInfo info = infoList.get(index);
            String str = minecraft.textRenderer.trimToWidth(info.modelName, entryWidth - 7);
            if (!str.equals(info.modelName))
                str += "...";
            drawString( minecraft.textRenderer, str, left + 1, itemTop + 1, index >= serverModelCount ?
                    0xffbfbfbf : info.fromClient ? 0xffdfdfdf : 0xffffffff);
        }

        if (mouseY >= top && mouseY < bottom && mouseX >= left && mouseX < right) {
            int index = (mouseY - top) / itemHeight + scrollPos;
            if (index < entries.size()) {
                ModelPackInfo info = infoList.get(entries.get(index));
                renderTooltip(infoStr.get(entries.get(index)), mouseX, mouseY);
            }
        }

        GlStateManager.enableDepthTest();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        searchInput.keyPressed(i, j, k);
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        searchInput.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseY >= top && mouseY < bottom && mouseX >= left && mouseX < right) {
            int index = (int) ((mouseY - top) / itemHeight + scrollPos);
            if (index < entries.size())
                setSelected(index);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void setSelected(int index) {
        if (index >= 0 && index != selected) {
            String modelId = infoList.get(entries.get(index)).modelId;
            if (CustomModelClient.isServerModded())
                sendMessage("/" + CustomModel.MODID + " select " + modelId);
            else CustomModelClient.manager.selectModel(MinecraftClient.getInstance().player.getGameProfile(), modelId);
        }

        selected = index;
    }

    @Override
    public void tick() {
        searchInput.tick();
        if (!searchInput.getText().equals(searchText)) {
            searchText = searchInput.getText().toLowerCase();
            updateSearchEntry();
        }

        super.tick();
    }

    private void updateSearchEntry() {
        entries.clear();
        for (int i = 0; i < infoList.size(); i++) {
            ModelPackInfo info = infoList.get(i);
            if (info.modelName.toLowerCase().indexOf(searchText) >= 0 || info.modelId.toLowerCase().indexOf(searchText) >= 0
                    || info.version.toLowerCase().indexOf(searchText) >= 0 || info.author.toLowerCase().indexOf(searchText) >= 0)
                entries.add(i);
        }

        scrollHeight = entries.isEmpty() ? entryHeight : Math.max(1, entryHeight / entries.size());
        scrollPos = Math.min(scrollPos, Math.max(0, entries.size() - 1));
        setSelected(-1);
    }
}
