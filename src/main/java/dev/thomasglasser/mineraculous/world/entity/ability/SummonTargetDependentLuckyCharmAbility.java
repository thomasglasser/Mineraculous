package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.datamaps.LuckyCharms;
import dev.thomasglasser.mineraculous.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.LuckyCharmItemSpawner;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharm;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharmIdData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.mineraculous.world.level.storage.loot.parameters.MineraculousLootContextParamSets;
import dev.thomasglasser.mineraculous.world.level.storage.loot.parameters.MineraculousLootContextParams;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

public record SummonTargetDependentLuckyCharmAbility(boolean requireActiveToolInHand, Optional<Holder<SoundEvent>> summonSound) implements Ability {
    public static final MapCodec<SummonTargetDependentLuckyCharmAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("require_active_tool_in_hand", false).forGetter(SummonTargetDependentLuckyCharmAbility::requireActiveToolInHand),
            SoundEvent.CODEC.optionalFieldOf("summon_sound").forGetter(SummonTargetDependentLuckyCharmAbility::summonSound)).apply(instance, SummonTargetDependentLuckyCharmAbility::new));

    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        boolean toolInHand = false;
        if (requireActiveToolInHand && performer instanceof LivingEntity livingEntity) {
            ItemStack stack = livingEntity.getMainHandItem();
            toolInHand = data.power().map(miraculous -> {
                KwamiData stackKwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA);
                return miraculous.value().tool().is(stack.getItem()) && stackKwamiData != null && performer.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).kwamiData().map(kwamiData -> kwamiData.uuid().equals(stackKwamiData.uuid())).orElse(false) && stack.getOrDefault(MineraculousDataComponents.ACTIVE, true);
            }, kamikotization -> kamikotization == stack.get(MineraculousDataComponents.KAMIKOTIZATION) && stack.getOrDefault(MineraculousDataComponents.ACTIVE, true));
        }
        if (!requireActiveToolInHand || toolInHand) {
            AbilityReversionEntityData entityData = AbilityReversionEntityData.get(level);
            Entity target = determineTarget(level, entityData.getTrackedEntity(performer.getUUID()), performer);
            if (target != null) {
                entityData.putRelatedEntity(performer.getUUID(), target.getUUID());
                entityData.putRelatedEntity(target.getUUID(), performer.getUUID());
            }
            ItemStack stack = getLuckyCharms(level, target).items().map(lootTable -> {
                LootParams.Builder paramsBuilder = new LootParams.Builder(level)
                        .withParameter(LootContextParams.THIS_ENTITY, performer)
                        .withParameter(LootContextParams.ORIGIN, performer.position())
                        .withParameter(MineraculousLootContextParams.POWER_LEVEL, data.powerLevel())
                        .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, target);

                if (performer instanceof LivingEntity livingEntity) {
                    paramsBuilder = paramsBuilder
                            .withOptionalParameter(LootContextParams.TOOL, livingEntity.getMainHandItem())
                            .withOptionalParameter(LootContextParams.DAMAGE_SOURCE, livingEntity.getLastDamageSource());
                }

                LootParams params = paramsBuilder
                        .create(MineraculousLootContextParamSets.LUCKY_CHARM);

                LootTable table = level.getServer().reloadableRegistries().getLootTable(lootTable);
                ObjectArrayList<ItemStack> stacks = target instanceof LivingEntity livingEntity ? table.getRandomItems(params, livingEntity.getLootTableSeed()) : table.getRandomItems(params);
                return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(level.random.nextInt(stacks.size()));
            }, set -> set.getRandomElement(level.random).map(item -> item.value().getDefaultInstance()).orElse(ItemStack.EMPTY));
            if (stack.isEmpty()) {
                stack = BuiltInRegistries.ITEM.getTag(MineraculousItemTags.GENERIC_LUCKY_CHARMS).orElseThrow().getRandomElement(level.random).orElseThrow().value().getDefaultInstance();
            }
            ItemStack result = stack;
            UUID uuid = data.power().map(miraculous -> {
                KwamiData kwamiData = performer.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).kwamiData().orElse(null);
                result.set(MineraculousDataComponents.KWAMI_DATA, kwamiData);
                return kwamiData != null ? kwamiData.uuid() : null;
            }, kamikotization -> {
                result.set(MineraculousDataComponents.KAMIKOTIZATION, kamikotization);
                return performer.getUUID();
            });
            result.set(MineraculousDataComponents.LUCKY_CHARM, new LuckyCharm(Optional.ofNullable(target).map(Entity::getUUID), uuid, uuid != null ? LuckyCharmIdData.get(level).incrementLuckyCharmId(uuid) : 0));
            LuckyCharmItemSpawner item = LuckyCharmItemSpawner.create(level, result);
            item.setPos(performer.position().add(0, 4, 0));
            level.addFreshEntity(item);
            Ability.playSound(level, performer, summonSound);
            return true;
        }
        return false;
    }

    private @Nullable Entity determineTarget(ServerLevel level, @Nullable UUID trackedId, Entity performer) {
        Entity target = trackedId != null ? level.getEntity(trackedId) : null;
        if (performer instanceof LivingEntity livingEntity) {
            if (target == null) {
                target = livingEntity.getKillCredit();
            }
            if (target == null) {
                target = livingEntity.getLastHurtMob();
            }
        }
        if (target instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            Entity owner = level.getEntity(ownable.getOwnerUUID());
            if (owner != null) {
                target = owner;
            }
        }
        return target;
    }

    private LuckyCharms getLuckyCharms(ServerLevel level, Entity target) {
        if (target != null) {
            Optional<Holder<Kamikotization>> kamikotization = target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).map(KamikotizationData::kamikotization);
            if (kamikotization.isPresent()) {
                LuckyCharms data = level.registryAccess().registryOrThrow(MineraculousRegistries.KAMIKOTIZATION).getData(MineraculousDataMaps.KAMIKOTIZATION_LUCKY_CHARMS, kamikotization.get().getKey());
                if (data != null) {
                    return data;
                }
            }
            MiraculousesData miraculousesData = target.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            if (miraculousesData.isTransformed()) {
                Holder<Miraculous> miraculous = miraculousesData.getTransformed().getFirst();
                LuckyCharms data = level.registryAccess().registryOrThrow(MineraculousRegistries.MIRACULOUS).getData(MineraculousDataMaps.MIRACULOUS_LUCKY_CHARMS, miraculous.getKey());
                if (data != null) {
                    return data;
                }
            }
            LuckyCharms data = BuiltInRegistries.ENTITY_TYPE.getData(MineraculousDataMaps.ENTITY_LUCKY_CHARMS, target.getType().builtInRegistryHolder().key());
            if (data != null) {
                return data;
            }
        }
        return new LuckyCharms(Either.right(BuiltInRegistries.ITEM.getTag(MineraculousItemTags.GENERIC_LUCKY_CHARMS).orElseThrow()));
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.SUMMON_TARGET_DEPENDENT_LUCKY_CHARM.get();
    }
}
