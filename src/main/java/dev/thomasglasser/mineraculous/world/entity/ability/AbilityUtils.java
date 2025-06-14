package dev.thomasglasser.mineraculous.world.entity.ability;

import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.entity.ability.context.BlockAbilityContext;
import dev.thomasglasser.mineraculous.world.entity.ability.context.EntityAbilityContext;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class AbilityUtils {
    public static boolean performPassiveAbilities(ServerLevel level, Entity entity, AbilityData abilityData, @Nullable AbilityContext abilityContext, HolderSet<Ability> passiveAbilities) {
        boolean overrideActive = false;
        for (Holder<Ability> abilityHolder : passiveAbilities) {
            Ability ability = abilityHolder.value();
            if (ability.perform(abilityData, level, entity, abilityContext)) {
                overrideActive = true;
                break;
            }
        }
        return overrideActive;
    }

    public static boolean performActiveAbility(ServerLevel level, Entity entity, AbilityData abilityData, @Nullable AbilityContext abilityContext, Optional<Holder<Ability>> activeAbility) {
        if (abilityData.powerActive()) {
            return activeAbility.map(ability -> ability.value().perform(abilityData, level, entity, abilityContext)).orElse(false);
        }
        return false;
    }

    public static void performEntityAbilities(Entity performer, ServerLevel level, Entity target) {
        performAbilitiesInternal(performer, level, new EntityAbilityContext(target));
    }

    public static void performBlockAbilities(Entity performer, ServerLevel level, BlockPos pos) {
        performAbilitiesInternal(performer, level, new BlockAbilityContext(pos));
    }

    private static void performAbilitiesInternal(Entity performer, ServerLevel level, @Nullable AbilityContext abilityContext) {
        MiraculousesData miraculousesData = performer.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        miraculousesData.getTransformed().forEach(miraculous -> miraculousesData.get(miraculous).performAbilities(level, performer, miraculous, abilityContext));
        performer.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> data.performAbilities(level, performer, abilityContext));
    }
}
