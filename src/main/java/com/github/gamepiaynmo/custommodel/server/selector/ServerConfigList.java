package com.github.gamepiaynmo.custommodel.server.selector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.util.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerConfigList<K, V extends ServerConfigEntry<K>> {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected final Gson GSON;
    private final File file;
    private final Map<String, V> map = Maps.newHashMap();
    private boolean enabled = true;
    private static final ParameterizedType field_14369 = new ParameterizedType() {
        public Type[] getActualTypeArguments() {
            return new Type[]{ServerConfigEntry.class};
        }

        public Type getRawType() {
            return List.class;
        }

        public Type getOwnerType() {
            return null;
        }
    };

    public ServerConfigList(File file_1) {
        this.file = file_1;
        GsonBuilder gsonBuilder_1 = (new GsonBuilder()).setPrettyPrinting();
        gsonBuilder_1.registerTypeHierarchyAdapter(ServerConfigEntry.class, new ServerConfigList.DeSerializer());
        this.GSON = gsonBuilder_1.create();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean boolean_1) {
        this.enabled = boolean_1;
    }

    public File getFile() {
        return this.file;
    }

    public void add(V serverConfigEntry_1) {
        this.map.put(this.toString(serverConfigEntry_1.getKey()), serverConfigEntry_1);

        try {
            this.save();
        } catch (IOException var3) {
            LOGGER.warn("Could not save the list after adding a user.", var3);
        }

    }

    public V get(K object_1) {
        this.removeInvalidEntries();
        return this.map.get(this.toString(object_1));
    }

    public void remove(K object_1) {
        this.map.remove(this.toString(object_1));

        try {
            this.save();
        } catch (IOException var3) {
            LOGGER.warn("Could not save the list after removing a user.", var3);
        }

    }

    public void removeEntry(ServerConfigEntry<K> serverConfigEntry_1) {
        this.remove(serverConfigEntry_1.getKey());
    }

    public String[] getNames() {
        return (String[])this.map.keySet().toArray(new String[this.map.size()]);
    }

    public boolean isEmpty() {
        return this.map.size() < 1;
    }

    protected String toString(K object_1) {
        return object_1.toString();
    }

    protected boolean contains(K object_1) {
        return this.map.containsKey(this.toString(object_1));
    }

    private void removeInvalidEntries() {
        List<K> list_1 = Lists.newArrayList();
        Iterator var2 = this.map.values().iterator();

        while(var2.hasNext()) {
            V serverConfigEntry_1 = (V)var2.next();
            if (serverConfigEntry_1.isInvalid()) {
                list_1.add(serverConfigEntry_1.getKey());
            }
        }

        var2 = list_1.iterator();

        while(var2.hasNext()) {
            K object_1 = (K)var2.next();
            this.map.remove(this.toString(object_1));
        }

    }

    protected ServerConfigEntry<K> fromJson(JsonObject jsonObject_1) {
        return new ServerConfigEntry((Object)null, jsonObject_1);
    }

    public Collection<V> values() {
        return this.map.values();
    }

    public void save() throws IOException {
        Collection<V> collection_1 = this.map.values();
        String string_1 = this.GSON.toJson(collection_1);
        BufferedWriter bufferedWriter_1 = null;

        try {
            bufferedWriter_1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            bufferedWriter_1.write(string_1);
        } finally {
            IOUtils.closeQuietly(bufferedWriter_1);
        }

    }

    public void load() throws FileNotFoundException {
        if (this.file.exists()) {
            BufferedReader bufferedReader_1 = null;

            try {
                bufferedReader_1 = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                Collection<ServerConfigEntry<K>> collection_1 = (Collection) JsonUtils.fromJson(this.GSON, (Reader)bufferedReader_1, (Type)field_14369);
                if (collection_1 != null) {
                    this.map.clear();
                    Iterator var3 = collection_1.iterator();

                    while(var3.hasNext()) {
                        ServerConfigEntry<K> serverConfigEntry_1 = (ServerConfigEntry)var3.next();
                        if (serverConfigEntry_1.getKey() != null) {
                            this.map.put(this.toString(serverConfigEntry_1.getKey()), (V)serverConfigEntry_1);
                        }
                    }
                }
            } finally {
                IOUtils.closeQuietly(bufferedReader_1);
            }

        }
    }

    class DeSerializer implements JsonDeserializer<ServerConfigEntry<K>>, JsonSerializer<ServerConfigEntry<K>> {
        private DeSerializer() {
        }

        @Override
        public JsonElement serialize(ServerConfigEntry<K> serverConfigEntry_1, Type type_1, JsonSerializationContext jsonSerializationContext_1) {
            JsonObject jsonObject_1 = new JsonObject();
            serverConfigEntry_1.serialize(jsonObject_1);
            return jsonObject_1;
        }

        @Override
        public ServerConfigEntry<K> deserialize(JsonElement jsonElement_1, Type type_1, JsonDeserializationContext jsonDeserializationContext_1) throws JsonParseException {
            if (jsonElement_1.isJsonObject()) {
                JsonObject jsonObject_1 = jsonElement_1.getAsJsonObject();
                return ServerConfigList.this.fromJson(jsonObject_1);
            } else {
                return null;
            }
        }
    }
}
