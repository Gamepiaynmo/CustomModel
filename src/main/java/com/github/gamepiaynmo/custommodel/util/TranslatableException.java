package com.github.gamepiaynmo.custommodel.util;

import net.minecraft.text.TranslatableText;

public class TranslatableException extends RuntimeException {

    public TranslatableException() {
        super();
    }

    public TranslatableException(String message, Object... args) {
        super(new TranslatableText(message, args).asString());
    }

    public TranslatableException(String message, Throwable cause, Object... args) {
        super(new TranslatableText(message, args).asString(), cause);
    }

    public TranslatableException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
