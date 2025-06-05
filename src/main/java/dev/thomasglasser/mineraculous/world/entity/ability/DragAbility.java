package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityEffectData;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public record DragAbility(Holder<Ability> ability, int dragTicks) implements AbilityWithSubAbilities {
    public static final MapCodec<DragAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ability.CODEC.fieldOf("ability").forGetter(DragAbility::ability),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("drag_ticks", SharedConstants.TICKS_PER_SECOND).forGetter(DragAbility::dragTicks)).apply(instance, DragAbility::new));

    public DragAbility(Holder<Ability> ability) {
        this(ability, SharedConstants.TICKS_PER_SECOND);
    }

    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        boolean consume = ability.value().perform(data, level, performer, context);
        AbilityEffectData abilityEffectData = performer.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
        if (context == null && abilityEffectData.dragTicks().isPresent()) {
            int dragTicks = abilityEffectData.dragTicks().get();
            dragTicks--;
            if (dragTicks <= 0) {
                abilityEffectData.withDragTicks(Optional.empty()).save(performer, true);
                return consume;
            }
        }
        if (consume) {
            if (performer instanceof ServerPlayer serverPlayer && context != null) {
                if (data.power().left().isPresent())
                    MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(serverPlayer, data.power().left().get(), context.advancementContext());
                if (data.power().right().isPresent())
                    MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(serverPlayer, data.power().right().get(), context.advancementContext());
            }
            if (abilityEffectData.dragTicks().isEmpty()) {
                abilityEffectData.withDragTicks(Optional.of(dragTicks)).save(performer, true);
                return false;
            }
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
        return AbilitySerializers.DRAG.get();
    }
}
