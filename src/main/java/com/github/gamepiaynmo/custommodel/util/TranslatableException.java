package com.github.gamepiaynmo.custommodel.util;

import net.minecraft.util.text.TextComponentTranslation;

public class TranslatableException extends RuntimeException {

    public TranslatableException() {
        super();
    }

    public TranslatableException(String message, Object... args) {
        super(new TextComponentTranslation(message, args).getUnformattedComponentText());
    }

    public TranslatableException(String message, Throwable cause, Object... args) {
        super(new TextComponentTranslation(message, args).getUnformattedComponentText(), cause);
    }

    public TranslatableException(Throwable cause) {
        super(cause);
    }
}
