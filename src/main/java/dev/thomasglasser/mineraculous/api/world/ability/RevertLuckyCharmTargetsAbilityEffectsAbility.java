package dev.thomasglasser.mineraculous.api.world.ability;

import com.google.common.collect.Multimap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.MiraculousLadybug;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LuckyCharmIdData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Reverts the ability effects of the {@link LuckyCharm} target and related entities.
 *
 * @param revertSound The sound to play when reverting ability effects
 */
public record RevertLuckyCharmTargetsAbilityEffectsAbility(Optional<Holder<SoundEvent>> revertSound) implements Ability {
    public static final MapCodec<RevertLuckyCharmTargetsAbilityEffectsAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SoundEvent.CODEC.optionalFieldOf("revert_sound").forGetter(RevertLuckyCharmTargetsAbilityEffectsAbility::revertSound)).apply(instance, RevertLuckyCharmTargetsAbilityEffectsAbility::new));

    @Override
    public State perform(AbilityData data, ServerLevel level, LivingEntity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        if (context == null && data.powerActive()) {
            ItemStack stack = performer.getMainHandItem();
            LuckyCharm luckyCharm = stack.get(MineraculousDataComponents.LUCKY_CHARM);
            if (luckyCharm != null) {
                UUID performerId = handler.getMatchingBlame(stack, performer);
                if (luckyCharm.owner().equals(performerId)) {
                    luckyCharm.target().ifPresent(target -> {
                        AbilityReversionEntityData entityData = AbilityReversionEntityData.get(level);
                        Set<UUID> toRevert = new ReferenceOpenHashSet<>();
                        toRevert.add(performerId);
                        collectToRevert(target, entityData, toRevert);
                        for (UUID relatedId : toRevert) {
                            if (level.getEntity(relatedId) instanceof LivingEntity related) {
                                //TODO rework on this when adding the summoning visual
                                List<BlockPos> blockTargets = getBlockTargets(level, relatedId); //TEMPORARY, the final arrays should hold every related entity's revert data.
                                blockTargets = MineraculousMathUtils.reduceNearbyBlocks(blockTargets);
                                if (blockTargets != null && !blockTargets.isEmpty()) {
                                    MiraculousLadybug miraculousLadybug1 = new MiraculousLadybug(MineraculousEntityTypes.MIRACULOUS_LADYBUG.get(), level);
                                    miraculousLadybug1.setPos(performer.getX(), performer.getY() + 5, performer.getZ());
                                    MiraculousLadybugTargetData targetData = new MiraculousLadybugTargetData(blockTargets);
                                    level.addFreshEntity(miraculousLadybug1);
                                    targetData.save(miraculousLadybug1, true);
                                }

                                Multimap<ResourceKey<Level>, Vec3> entityPositions = entityData.getReversionPositions(relatedId);
                                Multimap<ResourceKey<Level>, BlockPos> blockPositions = AbilityReversionBlockData.get(level).getReversionPositions(relatedId);
                                // TODO: Do something with the positions

                                // TODO: Move this to ML when the actual reversion happens
//                                related.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).map(KamikotizationData::kamikotization).or(() -> related.getData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION)).ifPresent(kamikotization -> {
//                                    Kamikotization value = kamikotization.value();
//                                    AbilityData abilityData = new AbilityData(0, false);
//                                    value.powerSource().ifLeft(tool -> {
//                                        if (tool.getItem() instanceof EffectRevertingItem item) {
//                                            item.revert(related);
//                                        }
//                                    }).ifRight(ability -> ability.value().revert(abilityData, level, related, ));
//                                    value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related, ));
//                                });
//                                MiraculousesData miraculousesData = related.getData(MineraculousAttachmentTypes.MIRACULOUSES);
//                                for (Holder<Miraculous> miraculous : miraculousesData.keySet()) {
//                                    Miraculous value = miraculous.value();
//                                    AbilityData abilityData = new AbilityData(miraculousesData.get(miraculous).powerLevel(), false);
//                                    value.activeAbility().value().revert(abilityData, level, related, );
//                                    value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related, ));
//                                }
                            }
                        }
                    });
                    LuckyCharmIdData.get(level).incrementLuckyCharmId(performerId);
                    Ability.playSound(level, performer, revertSound);
                    return State.SUCCESS;
                }
            }
        }
        return State.FAIL;
    }

    private void collectToRevert(UUID uuid, AbilityReversionEntityData entityData, Set<UUID> toRevert) {
        for (UUID related : entityData.getAndClearTrackedAndRelatedEntities(uuid)) {
            if (!toRevert.contains(related)) {
                toRevert.add(related);
                collectToRevert(related, entityData, toRevert);
            }
        }
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.REVERT_LUCKY_CHARM_TARGETS_ABILITY_EFFECTS.get();
    }

    private static List<BlockPos> getBlockTargets(ServerLevel level, UUID relatedId) {
        AbilityReversionBlockData reversionBlockData = AbilityReversionBlockData.get(level);
        List<BlockPos> blockPositions = reversionBlockData.getRevertibleBlocks(relatedId);
        if (blockPositions == null) {
            return List.of();
        }
        return MineraculousMathUtils.reduceNearbyBlocks(blockPositions);
    }
}
