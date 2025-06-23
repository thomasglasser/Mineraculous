package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityEffectData;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Performs the provided ability for the provided number of ticks after it consumes.
 *
 * @param ability           The ability to perform
 * @param ticks             The number of ticks to perform the ability for after it consumes
 * @param passiveStartSound The sound to play when started
 * @param activeStartSound  The sound to play when the provided ability first consumes
 * @param finishSound       The sound to play when the provided number of ticks has passed after the ability consumes
 */
public record ContinuousAbility(Holder<Ability> ability, int ticks, Optional<Holder<SoundEvent>> passiveStartSound, Optional<Holder<SoundEvent>> activeStartSound, Optional<Holder<SoundEvent>> finishSound) implements AbilityWithSubAbilities {

    public static final MapCodec<ContinuousAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ability.CODEC.fieldOf("ability").forGetter(ContinuousAbility::ability),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("ticks", SharedConstants.TICKS_PER_SECOND).forGetter(ContinuousAbility::ticks),
            SoundEvent.CODEC.optionalFieldOf("passive_start_sound").forGetter(ContinuousAbility::passiveStartSound),
            SoundEvent.CODEC.optionalFieldOf("active_start_sound").forGetter(ContinuousAbility::activeStartSound),
            SoundEvent.CODEC.optionalFieldOf("finish_sound").forGetter(ContinuousAbility::finishSound)).apply(instance, ContinuousAbility::new));
    public ContinuousAbility(Holder<Ability> ability, Optional<Holder<SoundEvent>> passiveStartSound, Optional<Holder<SoundEvent>> activeStartSound, Optional<Holder<SoundEvent>> finishSound) {
        this(ability, SharedConstants.TICKS_PER_SECOND, passiveStartSound, activeStartSound, finishSound);
    }

    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        boolean consume = ability.value().perform(data, level, performer, handler, context);
        AbilityEffectData abilityEffectData = performer.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
        if (context == null && abilityEffectData.continuousTicks().isPresent()) {
            int continuousTicks = abilityEffectData.continuousTicks().get();
            continuousTicks--;
            if (continuousTicks <= 0) {
                abilityEffectData.stopContinuousAbility().save(performer, true);
                Ability.playSound(level, performer, finishSound);
                return true;
            } else {
                abilityEffectData.withContinuousTicks(Optional.of(continuousTicks)).save(performer, true);
            }
        }
        if (consume) {
            if (performer instanceof ServerPlayer player && context != null) {
                handler.triggerPerformAdvancement(player, context);
            }
            if (abilityEffectData.continuousTicks().isEmpty()) {
                abilityEffectData.withContinuousTicks(Optional.of(ticks)).save(performer, true);
                Ability.playSound(level, performer, activeStartSound);
                return false;
            }
        }
        if (!abilityEffectData.playedContinuousAbilityStartSound()) {
            abilityEffectData.withPlayedContinuousAbilityStartSound(true).save(performer, true);
            Ability.playSound(level, performer, passiveStartSound);
        }
        return false;
    }

    @Override
    public List<Ability> getAll() {
        List<Ability> abilities = new ReferenceArrayList<>();
        abilities.add(this);
        abilities.addAll(Ability.getAll(ability.value()));
        return abilities;
    }

    @Override
    public List<Ability> getMatching(Predicate<Ability> predicate) {
        List<Ability> abilities = new ReferenceArrayList<>();
        abilities.add(this);
        abilities.addAll(Ability.getMatching(predicate, ability.value()));
        return abilities;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.CONTINUOUS.get();
    }
}
