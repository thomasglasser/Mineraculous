package dev.thomasglasser.mineraculous.impl.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.entity.ai.memory.MineraculousMemoryModuleTypes;
import dev.thomasglasser.mineraculous.api.world.entity.ai.memory.ReplicationState;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtils;

public record KamikoData(UUID uuid, UUID owner, int powerLevel, int nameColor, Optional<ResourceLocation> faceMaskTexture) {

    public static final Codec<KamikoData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("uuid").forGetter(KamikoData::uuid),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("owner").forGetter(KamikoData::owner),
            Codec.INT.fieldOf("power_level").forGetter(KamikoData::powerLevel),
            Codec.INT.fieldOf("name_color").forGetter(KamikoData::nameColor),
            ResourceLocation.CODEC.optionalFieldOf("face_mask_texture").forGetter(KamikoData::faceMaskTexture)).apply(instance, KamikoData::new));
    public static final StreamCodec<ByteBuf, KamikoData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), KamikoData::uuid,
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), KamikoData::owner,
            ByteBufCodecs.INT, KamikoData::powerLevel,
            ByteBufCodecs.INT, KamikoData::nameColor,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), KamikoData::faceMaskTexture,
            KamikoData::new);
    public KamikoData(UUID uuid, UUID owner, int powerLevel, int nameColor, Optional<ResourceLocation> faceMaskTexture) {
        this.uuid = uuid;
        this.owner = owner;
        this.powerLevel = Math.clamp(powerLevel, 0, MiraculousData.MAX_POWER_LEVEL);
        this.nameColor = nameColor;
        this.faceMaskTexture = faceMaskTexture;
    }

    public Kamiko summon(ServerLevel level, Vec3 spawnPos, boolean replicate) {
        Kamiko kamiko = MineraculousEntityTypes.KAMIKO.get().create(level);
        if (kamiko != null) {
            kamiko.setPos(spawnPos);
            kamiko.setUUID(uuid);
            kamiko.setOwnerUUID(owner);
            if (replicate && MineraculousServerConfig.get().enableKamikoReplication.getAsBoolean()) {
                BrainUtils.setMemory(kamiko, MineraculousMemoryModuleTypes.REPLICATION_STATUS.get(), ReplicationState.LOOKING_FOR_RESTING_LOCATION);
            }
            level.addFreshEntity(kamiko);
        }
        return kamiko;
    }
}
