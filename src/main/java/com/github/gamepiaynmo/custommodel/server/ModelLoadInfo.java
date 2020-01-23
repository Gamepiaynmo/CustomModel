package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.api.ModelPackInfo;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class ModelLoadInfo {
    public final ModelInfo info;
    public final Text text;
    public final boolean isClient;

    public int refCnt = 0;

    public ModelLoadInfo(ModelInfo info, boolean isClient) {
        this.info = info;
        this.text = getInfoText(info);
        this.isClient = isClient;
    }

    private Text getInfoText(ModelInfo info) {
        Text text = new LiteralText(info.modelName);
        Style style = text.getStyle();

        Text hoverText = new TranslatableText("text.custommodel.modelinfo.name", info.modelName).formatted(Formatting.GOLD);
        hoverText.append("\n").append(new TranslatableText("text.custommodel.modelinfo.id", info.modelId).formatted(Formatting.YELLOW));
        if (info.version.length() > 0)
            hoverText.append("\n").append(new TranslatableText("text.custommodel.modelinfo.version", info.version).formatted(Formatting.GREEN));
        if (info.author.length() > 0)
            hoverText.append("\n").append(new TranslatableText("text.custommodel.modelinfo.author", info.author).formatted(Formatting.BLUE));

        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/" + CustomModel.MODID + " select " + info.modelId));
        return text;
    }

    public ModelPackInfo getInfo() {
        return new ModelPackInfo(info.modelId, info.modelName, info.version, info.author, isClient);
    }
}
