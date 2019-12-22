package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.api.ModelPackInfo;
import com.github.gamepiaynmo.custommodel.expression.ParseException;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.EntityDimensions;
import com.github.gamepiaynmo.custommodel.render.EntityPose;
import com.github.gamepiaynmo.custommodel.util.Json;
import com.github.gamepiaynmo.custommodel.util.TranslatableException;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ModelInfo {

    public static ModelInfo fromFile(File file) throws IOException, ParseException {
        if (file.isDirectory())
            return fromFolder(file);
        else return fromZipFile(file);
    }

    private static ModelInfo fromZipFile(File file) throws IOException, ParseException {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
        ZipFile zf = new ZipFile(file);
        ZipEntry entry;

        while ((entry = zip.getNextEntry()) != null) {
            if (entry.isDirectory())
                continue;
            if (entry.getName().equals("model.json"))
                break;
        }

        if (entry == null)
            throw new TranslatableException("error.custommodel.loadmodelpack.nomodel");
        InputStream modelInputStream = zf.getInputStream(entry);
        JsonObject jsonObj = new JsonParser().parse(new InputStreamReader(modelInputStream)).getAsJsonObject();
        IOUtils.closeQuietly(modelInputStream);
        ModelInfo info = fromJson(jsonObj);
        info.fileName = file.getName();
        return info;
    }

    private static ModelInfo fromFolder(File file) throws IOException, ParseException {
        File model = null;
        for (File f : file.listFiles()) {
            if (f.getName().equals("model.json")) {
                model = f;
                break;
            }
        }

        if (model == null)
            throw new TranslatableException("error.custommodel.loadmodelpack.nomodel");
        InputStream modelInputStream = new FileInputStream(model);
        JsonObject jsonObj = new JsonParser().parse(new InputStreamReader(modelInputStream)).getAsJsonObject();
        IOUtils.closeQuietly(modelInputStream);
        ModelInfo info = fromJson(jsonObj);
        info.fileName = file.getName();
        return info;
    }

    private static boolean isValidId(String id) {
        return id.length() > 0 && (id.charAt(0) < '0' || id.charAt(0) > '9') && id.chars().allMatch(c -> {
            return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_';
        });
    }

    public static ModelInfo fromJson(JsonObject jsonObj) throws ParseException {
        ModelInfo res = new ModelInfo();

        res.modelId = Json.getString(jsonObj, CustomJsonModel.MODEL_ID);
        if (res.modelId == null || !isValidId(res.modelId))
            throw new TranslatableException("error.custommodel.loadmodelpack.invalidid", res.modelId);

        res.modelName = Json.getString(jsonObj, CustomJsonModel.MODEL_NAME);
        if (res.modelName == null)
            throw new TranslatableException("error.custommodel.loadmodelpack.invalidname");

        res.version = Json.getString(jsonObj, CustomJsonModel.VERSION, "");
        res.author = Json.getString(jsonObj, CustomJsonModel.AUTHOR, "");

        Json.parseJsonObject(jsonObj.get(CustomJsonModel.EYE_HEIGHT), (key, value) -> {
            EntityPose pose = CustomJsonModel.poseMap.get(key);
            if (pose == null)
                throw new TranslatableException("error.custommodel.loadmodelpack.unknownpose", key);
            res.eyeHeightMap.put(pose, value.getAsFloat());
        });

        Json.parseJsonObject(jsonObj.get(CustomJsonModel.BOUNDING_BOX), (key, value) -> {
            EntityPose pose = CustomJsonModel.poseMap.get(key);
            if (pose == null)
                throw new TranslatableException("error.custommodel.loadmodelpack.unknownpose", key);
            float[] dimension = Json.parseFloatArray(value, 2);
            res.dimensionsMap.put(pose, new EntityDimensions(dimension[0], dimension[1]));
        });

        return res;
    }

    public ModelPackInfo getInfo() {
        return new ModelPackInfo(fileName, modelId, modelName, version, author);
    }

    public String fileName;

    public String modelId;
    public String modelName;
    public String version;
    public String author;

    public Map<EntityPose, Float> eyeHeightMap = Maps.newEnumMap(EntityPose.class);
    public Map<EntityPose, EntityDimensions> dimensionsMap = Maps.newEnumMap(EntityPose.class);

    private ModelInfo() {}
}
