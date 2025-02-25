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
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharm;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharmIdDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousRecoveryDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.loot.parameters.MineraculousLootContextParamSets;
import dev.thomasglasser.mineraculous.world.level.storage.loot.parameters.MineraculousLootContextParams;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record SummonLuckyCharmAbility(boolean requireTool, Optional<Holder<SoundEvent>> startSound, boolean overrideActive) implements Ability {

    public static final MapCodec<SummonLuckyCharmAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("require_tool", false).forGetter(SummonLuckyCharmAbility::requireTool),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(SummonLuckyCharmAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(SummonLuckyCharmAbility::overrideActive)).apply(instance, SummonLuckyCharmAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Context context) {
        if (context == Context.PASSIVE) {
            UUID tracked = ((MiraculousRecoveryDataHolder) level.getServer().overworld()).mineraculous$getMiraculousRecoveryEntityData().getTrackedEntity(entity.getUUID());
            LivingEntity trackedEntity = tracked != null ? level.getEntity(tracked) instanceof LivingEntity livingEntity ? livingEntity : null : null;
            LivingEntity target = trackedEntity != null ? trackedEntity : entity.getKillCredit() != null ? entity.getKillCredit() : entity.getLastHurtByMob() != null ? entity.getLastHurtByMob() : entity.getLastHurtMob();
            if (target instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null)
                target = level.getEntity(ownable.getOwnerUUID()) instanceof LivingEntity livingEntity ? livingEntity : target;
            if (target != null)
                ((MiraculousRecoveryDataHolder) level.getServer().overworld()).mineraculous$getMiraculousRecoveryEntityData().putRelatedEntity(target.getUUID(), entity.getUUID());
            LuckyCharms charms = getCharms(level, target);
            AtomicReference<ItemStack> result = new AtomicReference<>();
            if (charms.items().left().isPresent()) {
                LootTable loottable = level.getServer().reloadableRegistries().getLootTable(charms.items().left().get());
                LootParams.Builder lootparams$builder = new LootParams.Builder(level)
                        .withParameter(LootContextParams.THIS_ENTITY, entity)
                        .withParameter(LootContextParams.ORIGIN, entity.position())
                        .withParameter(LootContextParams.TOOL, entity.getMainHandItem())
                        .withParameter(MineraculousLootContextParams.POWER_LEVEL, data.powerLevel())
                        .withOptionalParameter(LootContextParams.DAMAGE_SOURCE, entity.getLastDamageSource())
                        .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, target);

                LootParams lootparams = lootparams$builder.create(MineraculousLootContextParamSets.LUCKY_CHARM);
                loottable.getRandomItems(lootparams, target.getLootTableSeed(), result::set);
            } else {
                Optional<Holder<Item>> item = charms.items().right().get().getRandomElement(level.random);
                item.ifPresent(itemHolder -> result.set(itemHolder.value().getDefaultInstance()));
            }
            if (result.get() == null) {
                result.set(BuiltInRegistries.ITEM.getTag(MineraculousItemTags.GENERIC_LUCKY_CHARMS).orElseThrow().getRandomElement(level.random).orElseThrow().value().getDefaultInstance());
            }
            ItemStack toAdd = result.get();
            UUID uuid;
            if (data.power().left().isPresent()) {
                uuid = entity.getData(MineraculousAttachmentTypes.MIRACULOUS).get(data.power().left().get()).miraculousItem().get(MineraculousDataComponents.KWAMI_DATA).uuid();
                toAdd.set(MineraculousDataComponents.KWAMI_DATA, new KwamiData(uuid, false));
            } else {
                uuid = entity.getUUID();
                toAdd.set(MineraculousDataComponents.KAMIKOTIZATION, data.power().right().get());
            }
            toAdd.set(MineraculousDataComponents.LUCKY_CHARM, new LuckyCharm(Optional.ofNullable(target != null ? target.getUUID() : null), ((LuckyCharmIdDataHolder) level.getServer().overworld()).mineraculous$getLuckyCharmIdData().incrementLuckyCharmId(uuid)));
            LuckyCharmItemSpawner item = LuckyCharmItemSpawner.create(level, toAdd);
            item.setPos(entity.position().add(0, 4, 0));
            level.addFreshEntity(item);
            playStartSound(level, pos);
            return true;
        }
        return false;
    }

    private LuckyCharms getCharms(ServerLevel level, LivingEntity target) {
        if (target != null) {
            if (target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
                LuckyCharms data = level.registryAccess().registryOrThrow(MineraculousRegistries.KAMIKOTIZATION).getData(MineraculousDataMaps.KAMIKOTIZATION_LUCKY_CHARMS, target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).get().kamikotization());
                if (data != null)
                    return data;
            }
            if (target.getData(MineraculousAttachmentTypes.MIRACULOUS).isTransformed()) {
                List<ResourceKey<Miraculous>> transformed = target.getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed();
                LuckyCharms data = level.registryAccess().registryOrThrow(MineraculousRegistries.MIRACULOUS).getData(MineraculousDataMaps.MIRACULOUS_LUCKY_CHARMS, transformed.get(level.random.nextInt(transformed.size())));
                if (data != null)
                    return data;
            }
            LuckyCharms data = BuiltInRegistries.ENTITY_TYPE.getData(MineraculousDataMaps.ENTITY_LUCKY_CHARMS, target.getType().builtInRegistryHolder().key());
            if (data != null)
                return data;
        }
        return new LuckyCharms(Either.right(BuiltInRegistries.ITEM.getTag(MineraculousItemTags.GENERIC_LUCKY_CHARMS).orElseThrow()));
    }

    @Override
    public boolean canActivate(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {
        if (requireTool) {
            ItemStack mainHandItem = entity.getMainHandItem();
            Either<ResourceKey<Miraculous>, ResourceKey<Kamikotization>> power = data.power();
            if (power.left().isPresent()) {
                Integer toolId = mainHandItem.get(MineraculousDataComponents.TOOL_ID);
                return toolId != null && toolId == entity.getData(MineraculousAttachmentTypes.MIRACULOUS).get(power.left().get()).toolId() && mainHandItem.has(MineraculousDataComponents.ACTIVE);
            } else {
                return mainHandItem.get(MineraculousDataComponents.KAMIKOTIZATION) == power.right().get() && mainHandItem.has(MineraculousDataComponents.ACTIVE);
            }
        }
        return true;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.SUMMON_LUCKY_CHARM.get();
    }
}
