package dev.thomasglasser.mineraculous.api.world.ability;

import dev.thomasglasser.mineraculous.api.event.AbilityEvent;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.context.BlockAbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.context.EntityAbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public class AbilityUtils {
    public static Ability.State performAbilityWithEvents(Holder<Ability> ability, ServerLevel level, LivingEntity performer, AbilityData abilityData, AbilityHandler handler, @Nullable AbilityContext context) {
        Ability.State state = NeoForge.EVENT_BUS.post(new AbilityEvent.Perform.Pre(performer, ability, abilityData, handler, context)).getState();
        if (state != null)
            return state;
        state = ability.value().perform(abilityData, level, performer, handler, context);
        return NeoForge.EVENT_BUS.post(new AbilityEvent.Perform.Post(performer, ability, abilityData, handler, context, state)).getState();
    }

    /**
     * Performs all provided abilities with the given context until one consumes.
     *
     * @param level            The level to perform the abilities in
     * @param performer        The performer of the abilities
     * @param data             The relevant performer {@link AbilityData}
     * @param handler          The relevant {@link AbilityHandler}
     * @param context          The context of the abilities
     * @param passiveAbilities The abilities to perform
     * @return The resulting {@link Ability.State} of the performed abilities
     */
    public static Ability.State performPassiveAbilities(ServerLevel level, LivingEntity performer, AbilityData data, AbilityHandler handler, @Nullable AbilityContext context, HolderSet<Ability> passiveAbilities) {
        for (Holder<Ability> ability : passiveAbilities) {
            Ability.State state = performAbilityWithEvents(ability, level, performer, data, handler, context);
            if (state.shouldStop()) {
                return state;
            }
        }
        return Ability.State.PASS;
    }

    /**
     * Performs the provided ability if power is active.
     *
     * @param level         The level to perform the ability in
     * @param performer     The performer of the ability
     * @param abilityData   The relevant performer {@link AbilityData}
     * @param handler       The relevant {@link AbilityHandler}
     * @param context       The context of the ability
     * @param activeAbility The ability to perform
     * @return The resulting {@link Ability.State} of the performed ability,
     *         or {@link Ability.State#CANCEL} if power is inactive.
     */
    public static Ability.State performActiveAbility(ServerLevel level, LivingEntity performer, AbilityData abilityData, AbilityHandler handler, @Nullable AbilityContext context, Optional<Holder<Ability>> activeAbility) {
        if (abilityData.powerActive()) {
            return activeAbility.map(ability -> performAbilityWithEvents(ability, level, performer, abilityData, handler, context)).orElse(Ability.State.CANCEL);
        }
        return Ability.State.CANCEL;
    }

    /**
     * Performs {@link Miraculous} and {@link Kamikotization} abilities with an {@link EntityAbilityContext} of the provided target.
     *
     * @param level     The level to perform the abilities in
     * @param performer The performer of the abilities
     * @param target    The target to make the context for
     */
    public static void performEntityAbilities(ServerLevel level, LivingEntity performer, Entity target) {
        performAbilitiesInternal(level, performer, new EntityAbilityContext(target));
    }

    /**
     * Performs {@link Miraculous} and {@link Kamikotization} abilities with a {@link BlockAbilityContext} of the provided position.
     *
     * @param level     The level to perform the abilities in
     * @param performer The performer of the abilities
     * @param pos       The position to make the context for
     */
    public static void performBlockAbilities(ServerLevel level, LivingEntity performer, BlockPos pos) {
        performAbilitiesInternal(level, performer, new BlockAbilityContext(pos));
    }

    private static void performAbilitiesInternal(ServerLevel level, LivingEntity performer, @Nullable AbilityContext context) {
        MiraculousesData miraculousesData = performer.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        miraculousesData.getTransformed().forEach(miraculous -> miraculousesData.get(miraculous).performAbilities(level, performer, miraculous, context));
        performer.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> data.performAbilities(level, performer, context));
    }
}
