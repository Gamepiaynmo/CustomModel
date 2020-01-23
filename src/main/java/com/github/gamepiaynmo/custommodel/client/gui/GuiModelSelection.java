package com.github.gamepiaynmo.custommodel.client.gui;

import com.github.gamepiaynmo.custommodel.api.ModelPackInfo;
import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.server.ModelInfo;
import com.github.gamepiaynmo.custommodel.server.ModelLoadInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GuiModelSelection extends GuiScreen {
    private final List<ModelPackInfo> infoList = Lists.newArrayList();
    private final List<List<String>> infoStr = Lists.newArrayList();
    private final int serverModelCount;
    private final List<Integer> entries = Lists.newArrayList();
    private GuiTextField searchInput;

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
        infoList.addAll(serverInfo);
        Set<String> modelIds = Sets.newHashSet();
        for (ModelPackInfo info : serverInfo)
            modelIds.add(info.modelId);

        serverModelCount = modelIds.size();
        if (ModConfig.isSendModels() && CustomModelClient.serverConfig.receiveModels) {
            CustomModel.manager.refreshModelList();
            for (ModelLoadInfo info : CustomModel.manager.models.values())
                if (modelIds.add(info.info.modelId))
                    infoList.add(info.getInfo());
        }

        for (int i = 0; i < infoList.size(); i++) {
            ModelPackInfo info = infoList.get(i);
            List<String> str = Lists.newArrayListWithCapacity(5);
            str.add("§6" + I18n.format("text.custommodel.modelinfo.id", info.modelId));
            str.add("§e" + I18n.format("text.custommodel.modelinfo.name", info.modelName));
            if (!info.version.isEmpty())
                str.add("§a" + I18n.format("text.custommodel.modelinfo.version", info.version));
            if (!info.author.isEmpty())
                str.add("§9" + I18n.format("text.custommodel.modelinfo.author", info.author));
            if (i >= serverModelCount)
                str.add("§7" + I18n.format("text.custommodel.modelinfo.client"));
            else if (info.fromClient)
                str.add("§7" + I18n.format("text.custommodel.modelinfo.otherclient"));
            infoStr.add(str);
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void initGui() {
        left = width / 2 - 200;
        right = width / 2 - 40;
        top = height / 2 - 100;
        bottom = height / 2 + 90;

        entryWidth = right - left;
        entryHeight = bottom - top;
        itemCount = entryHeight / itemHeight;

        searchInput = new GuiTextField(0, mc.fontRenderer, left, bottom + 2, entryWidth + scrollWidth, itemHeight);
        searchInput.setMaxStringLength(256);
        searchInput.setFocused(true);
        searchInput.setCanLoseFocus(false);

        updateSearchEntry();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int dWheel = Mouse.getEventDWheel();

        if (dWheel != 0) {
            dWheel = dWheel < 0 ? 1 : -1;
            scrollPos = Math.max(0, Math.min(entries.size() - 1, scrollPos + dWheel));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.zLevel = -1000;
        drawDefaultBackground();

        if (selected >= 0) {
            ModelPackInfo info = infoList.get(entries.get(selected));
            int entityX = width / 2 + 100, entityY = height / 2, deltaY = 0;
            GuiInventory.drawEntityOnScreen(entityX, entityY + 60, 60, entityX - mouseX, entityY - mouseY, Minecraft.getMinecraft().player);
            for (String str : infoStr.get(entries.get(selected)))
                drawCenteredString(mc.fontRenderer, str, entityX, entityY + 60 + (deltaY += 10), 0xffffffff);
        }

        searchInput.drawTextBox();
        GlStateManager.disableDepth();
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawRect(left, top, right, bottom, 0xbf3f3f3f);

        drawRect(right, top, right + scrollWidth, bottom, 0xbf000000);
        int scrollTop = top + (entries.isEmpty() ? 0 : entryHeight * scrollPos / entries.size());
        drawRect(right, scrollTop, right + scrollWidth, scrollTop + scrollHeight, 0xffffffff);
        drawRect(right + scrollWidth * 2 / 3, scrollTop, right + scrollWidth, scrollTop + scrollHeight, 0xffbfbfbf);

        for (int i = Math.min(scrollPos + itemCount, entries.size()) - 1; i >= scrollPos; i--) {
            int index = entries.get(i);
            int itemTop = top + (i - scrollPos) * itemHeight;
            if (selected == i) {
                drawRect(left, itemTop, left + entryWidth, itemTop + itemHeight, 0xffffffff);
                drawRect(left + 1, itemTop + 1, left + entryWidth - 1, itemTop + itemHeight - 1, 0xff000000);
            }
            ModelPackInfo info = infoList.get(index);
            String str = mc.fontRenderer.trimStringToWidth(info.modelName, entryWidth - 7);
            if (!str.equals(info.modelName))
                str += "...";
            drawString(mc.fontRenderer, str, left + 1, itemTop + 1, index >= serverModelCount ?
                    0xffbfbfbf : info.fromClient ? 0xffdfdfdf : 0xffffffff);
        }

        if (mouseY >= top && mouseY < bottom && mouseX >= left && mouseX < right) {
            int index = (mouseY - top) / itemHeight + scrollPos;
            if (index < entries.size()) {
                ModelPackInfo info = infoList.get(entries.get(index));
                drawHoveringText(infoStr.get(entries.get(index)), mouseX, mouseY);
            }
        }

        GlStateManager.enableDepth();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        } else {
            searchInput.textboxKeyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchInput.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseY >= top && mouseY < bottom && mouseX >= left && mouseX < right) {
            int index = (mouseY - top) / itemHeight + scrollPos;
            if (index < entries.size())
                setSelected(index);
        }
    }

    private void setSelected(int index) {
        if (index >= 0 && index != selected) {
            sendChatMessage("/" + CustomModel.MODID + " select " + infoList.get(entries.get(index)).modelId);
        }

        selected = index;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void updateScreen() {
        searchInput.updateCursorCounter();
        if (!searchInput.getText().equals(searchText)) {
            searchText = searchInput.getText().toLowerCase();
            updateSearchEntry();
        }
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
