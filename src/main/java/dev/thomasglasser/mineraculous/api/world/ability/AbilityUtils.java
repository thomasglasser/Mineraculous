package dev.thomasglasser.mineraculous.api.world.ability;

import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.context.BlockAbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.context.EntityAbilityContext;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.api.world.level.storage.MiraculousesData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class AbilityUtils {
    /**
     * Performs all provided abilities with the given context until one consumes.
     * 
     * @param level            The level to perform the abilities in
     * @param performer        The performer of the abilities
     * @param abilityData      The relevant performer {@link AbilityData}
     * @param abilityContext   The context of the abilities
     * @param passiveAbilities The abilities to perform
     * @return Whether the passive abilities consumed (i.e., they overrode the active ability)
     */
    public static boolean performPassiveAbilities(ServerLevel level, Entity performer, AbilityData abilityData, @Nullable AbilityContext abilityContext, HolderSet<Ability> passiveAbilities) {
        for (Holder<Ability> ability : passiveAbilities) {
            if (ability.value().perform(abilityData, level, performer, abilityContext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs the provided ability if power is active.
     * 
     * @param level          The level to perform the ability in
     * @param performer      The performer of the ability
     * @param abilityData    The relevant performer {@link AbilityData}
     * @param abilityContext The context of the ability
     * @param activeAbility  The ability to perform
     * @return Whether the active ability consumed (i.e., was used up)
     */
    public static boolean performActiveAbility(ServerLevel level, Entity performer, AbilityData abilityData, @Nullable AbilityContext abilityContext, Optional<Holder<Ability>> activeAbility) {
        if (abilityData.powerActive()) {
            return activeAbility.map(ability -> ability.value().perform(abilityData, level, performer, abilityContext)).orElse(false);
        }
        return false;
    }

    /**
     * Performs {@link Miraculous} and {@link Kamikotization} abilities with an {@link EntityAbilityContext} of the provided target.
     *
     * @param level     The level to perform the abilities in
     * @param performer The performer of the abilities
     * @param target    The target to make the context for
     */
    public static void performEntityAbilities(ServerLevel level, Entity performer, Entity target) {
        performAbilitiesInternal(level, performer, new EntityAbilityContext(target));
    }

    /**
     * Performs {@link Miraculous} and {@link Kamikotization} abilities with a {@link BlockAbilityContext} of the provided {@link BlockPos}.
     *
     * @param level     The level to perform the abilities in
     * @param performer The performer of the abilities
     * @param pos       The {@link BlockPos} to make the context for
     */
    public static void performBlockAbilities(ServerLevel level, Entity performer, BlockPos pos) {
        performAbilitiesInternal(level, performer, new BlockAbilityContext(pos));
    }

    private static void performAbilitiesInternal(ServerLevel level, Entity performer, @Nullable AbilityContext abilityContext) {
        MiraculousesData miraculousesData = performer.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        miraculousesData.getTransformed().forEach(miraculous -> miraculousesData.get(miraculous).performAbilities(level, performer, miraculous, abilityContext));
        performer.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> data.performAbilities(level, performer, abilityContext));
    }
}
