package dev.thomasglasser.mineraculous.impl.server.look;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.core.look.LookData;
import dev.thomasglasser.mineraculous.impl.network.ClientboundRequestLooksPayload;
import dev.thomasglasser.mineraculous.impl.network.ClientboundSendLookPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class ServerLookManager {
    private static final Map<String, CachedLook> LOOKS = new ConcurrentHashMap<>();

    public static void refresh() {
        LOOKS.clear();
    }

    public static @Nullable CachedLook get(String hash) {
        return LOOKS.get(hash);
    }

    public static void add(String hash, Path source, boolean equippable) throws IOException {
        add(hash, Files.readAllBytes(source), equippable);
    }

    public static void add(String hash, byte[] data, boolean equippable) {
        LOOKS.put(hash, new CachedLook(data, equippable));
    }

    public static void requestMissingLooks(LookData data, ServerPlayer player) {
        ImmutableSet.Builder<String> missing = new ImmutableSet.Builder<>();
        for (ResourceLocation id : data.looks().values()) {
            if (id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
                if (!LOOKS.containsKey(id.getPath()))
                    missing.add(id.getPath());
            }
        }
        TommyLibServices.NETWORK.sendToClient(new ClientboundRequestLooksPayload(missing.build()), player);
    }

    public static void sendServerLooks(ServerPlayer player) {
        for (Map.Entry<String, CachedLook> entry : LOOKS.entrySet()) {
            if (entry.getValue().equippable())
                TommyLibServices.NETWORK.sendToClient(new ClientboundSendLookPayload(entry.getKey(), entry.getValue()), player);
        }
    }

    public record CachedLook(byte[] data, boolean equippable) {
        public static final StreamCodec<ByteBuf, CachedLook> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BYTE_ARRAY, CachedLook::data,
                ByteBufCodecs.BOOL, CachedLook::equippable,
                CachedLook::new);
    }
}
