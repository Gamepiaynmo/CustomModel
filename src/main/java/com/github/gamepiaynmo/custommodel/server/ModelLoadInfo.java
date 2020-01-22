package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.api.ModelPackInfo;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class ModelLoadInfo {
    public final ModelInfo info;
    public final ITextComponent text;
    public final boolean isClient;

    public int refCnt = 0;

    public ModelLoadInfo(ModelInfo info, boolean isClient) {
        this.info = info;
        this.text = getInfoText(info);
        this.isClient = isClient;
    }

    private ITextComponent getInfoText(ModelInfo info) {
        ITextComponent text = new TextComponentString(info.modelName);
        Style style = text.getStyle();

        ITextComponent hoverText = setColor(new TextComponentTranslation("text.custommodel.modelinfo.name", info.modelName), TextFormatting.GOLD);
        hoverText.appendText("\n").appendSibling(setColor(new TextComponentTranslation("text.custommodel.modelinfo.id", info.modelId), TextFormatting.YELLOW));
        if (info.version.length() > 0)
            hoverText.appendText("\n").appendSibling(setColor(new TextComponentTranslation("text.custommodel.modelinfo.version", info.version), TextFormatting.GREEN));
        if (info.author.length() > 0)
            hoverText.appendText("\n").appendSibling(setColor(new TextComponentTranslation("text.custommodel.modelinfo.author", info.author), TextFormatting.BLUE));

        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/" + CustomModel.MODID + " select " + info.modelId));
        return text;
    }

    private ITextComponent setColor(ITextComponent text, TextFormatting color) {
        text.getStyle().setColor(color);
        return text;
    }

    public ModelPackInfo getInfo() {
        return new ModelPackInfo(info.modelId, info.modelName, info.version, info.author, isClient);
    }
}
