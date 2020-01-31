package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.api.ModelPackInfo;
import com.github.gamepiaynmo.custommodel.expression.ParseException;
import com.github.gamepiaynmo.custommodel.client.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.network.PacketModel;
import com.github.gamepiaynmo.custommodel.util.Json;
import com.github.gamepiaynmo.custommodel.util.TranslatableException;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ModelInfo {

    public static ModelInfo fromFile(File file) throws IOException, ParseException {
        if (file.isDirectory())
            return fromFolder(file);
        else return fromZipFile(file);
    }

    private static ModelInfo fromZip(InputStream stream, String filename) throws IOException, ParseException {
        ZipInputStream zip = new ZipInputStream(stream);
        ZipEntry entry;
        InputStream modelInputStream = null;

        while ((entry = zip.getNextEntry()) != null) {
            if (entry.isDirectory())
                continue;
            if (entry.getName().equals("model.json")) {
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream array = new ByteArrayOutputStream();
                int cnt = 0;
                while ((cnt = zip.read(buffer, 0, 1024)) > 0)
                    array.write(buffer, 0, cnt);
                array.close();
                modelInputStream = new ByteArrayInputStream(array.toByteArray());
                break;
            }
        }

        if (entry == null)
            throw new TranslatableException("error.custommodel.loadmodelpack.nomodel");
        JsonObject jsonObj = new JsonParser().parse(new InputStreamReader(modelInputStream)).getAsJsonObject();
        IOUtils.closeQuietly(modelInputStream);
        ModelInfo info = fromJson(jsonObj);
        info.fileName = filename;
        return info;
    }

    private static ModelInfo fromZipFile(File file) throws IOException, ParseException {
        return fromZip(new BufferedInputStream(new FileInputStream(file)), file.getName());
    }

    public static ModelInfo fromZipMemory(byte[] data) throws IOException, ParseException {
        ModelInfo info = fromZip(new ByteArrayInputStream(data), "");
        info.data = data;
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
            res.dimensionsMap.put(pose, new EntityDimensions(dimension[0], dimension[1], true));
        });

        return res;
    }

    public PacketModel getPacket(UUID uuid) {
        if (data != null) return new PacketModel(uuid, data);
        else return new PacketModel(getModelFile(), uuid);
    }

    public File getModelFile() {
        return new File(CustomModel.MODEL_DIR + "/" + fileName);
    }

    public String fileName;
    public byte[] data = null;
    public UUID sender = null;

    public String modelId;
    public String modelName;
    public String version;
    public String author;

    public Map<EntityPose, Float> eyeHeightMap = Maps.newEnumMap(EntityPose.class);
    public Map<EntityPose, EntityDimensions> dimensionsMap = Maps.newEnumMap(EntityPose.class);

    private ModelInfo() {}
}
