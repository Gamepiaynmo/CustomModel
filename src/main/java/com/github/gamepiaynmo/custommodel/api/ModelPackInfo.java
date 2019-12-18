package com.github.gamepiaynmo.custommodel.api;

public class ModelPackInfo {
    public final String fileName;

    public final String modelId;
    public final String modelName;
    public final String version;
    public final String author;

    public ModelPackInfo(String... params) {
        fileName = params[0];
        modelId = params[1];
        modelName = params[2];
        version = params[3];
        author = params[4];
    }
}
