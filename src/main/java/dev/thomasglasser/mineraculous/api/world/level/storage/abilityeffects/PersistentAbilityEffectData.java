package dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.ContinuousAbility;
import dev.thomasglasser.mineraculous.api.world.ability.ReplaceAdjacentBlocksAbility;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.BlockLocation;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
public record PersistentAbilityEffectData(boolean playedContinuousAbilityStartSound, Optional<Integer> continuousTicks, Set<ReferenceLinkedOpenHashSet<DelayedBlockReplacement>> delayedBlockReplacements, Optional<UUID> killCreditOverride) {

    public static final Codec<PersistentAbilityEffectData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("played_continuous_ability_start_sound").forGetter(PersistentAbilityEffectData::playedContinuousAbilityStartSound),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("continuous_ticks").forGetter(PersistentAbilityEffectData::continuousTicks),
            DelayedBlockReplacement.CODEC.listOf().listOf().xmap(PersistentAbilityEffectData::readDelayedBlockReplacements, PersistentAbilityEffectData::writeDelayedBlockReplacements).fieldOf("delayed_block_replacements").forGetter(PersistentAbilityEffectData::delayedBlockReplacements),
            UUIDUtil.CODEC.optionalFieldOf("kill_credit_override").forGetter(PersistentAbilityEffectData::killCreditOverride)).apply(instance, PersistentAbilityEffectData::new));

    public PersistentAbilityEffectData() {
        this(false, Optional.empty(), new ReferenceOpenHashSet<>(), Optional.empty());
    }

    @ApiStatus.Internal
    public void tick(LivingEntity entity, ServerLevel level) {
        Iterator<ReferenceLinkedOpenHashSet<DelayedBlockReplacement>> iterator = delayedBlockReplacements.iterator();
        while (iterator.hasNext()) {
            ReferenceLinkedOpenHashSet<DelayedBlockReplacement> set = iterator.next();
            if (!set.isEmpty()) {
                DelayedBlockReplacement replacement = set.removeFirst();
                ReplaceAdjacentBlocksAbility.replace(replacement.location(), replacement.replacement(), entity.getUUID(), level);
            } else {
                iterator.remove();
            }
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

    public PersistentAbilityEffectData withKillCreditOverride(Optional<UUID> killCreditOverride) {
        return new PersistentAbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, delayedBlockReplacements, killCreditOverride);
    }

    private static Set<ReferenceLinkedOpenHashSet<DelayedBlockReplacement>> readDelayedBlockReplacements(List<List<DelayedBlockReplacement>> delayedBlockReplacements) {
        Set<ReferenceLinkedOpenHashSet<DelayedBlockReplacement>> set = new ReferenceOpenHashSet<>();
        for (List<DelayedBlockReplacement> list : delayedBlockReplacements) {
            set.add(new ReferenceLinkedOpenHashSet<>(list));
        }
        return set;
    }

    private static List<List<DelayedBlockReplacement>> writeDelayedBlockReplacements(Set<ReferenceLinkedOpenHashSet<DelayedBlockReplacement>> delayedBlockReplacements) {
        List<List<DelayedBlockReplacement>> list = new ReferenceArrayList<>();
        for (ReferenceLinkedOpenHashSet<DelayedBlockReplacement> set : delayedBlockReplacements) {
            list.add(new ReferenceArrayList<>(set));
        }
        return list;
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
