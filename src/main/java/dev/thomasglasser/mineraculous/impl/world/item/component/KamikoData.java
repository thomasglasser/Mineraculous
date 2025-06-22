package dev.thomasglasser.mineraculous.impl.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public record KamikoData(UUID uuid, UUID owner, int nameColor, Optional<ResourceLocation> faceMaskTexture) {

    public static final Codec<KamikoData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("uuid").forGetter(KamikoData::uuid),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("owner").forGetter(KamikoData::owner),
            Codec.INT.fieldOf("name_color").forGetter(KamikoData::nameColor),
            ResourceLocation.CODEC.optionalFieldOf("face_mask_texture").forGetter(KamikoData::faceMaskTexture)).apply(instance, KamikoData::new));
    public static final StreamCodec<ByteBuf, KamikoData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), KamikoData::uuid,
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), KamikoData::owner,
            ByteBufCodecs.INT, KamikoData::nameColor,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), KamikoData::faceMaskTexture,
            KamikoData::new);
    public Kamiko summon(ServerLevel level, Vec3 spawnPos) {
        Kamiko kamiko = MineraculousEntityTypes.KAMIKO.get().create(level);
        if (kamiko != null) {
            kamiko.setPos(spawnPos);
            kamiko.setUUID(uuid);
            kamiko.setOwnerUUID(owner);
            level.addFreshEntity(kamiko);
        }
        return kamiko;
    }
}
