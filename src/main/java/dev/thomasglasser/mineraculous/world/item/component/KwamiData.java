package dev.thomasglasser.mineraculous.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.world.entity.Kwami;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record KwamiData(UUID uuid, int id, boolean charged) {

    public static final Codec<KwamiData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(KwamiData::uuid),
            Codec.INT.fieldOf("id").forGetter(KwamiData::id),
            Codec.BOOL.fieldOf("charged").forGetter(KwamiData::charged)).apply(instance, KwamiData::new));
    public static final StreamCodec<ByteBuf, KwamiData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, KwamiData::uuid,
            ByteBufCodecs.INT, KwamiData::id,
            ByteBufCodecs.BOOL, KwamiData::charged,
            KwamiData::new);
    public static Kwami summon(Optional<KwamiData> kwamiData, ServerLevel level, ResourceKey<Miraculous> miraculous, Entity owner) {
        Kwami kwami = MineraculousEntityTypes.KWAMI.get().create(level);
        if (kwami != null) {
            kwami.setMiraculous(miraculous);
            KwamiData data = kwamiData.orElse(null);
            if (data != null) {
                kwami.setUUID(data.uuid());
                kwami.setId(data.id());
                kwami.setCharged(data.charged());
            } else {
                kwami.setCharged(true);
            }
            Direction direction = owner.getDirection().getOpposite();
            int xOffset = switch (direction) {
                case WEST -> 1;
                case EAST -> -1;
                default -> 0;
            };
            int zOffset = switch (direction) {
                case NORTH -> 1;
                case SOUTH -> -1;
                default -> 0;
            };
            kwami.teleportTo(level, owner.getX() + xOffset, owner.getY() + 1, owner.getZ() + zOffset, Set.of(), direction.toYRot(), 0.0F);
            if (owner instanceof Player player) {
                kwami.tame(player);
            } else {
                kwami.setOwnerUUID(owner.getUUID());
                kwami.setTame(true, true);
            }
            level.addFreshEntity(kwami);
            kwami.playSound(MineraculousSoundEvents.KWAMI_SUMMON.get());
            return kwami;
        }
        return null;
    }
}
