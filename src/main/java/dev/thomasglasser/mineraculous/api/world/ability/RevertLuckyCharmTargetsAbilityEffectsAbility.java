package dev.thomasglasser.mineraculous.api.world.ability;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ibm.icu.impl.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTriggerData;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
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
        boolean validUsage = isValidLuckyCharmUse(data, context, performer, handler);
        if (validUsage) {
            ItemStack stack = performer.getMainHandItem();
            LuckyCharm luckyCharm = stack.get(MineraculousDataComponents.LUCKY_CHARM);
            UUID performerId = handler.getMatchingBlame(stack, performer);
            luckyCharm.target().ifPresent(target -> {
                AbilityReversionEntityData entityData = AbilityReversionEntityData.get(level);
                Set<UUID> toRevert = new ReferenceOpenHashSet<>();
                toRevert.add(performerId);
                collectToRevert(target, entityData, toRevert);
                Pair<Multimap<ResourceKey<Level>, Vec3>, Multimap<ResourceKey<Level>, BlockPos>> positions = gatherReversionPositions(level, toRevert);
                Multimap<ResourceKey<Level>, BlockPos> blockPositions = positions.second;
                Multimap<ResourceKey<Level>, Vec3> entityPositions = positions.first;
                //TODO treat other dimensions as well
                ResourceKey<Level> currentLevelKey = level.dimension();
                blockPositions = MineraculousMathUtils.reduceNearbyBlocks(blockPositions);
                List<BlockPos> blockTargets = new ArrayList<>(blockPositions.get(currentLevelKey));
                List<Vec3> entityTargets = new ArrayList<>(entityPositions.get(currentLevelKey));
                ItemEntity luckyCharmEntity = new ItemEntity(level, performer.getX(), performer.getY() + 2, performer.getZ(), stack);
                luckyCharmEntity.setNeverPickUp();
                luckyCharmEntity.setUnlimitedLifetime();
                level.addFreshEntity(luckyCharmEntity);
                performer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                luckyCharmEntity.setDeltaMovement(0, 1.3, 0);
                luckyCharmEntity.hurtMarked = true;
                MiraculousLadybugTriggerData triggerData = new MiraculousLadybugTriggerData(blockTargets, entityTargets, Optional.of(performer.getId()), revertSound);
                triggerData.save(luckyCharmEntity, true);
            });
            return State.SUCCESS;
        }
        return State.FAIL;
    }

    private static Pair<Multimap<ResourceKey<Level>, Vec3>, Multimap<ResourceKey<Level>, BlockPos>> gatherReversionPositions(ServerLevel level, Set<UUID> toRevert) {
        Multimap<ResourceKey<Level>, Vec3> entityPositions = ArrayListMultimap.create();
        Multimap<ResourceKey<Level>, BlockPos> blockPositions = ArrayListMultimap.create();
        AbilityReversionEntityData entityData = AbilityReversionEntityData.get(level);
        for (UUID relatedId : toRevert) {
            if (level.getEntity(relatedId) instanceof LivingEntity) {
                Multimap<ResourceKey<Level>, Vec3> relatedEntityPositions = entityData.getReversionPositions(relatedId);
                Multimap<ResourceKey<Level>, BlockPos> relatedBlockPositions = AbilityReversionBlockData.get(level).getReversionPositions(relatedId);
                entityPositions.putAll(relatedEntityPositions);
                blockPositions.putAll(relatedBlockPositions);

                // TODO: Move this to ML when the actual reversion happens
//                related.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).map(KamikotizationData::kamikotization).or(() -> related.getData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION)).ifPresent(kamikotization -> {
//                    Kamikotization value = kamikotization.value();
//                    AbilityData abilityData = new AbilityData(0, false);
//                    value.powerSource().ifLeft(tool -> {
//                        if (tool.getItem() instanceof EffectRevertingItem item) {
//                            item.revert(related);
//                        }
//                    }).ifRight(ability -> ability.value().revert(abilityData, level, related, ));
//                    value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related, ));
//                });
//                MiraculousesData miraculousesData = related.getData(MineraculousAttachmentTypes.MIRACULOUSES);
//                for (Holder<Miraculous> miraculous : miraculousesData.keySet()) {
//                    Miraculous value = miraculous.value();
//                    AbilityData abilityData = new AbilityData(miraculousesData.get(miraculous).powerLevel(), false);
//                    value.activeAbility().value().revert(abilityData, level, related, );
//                    value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related, ));
//                }
            }
        }
        return Pair.of(entityPositions, blockPositions);
    }

    private static boolean isValidLuckyCharmUse(AbilityData data, @Nullable AbilityContext context, LivingEntity performer, AbilityHandler handler) {
        if (context == null && data.powerActive()) {
            ItemStack stack = performer.getMainHandItem();
            LuckyCharm luckyCharm = stack.get(MineraculousDataComponents.LUCKY_CHARM);
            if (luckyCharm != null) {
                UUID performerId = handler.getMatchingBlame(stack, performer);
                if (luckyCharm.owner().equals(performerId)) {
                    return true;
                }
            }
        }
        return false;
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
}
