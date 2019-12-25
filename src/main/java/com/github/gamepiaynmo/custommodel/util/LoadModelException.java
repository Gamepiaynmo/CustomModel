package com.github.gamepiaynmo.custommodel.util;

import net.minecraft.text.TranslatableText;

public class LoadModelException extends RuntimeException {
    private String fileName;
    public LoadModelException(String fileName, Exception cause) {
        super(cause);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return new TranslatableText("error.custommodel.loadmodelpack.notfound", fileName).asString();
    }
}
