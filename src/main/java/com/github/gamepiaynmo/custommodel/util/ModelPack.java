package com.github.gamepiaynmo.custommodel.util;

import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.CustomTexture;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModelPack {

    public static final TextureGetter skinGetter = player -> player.getSkinTexture();

    private JsonObject modelJson;
    private Map<String, Identifier> textureIds = Maps.newHashMap();
    private List<CustomTexture> textures = Lists.newArrayList();
    private CustomJsonModel model;
    private boolean success = false;

    private ModelPack() {}

    public static ModelPack fromDirectory(TextureManager textureManager, File dir) throws FileNotFoundException, IOException {
        File modelFile = null;
        List<File> textureFiles = Lists.newArrayList();
        for (File modelPackItem : dir.listFiles()) {
            if (modelPackItem.getName().equals("model.json"))
                modelFile = modelPackItem;
            else if (modelPackItem.getName().endsWith(".png"))
                textureFiles.add(modelPackItem);
        }

        ModelPack pack = new ModelPack();
        InputStream modelInputStream = new FileInputStream(modelFile);
        pack.modelJson = new JsonParser().parse(new InputStreamReader(modelInputStream)).getAsJsonObject();
        IOUtils.closeQuietly(modelInputStream);
        for (File texture : textureFiles) {
            Identifier identifier = new Identifier(("custommodel/" + dir.getName() + "/" + texture.getName()).toLowerCase());
            pack.textureIds.put(texture.getName(), identifier);
            NativeImage image = NativeImage.read(new FileInputStream(texture));
            CustomTexture customTexture = new CustomTexture(image);
            pack.textures.add(customTexture);
            textureManager.registerTexture(identifier, customTexture);
        }

        pack.model = CustomJsonModel.fromJson(pack, pack.modelJson);
        pack.success = true;
        return pack;
    }

    public JsonObject getModelJson() {
        return modelJson;
    }

    public TextureGetter getBaseTexture() {
        return skinGetter;
    }

    public TextureGetter getTexture(String name) {
        if (name.equals("skin.png"))
            return skinGetter;
        Identifier texture = textureIds.get(name);
        if (texture != null)
            return player -> texture;
        return null;
    }

    public boolean successfulLoaded() {
        return success;
    }

    public CustomJsonModel getModel() {
        return model;
    }

    public void release() {
        for (CustomTexture texture : textures)
            texture.clearGlId();
    }

    public static interface TextureGetter {
        public Identifier getTexture(AbstractClientPlayerEntity player);
    }
}
