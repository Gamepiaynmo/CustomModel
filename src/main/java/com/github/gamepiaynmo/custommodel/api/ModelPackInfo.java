package com.github.gamepiaynmo.custommodel.api;

public class ModelPackInfo {
    /**
     * Id of the model, a string start with [a-z] and compose of [a-z0-9].
     */
    public final String modelId;

    /**
     * Name of the model, a non-empty unicode string.
     */
    public final String modelName;

    /**
     * Version of the model, a unicode string.
     */
    public final String version;

    /**
     * Author of the model, a unicode string.
     */
    public final String author;

    /**
     * Does this model come from one of the clients.
     */
    public final boolean fromClient;

    public ModelPackInfo(String id, String name, String ver, String auth, boolean client) {
        modelId = id;
        modelName = name;
        version = ver;
        author = auth;
        fromClient = client;
    }
}
