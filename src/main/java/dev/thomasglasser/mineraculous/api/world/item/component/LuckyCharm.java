package dev.thomasglasser.mineraculous.api.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.ability.RevertLuckyCharmTargetsAbilityEffectsAbility;
import dev.thomasglasser.mineraculous.api.world.ability.SummonTargetDependentLuckyCharmAbility;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

/**
 * Holds target and owner data for {@link SummonTargetDependentLuckyCharmAbility} and {@link RevertLuckyCharmTargetsAbilityEffectsAbility}.
 *
 * @param target The target of the lucky charm, if any
 * @param owner  The owner of the lucky charm
 * @param id     The id of the lucky charm, unique to the stack and incremented when a new one is summoned
 */
public record LuckyCharm(Optional<UUID> target, UUID owner, int id) {
    public static final Codec<LuckyCharm> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("target").forGetter(LuckyCharm::target),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(LuckyCharm::owner),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("id").forGetter(LuckyCharm::id)).apply(instance, LuckyCharm::new));

    public static final StreamCodec<ByteBuf, LuckyCharm> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), LuckyCharm::target,
            UUIDUtil.STREAM_CODEC, LuckyCharm::owner,
            ByteBufCodecs.INT, LuckyCharm::id,
            LuckyCharm::new);
}
