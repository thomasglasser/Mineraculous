package dev.thomasglasser.mineraculous.impl.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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
    public static Kwami summon(Optional<KwamiData> kwamiData, ServerLevel level, Holder<Miraculous> miraculous, Entity owner) {
        Kwami kwami = MineraculousEntityTypes.KWAMI.get().create(level);
        if (kwami != null) {
            kwami.setSummonTicks(SharedConstants.TICKS_PER_SECOND * MineraculousServerConfig.get().kwamiSummonTime.getAsInt());
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
            // TODO: Spin around and land in front facing owner
            kwami.teleportTo(level, owner.getX(), owner.getY(), owner.getZ(), Set.of(), direction.toYRot(), 0.0F);
            kwami.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new Vec3(owner.getX() + xOffset, owner.getY() + 1, owner.getZ() + zOffset), 4, 1));
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

    public static Optional<KwamiData> renounce(Optional<KwamiData> kwamiData, ItemStack stack, ServerLevel level) {
        stack.set(MineraculousDataComponents.POWERED, Unit.INSTANCE);
        stack.remove(MineraculousDataComponents.REMAINING_TICKS);
        if (kwamiData.isPresent() && level.getEntity(kwamiData.get().uuid()) instanceof Kwami kwami) {
            KwamiData newData = new KwamiData(kwami.getUUID(), kwami.getId(), kwamiData.get().charged());
            stack.set(MineraculousDataComponents.KWAMI_DATA, newData);
            kwami.discard();
            return Optional.of(newData);
        }
        return Optional.empty();
    }
}
