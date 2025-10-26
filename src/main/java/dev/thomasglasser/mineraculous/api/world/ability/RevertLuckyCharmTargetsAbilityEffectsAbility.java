package dev.thomasglasser.mineraculous.api.world.ability;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
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
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTriggerData;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
                Pair<Table<ResourceKey<Level>, Vec3, List<CompoundTag>>, Multimap<ResourceKey<Level>, BlockPos>> positions = gatherReversionPositions(level, toRevert);
                Multimap<ResourceKey<Level>, BlockPos> blockPositions = positions.second;
                // Final positions after recovery, where they were originally at when affected
                Table<ResourceKey<Level>, Vec3, List<CompoundTag>> entityPositions = positions.first;
                Map<Vec3, List<CompoundTag>> row = entityPositions.row(level.dimension());
                ArrayList<MiraculousLadybugTargetData.EntityTarget> entityTargets = new ArrayList<>();
                Set<UUID> seen = new HashSet<>();
                for (Map.Entry<Vec3, List<CompoundTag>> entry : row.entrySet()) {
                    for (CompoundTag tag : row.get(entry.getKey())) {
                        UUID entityId = tag.getUUID("UUID");
                        if (!seen.add(entityId)) continue;

                        ListTag pos = tag.getList("Pos", ListTag.TAG_DOUBLE);
                        double x = pos.getDouble(0);
                        double y = pos.getDouble(1);
                        double z = pos.getDouble(2);

                        Entity newEntity = EntityType.loadEntityRecursive(tag, level, e -> e);
                        Vec3 currentPosition = newEntity != null ? new Vec3(x, y, z) : entry.getKey();
                        float width = newEntity != null ? newEntity.getBbWidth() : 1f;
                        float height = newEntity != null ? newEntity.getBbHeight() : 2f;

                        entityTargets.add(new MiraculousLadybugTargetData.EntityTarget(currentPosition, width, height));
                    }
                }
                //TODO treat other dimensions as well (ill just spawn particles cuz lazy)
                ResourceKey<Level> currentLevelKey = level.dimension();
                blockPositions = MineraculousMathUtils.reduceNearbyBlocks(blockPositions);
                ArrayList<BlockPos> blockTargets = new ArrayList<>(blockPositions.get(currentLevelKey));
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

    private static Pair<Table<ResourceKey<Level>, Vec3, List<CompoundTag>>, Multimap<ResourceKey<Level>, BlockPos>> gatherReversionPositions(ServerLevel level, Set<UUID> toRevert) {
        Table<ResourceKey<Level>, Vec3, List<CompoundTag>> entityPositions = HashBasedTable.create();
        Multimap<ResourceKey<Level>, BlockPos> blockPositions = ArrayListMultimap.create();
        AbilityReversionEntityData entityData = AbilityReversionEntityData.get(level);
        for (UUID relatedId : toRevert) {
            if (level.getEntity(relatedId) instanceof LivingEntity) {
                Multimap<ResourceKey<Level>, BlockPos> relatedBlockPositions = AbilityReversionBlockData.get(level).getReversionPositions(relatedId);
                blockPositions.putAll(relatedBlockPositions);
                for (Map.Entry<ResourceKey<Level>, Vec3> entry : entityData.getReversionPositions(relatedId).entries()) {
                    entityPositions.put(entry.getKey(), entry.getValue(), entityData.getRevertibleAt(relatedId, entry.getKey(), entry.getValue()));
                }

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
