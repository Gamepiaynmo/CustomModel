package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.CustomTexture;
import com.github.gamepiaynmo.custommodel.util.TranslatableException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ModelPack {

    public static final TextureGetter skinGetter = player -> player.getSkinTexture();

    private JsonObject modelJson;
    private Map<String, Identifier> textureIds = Maps.newHashMap();
    private CustomJsonModel model;
    private boolean success = false;
    private String dirName;

    private ModelPack() {}

    public static ModelPack fromDirectory(TextureManager textureManager, File dir) throws FileNotFoundException, IOException {
        IModelResource modelFile = null;
        List<IModelResource> textureFiles = Lists.newArrayList();

        class FileResource implements IModelResource {
            private final File file;

            public FileResource(File file) {
                this.file = file;
            }

            @Override
            public String getName() {
                return file.getName();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(file);
            }
        }

        for (File modelPackItem : dir.listFiles()) {
            if (modelPackItem.isDirectory())
                continue;

            if (modelPackItem.getName().equals("model.json"))
                modelFile = new FileResource(modelPackItem);
            else if (modelPackItem.getName().endsWith(".png"))
                textureFiles.add(new FileResource(modelPackItem));
        }

        return fromResource(textureManager, dir.getName(), modelFile, textureFiles);
    }

    public static ModelPack fromZipFile(TextureManager textureManager, File zipFile) throws IOException {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
        ZipFile file = new ZipFile(zipFile);
        ZipEntry entry;

        class ZipResource implements IModelResource {
            private final ZipEntry entry;

            public ZipResource(ZipEntry entry) {
                this.entry = entry;
            }

            @Override
            public String getName() {
                return entry.getName();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return file.getInputStream(entry);
            }
        }

        IModelResource modelFile = null;
        List<IModelResource> textureFiles = Lists.newArrayList();
        while ((entry = zip.getNextEntry()) != null) {
            if (entry.isDirectory())
                continue;

            if (entry.getName().equals("model.json"))
                modelFile = new ZipResource(entry);
            else if (entry.getName().endsWith(".png"))
                textureFiles.add(new ZipResource(entry));
        }

        return fromResource(textureManager, zipFile.getName(), modelFile, textureFiles);
    }

    public static ModelPack fromZipMemory(TextureManager textureManager, String name, byte[] data) throws IOException {
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data));
        ZipEntry entry;

        class MemoryResource implements IModelResource {
            private final String name;
            private final byte[] data;

            public MemoryResource(ZipEntry zipEntry) throws IOException {
                name = zipEntry.getName();
                data = new byte[(int) zipEntry.getSize()];
                int cnt = 0, pos = 0;
                while ((cnt = zip.read(data, pos, data.length - pos)) > 0)
                    pos += cnt;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(data);
            }
        }

        IModelResource modelFile = null;
        List<IModelResource> textureFiles = Lists.newArrayList();
        while ((entry = zip.getNextEntry()) != null) {
            if (entry.isDirectory())
                continue;

            if (entry.getName().equals("model.json"))
                modelFile = new MemoryResource(entry);
            else if (entry.getName().endsWith(".png"))
                textureFiles.add(new MemoryResource(entry));
        }

        return fromResource(textureManager, name, modelFile, textureFiles);
    }

    public static ModelPack fromResource(TextureManager textureManager, String name, IModelResource model, Collection<IModelResource> textures) throws IOException {
        ModelPack pack = new ModelPack();
        pack.dirName = getFileName(name);
        if (model == null)
            throw new TranslatableException("error.custommodel.loadmodelpack.nomodel");
        InputStream modelInputStream = model.getInputStream();
        pack.modelJson = new JsonParser().parse(new InputStreamReader(modelInputStream)).getAsJsonObject();
        IOUtils.closeQuietly(modelInputStream);
        for (IModelResource texture : textures) {
            Identifier identifier = new Identifier(CustomModel.MODID, (pack.dirName + "/" + texture.getName()).toLowerCase());
            pack.textureIds.put(texture.getName(), identifier);
            NativeImage image = NativeImage.read(texture.getInputStream());
            textureManager.registerTexture(identifier, new CustomTexture(image));
        }

        pack.model = CustomJsonModel.fromJson(pack, pack.modelJson);
        pack.success = true;
        return pack;
    }

    private static String getFileName(String path) {
        int idx1 = Math.max(0, Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')));
        int idx2 = path.indexOf('.', idx1);
        if (idx2 < 0) idx2 = path.length() - 1;
        return path.substring(idx1, idx2);
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
        for (Identifier texture : textureIds.values())
            CustomModelClient.textureManager.destroyTexture(texture);
        model.release();
    }

    public String getDirName() {
        return dirName;
    }

    public static interface TextureGetter {
        public Identifier getTexture(AbstractClientPlayerEntity player);
    }

    public static interface IModelResource {
        public String getName();
        public InputStream getInputStream() throws IOException;
    }
}
