package dev.thomasglasser.mineraculous.api.world.ability;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.MiraculousLadybug;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LuckyCharmIdData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
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
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Reverts the ability effects of the {@link LuckyCharm} target and related entities.
 *
 * @param revertSound The sound to play when reverting ability effects
 */
public record RevertLuckyCharmTargetsAbilityEffectsAbility(Optional<Holder<SoundEvent>> revertSound) implements Ability {
    public static final MapCodec<RevertLuckyCharmTargetsAbilityEffectsAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SoundEvent.CODEC.optionalFieldOf("revert_sound").forGetter(RevertLuckyCharmTargetsAbilityEffectsAbility::revertSound)).apply(instance, RevertLuckyCharmTargetsAbilityEffectsAbility::new));

    public static final int MIRACULOUS_LADYBUGS_COUNT = 8;

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
                        Multimap<ResourceKey<Level>, Vec3> entityPositions = ArrayListMultimap.create();
                        Multimap<ResourceKey<Level>, BlockPos> blockPositions = ArrayListMultimap.create();
                        for (UUID relatedId : toRevert) {
                            if (level.getEntity(relatedId) instanceof LivingEntity) {
                                Multimap<ResourceKey<Level>, Vec3> relatedEntityPositions = entityData.getReversionPositions(relatedId);
                                Multimap<ResourceKey<Level>, BlockPos> relatedBlockPositions = AbilityReversionBlockData.get(level).getReversionPositions(relatedId);
                                entityPositions.putAll(relatedEntityPositions);
                                blockPositions.putAll(relatedBlockPositions);

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
                        ResourceKey<Level> currentLevelKey = level.dimension();
                        blockPositions = MineraculousMathUtils.reduceNearbyBlocks(blockPositions);
                        List<BlockPos> blockTargets = new ArrayList<>(blockPositions.get(currentLevelKey));
                        List<Vec3> entityTargets = new ArrayList<>(entityPositions.get(currentLevelKey));
                        int blockTasks = blockTargets.size();
                        int entityTasks = entityTargets.size();

                        //List<MiraculousLadybug> miraculousLadybugs = new ArrayList<>();
                        ArrayList<MiraculousLadybugTargetData> taskTable = new ArrayList<>();
                        int taskIndex = -1;
                        if (blockTasks > 0) {
                            int tasksPerEntity = blockTasks / MIRACULOUS_LADYBUGS_COUNT;
                            int remainder = blockTasks % MIRACULOUS_LADYBUGS_COUNT;
                            int maxCount = Math.min(blockTasks, MIRACULOUS_LADYBUGS_COUNT);
                            int start = 0;
                            for (int i = 0; i < maxCount; i++) { //in case the remainder is smaller than 8, entities with no task will be completely discarded
                                //MiraculousLadybug miraculousLadybug = new MiraculousLadybug(level);
                                //miraculousLadybug.setPos(performer.getX(), performer.getY() + 5, performer.getZ()); //TODO check if y+5 is air, otherwise continue to decrement
                                int end = start + tasksPerEntity + (i < remainder ? 1 : 0);
                                List<BlockPos> subTargets = blockTargets.subList(start, Math.min(end, blockTasks));
                                //level.addFreshEntity(miraculousLadybug);
                                taskTable.add(new MiraculousLadybugTargetData(subTargets, null));
                                taskIndex++;
                                //targetData.save(miraculousLadybug, true);
                                start = end;
                            }
                        }
                        taskIndex++;
                        taskIndex = taskIndex >= MIRACULOUS_LADYBUGS_COUNT ? taskIndex % MIRACULOUS_LADYBUGS_COUNT : taskIndex; //task index must belong to {0, 1, 2 ... 7}
                        if (entityTasks > 0) {
                            int tasksPerEntity = entityTasks / MIRACULOUS_LADYBUGS_COUNT;
                            int remainder = entityTasks % MIRACULOUS_LADYBUGS_COUNT;
                            int maxCount = Math.min(entityTasks, MIRACULOUS_LADYBUGS_COUNT);
                            int start = 0;
                            for (int i = 0; i < maxCount; i++) { //in case the remainder is smaller than 8, entities with no task will be completely discarded
                                int end = start + tasksPerEntity + (i < remainder ? 1 : 0);
                                List<Vec3> subTargets = entityTargets.subList(start, Math.min(end, blockTasks));
                                if (taskIndex < taskTable.size()) {
                                    taskTable.set(taskIndex, taskTable.get(taskIndex).withEntityTargets(subTargets));
                                } else {
                                    taskTable.add(new MiraculousLadybugTargetData(null, null));
                                    taskIndex++;
                                    taskIndex = taskIndex >= MIRACULOUS_LADYBUGS_COUNT ? taskIndex % MIRACULOUS_LADYBUGS_COUNT : taskIndex;
                                }
                                start = end;
                            }
                        }

                        ArrayList<Vector2d> circle = MineraculousMathUtils.generateCirclePoints(13, taskTable.size());
                        //TODO merge and then move the next for in MathUtils and then replace it in summontargetdependentluckycharm
                        Vec3 spawnPos = performer.position();
                        for (int i = 0; i <= 5; i++) {
                            Vec3 above = spawnPos.add(0, 1, 0);
                            if (level.getBlockState(new BlockPos(MineraculousMathUtils.getVec3i(above))).isAir()) {
                                spawnPos = above;
                            } else {
                                break;
                            }
                        }
                        for (int i = 0; i < taskTable.size(); i++) {
                            MiraculousLadybugTargetData task = taskTable.get(i);
                            List<BlockPos> updatedBlockTargets = task.blockTargets();
                            int x = (int) circle.get(i).x;
                            int y = (int) circle.get(i).y;
                            Vec3 targetPos = spawnPos.add(x, 0, y);
                            BlockPos newTarget = new BlockPos(MineraculousMathUtils.getVec3i(targetPos));
                            updatedBlockTargets.add(new BlockPos(newTarget));
                            task = task.withBlockTargets(updatedBlockTargets);
                            MiraculousLadybug miraculousLadybug = new MiraculousLadybug(level);
                            miraculousLadybug.setPos(spawnPos);
                            level.addFreshEntity(miraculousLadybug);
                            task.save(miraculousLadybug, true);
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
