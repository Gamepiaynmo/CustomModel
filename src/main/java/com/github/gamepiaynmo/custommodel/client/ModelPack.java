package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.expression.ConstantFloat;
import com.github.gamepiaynmo.custommodel.expression.IExpressionFloat;
import com.github.gamepiaynmo.custommodel.expression.ParseException;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.CustomTexture;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.util.TranslatableException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Texture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ModelPack {
    public static final Supplier<Identifier>[] defGetter = new Supplier[]{ () -> CustomModelClient.currentPlayer.getSkinTexture(),
            () -> CustomModelClient.currentPlayer.getCapeTexture(),
            () -> CustomModelClient.currentPlayer.getElytraTexture() };

    private Map<String, Integer> textureIds = Maps.newHashMap();
    private List<Identifier> textures = Lists.newArrayList();
    private CustomJsonModel model;
    private boolean success = false;
    private String dirName;
    private List<Texture> texList = Lists.newArrayList();

    private ModelPack() {}

    public static ModelPack fromDirectory(TextureManager textureManager, File dir, UUID uuid) throws IOException, ParseException {
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

        return fromResource(textureManager, uuid, modelFile, textureFiles);
    }

    public static ModelPack fromZipFile(TextureManager textureManager, File zipFile, UUID uuid) throws IOException, ParseException {
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

        return fromResource(textureManager, uuid, modelFile, textureFiles);
    }

    public static ModelPack fromZipMemory(TextureManager textureManager, UUID uuid, byte[] data) throws IOException, ParseException {
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data));
        ZipEntry entry;
        byte[] buffer = new byte[1024];

        class MemoryResource implements IModelResource {
            private final String name;
            private final byte[] data;

            public MemoryResource(ZipEntry zipEntry) throws IOException {
                name = zipEntry.getName();
                ByteArrayOutputStream array = new ByteArrayOutputStream();
                int cnt = 0;
                while ((cnt = zip.read(buffer, 0, 1024)) > 0)
                    array.write(buffer, 0, cnt);
                array.close();
                data = array.toByteArray();
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

        return fromResource(textureManager, uuid, modelFile, textureFiles);
    }

    public static ModelPack fromResource(TextureManager textureManager, UUID uuid, IModelResource model, Collection<IModelResource> textures) throws IOException, ParseException {
        ModelPack pack = new ModelPack();
        pack.dirName = uuid.toString().toLowerCase();
        if (model == null)
            throw new TranslatableException("error.custommodel.loadmodelpack.nomodel");
        InputStream modelInputStream = model.getInputStream();
        JsonObject modelJson = new JsonParser().parse(new InputStreamReader(modelInputStream)).getAsJsonObject();
        IOUtils.closeQuietly(modelInputStream);
        for (IModelResource texture : textures) {
            Identifier identifier = new Identifier(CustomModel.MODID, (pack.dirName + "/" + texture.getName()).toLowerCase());
            pack.textureIds.put(getFileName(texture.getName()), pack.textures.size());
            pack.textures.add(identifier);
            NativeImage image = NativeImage.read(texture.getInputStream());
            CustomTexture tex = new CustomTexture(image);
            pack.texList.add(tex);
            textureManager.registerTexture(identifier, tex);
        }

        pack.model = CustomJsonModel.fromJson(pack, modelJson);
        pack.success = true;
        return pack;
    }

    private static String getFileName(String path) {
        int idx1 = Math.max(0, Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')));
        int idx2 = path.indexOf('.', idx1);
        if (idx2 < 0) idx2 = path.length();
        return path.substring(idx1, idx2);
    }

    public Supplier<Identifier> getBaseTexture() {
        return defGetter[0];
    }

    public Supplier<Identifier> getTexture(int id) {
        if (id < defGetter.length)
            return defGetter[id];
        id -= defGetter.length;
        if (id < textures.size()) {
            Identifier identifier = textures.get(id);
            return () -> identifier;
        }
        return defGetter[0];
    }

    public IExpressionFloat getTexture(String name) {
        if (name.equals("skin"))
            return new ConstantFloat(0);
        if (name.equals("cape"))
            return new NullableTextureId(1);
        if (name.equals("elytra"))
            return new NullableTextureId(2);
        Integer id = textureIds.get(name);
        if (id != null)
            return new ConstantFloat(id + defGetter.length);
        return null;
    }

    public boolean successfulLoaded() {
        return success;
    }

    public CustomJsonModel getModel() {
        return model;
    }

    public void release() {
        for (Texture texture : texList)
            TextureUtil.releaseTextureId(texture.getGlId());
        model.release();
    }

    public interface TextureGetter {
        Identifier getTexture(AbstractClientPlayerEntity player);
    }

    public interface IModelResource {
        String getName();
        InputStream getInputStream() throws IOException;
    }

    public static class NullableTextureId implements IExpressionFloat {
        int getter;

        public NullableTextureId(int getter) {
            this.getter = getter;
        }

        @Override
        public float eval() {
            return defGetter[getter].get() != null ? getter : -1;
        }
    }
}
