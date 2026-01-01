package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.datamaps.LuckyCharms;
import dev.thomasglasser.mineraculous.api.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.api.event.LuckyCharmEvent;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.level.storage.EntityReversionData;
import dev.thomasglasser.mineraculous.api.world.level.storage.loot.parameters.MineraculousLootContextParamSets;
import dev.thomasglasser.mineraculous.api.world.level.storage.loot.parameters.MineraculousLootContextParams;
import dev.thomasglasser.mineraculous.impl.world.entity.LuckyCharmItemSpawner;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LuckyCharmIdData;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

/**
 * Summons an {@link ItemStack} from a {@link LuckyCharms} pool based on related entities.
 *
 * @param requireActiveToolInHand     Whether the performer must have their tool in-hand to summon the lucky charm
 * @param disappearOnDetransformation Whether the lucky charm should disappear when the performer detransforms
 * @param summonSound                 The sound to play when summoning the lucky charm successfully
 */
public record SummonTargetDependentLuckyCharmAbility(boolean requireActiveToolInHand, boolean disappearOnDetransformation, Optional<Holder<SoundEvent>> summonSound) implements Ability {

    public static final MapCodec<SummonTargetDependentLuckyCharmAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("require_active_tool_in_hand", false).forGetter(SummonTargetDependentLuckyCharmAbility::requireActiveToolInHand),
            Codec.BOOL.optionalFieldOf("disappear_on_detransformation", false).forGetter(SummonTargetDependentLuckyCharmAbility::disappearOnDetransformation),
            SoundEvent.CODEC.optionalFieldOf("summon_sound").forGetter(SummonTargetDependentLuckyCharmAbility::summonSound)).apply(instance, SummonTargetDependentLuckyCharmAbility::new));
    @Override
    public State perform(AbilityData data, ServerLevel level, LivingEntity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        ItemStack mainHand = performer.getMainHandItem();
        ItemStack offHand = performer.getOffhandItem();
        ItemStack tool = null;
        if (handler.isActiveTool(mainHand, performer))
            tool = mainHand;
        else if (handler.isActiveTool(offHand, performer))
            tool = offHand;
        if (!requireActiveToolInHand || tool != null) {
            var event = NeoForge.EVENT_BUS.post(new LuckyCharmEvent.DetermineSpawnPos(performer, tool, defaultLuckyCharmSpawnPosition(level, performer)));
            if (event.isCanceled())
                return State.CANCEL;
            EntityReversionData entityData = EntityReversionData.get(level);
            Entity target = NeoForge.EVENT_BUS.post(new LuckyCharmEvent.DetermineTarget(performer, tool, determineTarget(level, performer))).getTarget();
            if (target != null) {
                entityData.putRelatedEntity(performer.getUUID(), target.getUUID());
                entityData.putRelatedEntity(target.getUUID(), performer.getUUID());
            }
            ItemStack stack = NeoForge.EVENT_BUS.post(new LuckyCharmEvent.DetermineLuckyCharms(performer, tool, target, getLuckyCharms(target))).getLuckyCharms().items().map(lootTable -> {
                LootParams.Builder paramsBuilder = new LootParams.Builder(level)
                        .withParameter(LootContextParams.THIS_ENTITY, performer)
                        .withParameter(LootContextParams.ORIGIN, performer.position())
                        .withParameter(MineraculousLootContextParams.POWER_LEVEL, data.powerLevel())
                        .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, target)
                        .withOptionalParameter(LootContextParams.TOOL, performer.getMainHandItem())
                        .withOptionalParameter(LootContextParams.DAMAGE_SOURCE, performer.getLastDamageSource());

                LootParams params = paramsBuilder
                        .create(MineraculousLootContextParamSets.LUCKY_CHARM);

                LootTable table = level.getServer().reloadableRegistries().getLootTable(lootTable);
                List<ItemStack> stacks = target instanceof LivingEntity livingEntity ? table.getRandomItems(params, livingEntity.getLootTableSeed()) : table.getRandomItems(params);
                return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(level.random.nextInt(stacks.size()));
            }, set -> set.getRandomElement(level.random).map(item -> item.value().getDefaultInstance()).orElse(ItemStack.EMPTY));
            if (stack.isEmpty()) {
                stack = BuiltInRegistries.ITEM.getTag(MineraculousItemTags.GENERIC_LUCKY_CHARMS).orElseThrow().getRandomElement(level.random).orElseThrow().value().getDefaultInstance();
            }
            stack.set(MineraculousDataComponents.LUCKY_CHARM, new LuckyCharm(Optional.ofNullable(target).map(Entity::getUUID), performer.getUUID(), LuckyCharmIdData.get(level).incrementLuckyCharmId(performer.getUUID())));
            LuckyCharmItemSpawner item = LuckyCharmItemSpawner.create(level, stack);
            item.moveTo(event.getSpawnPos());
            level.addFreshEntity(item);
            Ability.playSound(level, performer, summonSound);
            return State.CONSUME;
        }
        return State.CANCEL;
    }

    @Override
    public void detransform(AbilityData data, ServerLevel level, LivingEntity performer) {
        if (disappearOnDetransformation)
            LuckyCharmIdData.get(level).incrementLuckyCharmId(performer.getUUID());
    }

    private @Nullable Entity determineTarget(ServerLevel level, LivingEntity performer) {
        Entity target = performer.getKillCredit();
        if (target == null) {
            target = performer.getLastHurtMob();
        }
        if (target instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            Entity owner = level.getEntity(ownable.getOwnerUUID());
            if (owner != null) {
                target = owner;
            }
        }
        return target;
    }

    private LuckyCharms getLuckyCharms(Entity target) {
        if (target != null) {
            LuckyCharms luckyCharms = BuiltInRegistries.ENTITY_TYPE.getData(MineraculousDataMaps.ENTITY_LUCKY_CHARMS, target.getType().builtInRegistryHolder().key());
            if (luckyCharms != null) {
                return luckyCharms;
            }
        }
        return new LuckyCharms(Either.right(BuiltInRegistries.ITEM.getTag(MineraculousItemTags.GENERIC_LUCKY_CHARMS).orElseThrow()));
    }

    private Vec3 defaultLuckyCharmSpawnPosition(ServerLevel level, LivingEntity performer) {
        Vec3 above = performer.position().add(0, performer.getBbHeight() / 2, 0);
        if (level.getBlockState(BlockPos.containing(above)).isAir())
            return above;
        return performer.position();
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.SUMMON_TARGET_DEPENDENT_LUCKY_CHARM.get();
    }
}
