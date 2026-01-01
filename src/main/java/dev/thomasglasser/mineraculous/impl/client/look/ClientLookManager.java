package dev.thomasglasser.mineraculous.impl.client.look;

import com.google.gson.JsonParser;
import dev.thomasglasser.mineraculous.api.client.look.Look;
import dev.thomasglasser.mineraculous.api.client.look.asset.BuiltInLookAssets;
import dev.thomasglasser.mineraculous.api.client.look.asset.LoadedLookAssets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ClientLookManager {
    private static final Map<ResourceLocation, Look<BuiltInLookAssets>> BUILT_IN_LOOKS = new ConcurrentHashMap<>();
    private static final Map<String, LoadedLook> LOADED_LOOKS = new ConcurrentHashMap<>();
    private static final Map<String, LoadedLook> EQUIPPABLE_LOOKS = new ConcurrentHashMap<>();

    public static void refreshLoaded() {
        LOADED_LOOKS.clear();
        EQUIPPABLE_LOOKS.clear();
    }

    public static void setBuiltIn(Map<ResourceLocation, Look<BuiltInLookAssets>> looks) {
        BUILT_IN_LOOKS.clear();
        BUILT_IN_LOOKS.putAll(looks);
    }

    public static void add(String hash, Path root, Path source, Path file, boolean equippable) throws IOException {
        Look<LoadedLookAssets> look = Look.load(JsonParser.parseString(Files.readString(file)).getAsJsonObject(), ResourceLocation.withDefaultNamespace(hash), () -> new LoadedLookAssets.Builder(root));
        LOADED_LOOKS.put(hash, new LoadedLook(look, source));
        if (equippable)
            EQUIPPABLE_LOOKS.put(hash, new LoadedLook(look, source));
    }

    public static Set<ResourceLocation> getBuiltIn() {
        return BUILT_IN_LOOKS.keySet();
    }

    public static @Nullable Look<BuiltInLookAssets> getBuiltInLook(ResourceLocation id) {
        return BUILT_IN_LOOKS.get(id);
    }

    public static @Nullable Look<LoadedLookAssets> getLoadedLook(String hash) {
        LoadedLook look = LOADED_LOOKS.get(hash);
        return look != null ? look.look() : null;
    }

    public static Set<String> getEquippable() {
        return EQUIPPABLE_LOOKS.keySet();
    }

    public static @Nullable Look<LoadedLookAssets> getEquippableLook(String hash) {
        LoadedLook look = EQUIPPABLE_LOOKS.get(hash);
        return look != null ? look.look() : null;
    }

    public static @Nullable Path getEquippablePath(String hash) {
        LoadedLook look = EQUIPPABLE_LOOKS.get(hash);
        return look != null ? look.path() : null;
    }

    private record LoadedLook(Look<LoadedLookAssets> look, Path path) {}
}
