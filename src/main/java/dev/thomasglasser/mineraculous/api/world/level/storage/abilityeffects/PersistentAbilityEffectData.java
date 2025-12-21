package dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.ContinuousAbility;
import dev.thomasglasser.mineraculous.api.world.ability.ReplaceAdjacentBlocksAbility;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.BlockLocation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

/**
 * Holds information used by existing {@link Ability}s that are persistent across server restarts.
 *
 * @param playedContinuousAbilityStartSound Whether the start sound of the current {@link ContinuousAbility} has been played
 * @param continuousTicks                   The current tick count of the current {@link ContinuousAbility} if present
 * @param delayedBlockReplacements          Sets of {@link DelayedBlockReplacement}s to slowly replace on tick
 * @param killCreditOverride                The kill credit override for the entity if present
 */
public record PersistentAbilityEffectData(boolean playedContinuousAbilityStartSound, Optional<Integer> continuousTicks, ImmutableSet<ImmutableSet<DelayedBlockReplacement>> delayedBlockReplacements, Optional<UUID> killCreditOverride) {

    public static final Codec<PersistentAbilityEffectData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("played_continuous_ability_start_sound").forGetter(PersistentAbilityEffectData::playedContinuousAbilityStartSound),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("continuous_ticks").forGetter(PersistentAbilityEffectData::continuousTicks),
            DelayedBlockReplacement.CODEC.listOf().listOf().xmap(PersistentAbilityEffectData::readDelayedBlockReplacements, PersistentAbilityEffectData::writeDelayedBlockReplacements).fieldOf("delayed_block_replacements").forGetter(PersistentAbilityEffectData::delayedBlockReplacements),
            UUIDUtil.CODEC.optionalFieldOf("kill_credit_override").forGetter(PersistentAbilityEffectData::killCreditOverride)).apply(instance, PersistentAbilityEffectData::new));

    public PersistentAbilityEffectData() {
        this(false, Optional.empty(), ImmutableSet.of(), Optional.empty());
    }

    @ApiStatus.Internal
    public void tick(LivingEntity entity, ServerLevel level) {
        if (!delayedBlockReplacements.isEmpty()) {
            ImmutableSet.Builder<ImmutableSet<DelayedBlockReplacement>> updatedReplacements = new ImmutableSet.Builder<>();
            for (ImmutableSet<DelayedBlockReplacement> set : delayedBlockReplacements) {
                if (!set.isEmpty()) {
                    ImmutableList<DelayedBlockReplacement> list = set.asList();
                    DelayedBlockReplacement replacement = list.getFirst();
                    ReplaceAdjacentBlocksAbility.replace(replacement.location(), replacement.replacement(), entity.getUUID(), level);
                    if (set.size() > 1)
                        updatedReplacements.add(ImmutableSet.copyOf(list.subList(1, list.size())));
                }
            }
            withDelayedBlockReplacements(updatedReplacements.build()).save(entity);
        }
    }

    public PersistentAbilityEffectData stopContinuousAbility() {
        return new PersistentAbilityEffectData(false, Optional.empty(), delayedBlockReplacements, killCreditOverride);
    }

    public PersistentAbilityEffectData withPlayedContinuousAbilityStartSound(boolean playedContinuousAbilityStartSound) {
        return new PersistentAbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, delayedBlockReplacements, killCreditOverride);
    }

    public PersistentAbilityEffectData withContinuousTicks(Optional<Integer> continuousTicks) {
        return new PersistentAbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, delayedBlockReplacements, killCreditOverride);
    }

    public PersistentAbilityEffectData withDelayedBlockReplacements(ImmutableSet<ImmutableSet<DelayedBlockReplacement>> delayedBlockReplacements) {
        return new PersistentAbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, delayedBlockReplacements, killCreditOverride);
    }

    public PersistentAbilityEffectData addDelayedBlockReplacements(ImmutableSet<DelayedBlockReplacement> replacements) {
        ImmutableSet.Builder<ImmutableSet<DelayedBlockReplacement>> builder = new ImmutableSet.Builder<>();
        builder.addAll(delayedBlockReplacements);
        builder.add(replacements);
        return new PersistentAbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, builder.build(), killCreditOverride);
    }

    public PersistentAbilityEffectData withKillCreditOverride(Optional<UUID> killCreditOverride) {
        return new PersistentAbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, delayedBlockReplacements, killCreditOverride);
    }

    private static ImmutableSet<ImmutableSet<DelayedBlockReplacement>> readDelayedBlockReplacements(List<List<DelayedBlockReplacement>> delayedBlockReplacements) {
        ImmutableSet.Builder<ImmutableSet<DelayedBlockReplacement>> set = new ImmutableSet.Builder<>();
        for (List<DelayedBlockReplacement> list : delayedBlockReplacements) {
            set.add(ImmutableSet.copyOf(list));
        }
        return set.build();
    }

    private static List<List<DelayedBlockReplacement>> writeDelayedBlockReplacements(ImmutableSet<ImmutableSet<DelayedBlockReplacement>> delayedBlockReplacements) {
        ImmutableList.Builder<List<DelayedBlockReplacement>> list = new ImmutableList.Builder<>();
        for (ImmutableSet<DelayedBlockReplacement> set : delayedBlockReplacements) {
            list.add(ImmutableList.copyOf(set));
        }
        return list.build();
    }
    public record DelayedBlockReplacement(BlockLocation location, BlockState replacement) {
        private static final Codec<DelayedBlockReplacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockLocation.CODEC.fieldOf("location").forGetter(DelayedBlockReplacement::location),
                BlockState.CODEC.fieldOf("replacement").forGetter(DelayedBlockReplacement::replacement)).apply(instance, DelayedBlockReplacement::new));
    }

    public void save(Entity entity) {
        entity.setData(MineraculousAttachmentTypes.PERSISTENT_ABILITY_EFFECTS, this);
    }
}
