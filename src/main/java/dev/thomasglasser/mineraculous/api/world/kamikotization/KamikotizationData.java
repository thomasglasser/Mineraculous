package dev.thomasglasser.mineraculous.api.world.kamikotization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.look.LookData;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.api.event.KamikotizationEvent;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityData;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityUtils;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.KamikotizationAbilityHandler;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.api.world.level.storage.EntityReversionData;
import dev.thomasglasser.mineraculous.api.world.level.storage.ItemReversionData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.impl.world.item.component.KamikoData;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Performs functions of a {@link Kamikotization}.
 *
 * @param kamikotization         The current Kamikotization
 * @param kamikoData             The current {@link KamikoData}
 * @param lookData               The current {@link LookData}
 * @param revertibleId           The unique identifier for the kamikotized item
 * @param kamikotizedSlot        The equipment slot the kamikotized item is in if present
 * @param transformationState    The remaining transformation frames for the current kamikotization if present
 * @param originalStackCount     The original count of items used in the kamikotization
 * @param remainingStackCount    The remaining count of items to be broken for the kamikotization to end
 * @param powerActive            Whether the kamikotized entity's power is active
 * @param buffsActive            Whether the kamikotized entity's buffs are active
 * @param brokenKamikotizedStack The broken kamikotized item if present
 */
public record KamikotizationData(Holder<Kamikotization> kamikotization, KamikoData kamikoData, LookData lookData, UUID revertibleId, Optional<EquipmentSlot> kamikotizedSlot, Optional<MiraculousData.TransformationState> transformationState, int originalStackCount, int remainingStackCount, boolean powerActive, boolean buffsActive, Optional<ItemStack> brokenKamikotizedStack) {

    public static final Codec<KamikotizationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Kamikotization.CODEC.fieldOf("kamikotization").forGetter(KamikotizationData::kamikotization),
            KamikoData.CODEC.fieldOf("kamiko_data").forGetter(KamikotizationData::kamikoData),
            LookData.CODEC.optionalFieldOf("look_data", LookData.DEFAULT).forGetter(KamikotizationData::lookData),
            UUIDUtil.CODEC.fieldOf("revertible_id").forGetter(KamikotizationData::revertibleId),
            EquipmentSlot.CODEC.optionalFieldOf("kamikotized_slot").forGetter(KamikotizationData::kamikotizedSlot),
            MiraculousData.TransformationState.CODEC.optionalFieldOf("transformation_state").forGetter(KamikotizationData::transformationState),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("original_stack_count").forGetter(KamikotizationData::originalStackCount),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("remaining_stack_count").forGetter(KamikotizationData::remainingStackCount),
            Codec.BOOL.fieldOf("power_active").forGetter(KamikotizationData::powerActive),
            Codec.BOOL.fieldOf("buffs_active").forGetter(KamikotizationData::buffsActive),
            ItemStack.CODEC.optionalFieldOf("broken_kamikotized_stack").forGetter(KamikotizationData::brokenKamikotizedStack)).apply(instance, KamikotizationData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KamikotizationData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            Kamikotization.STREAM_CODEC, KamikotizationData::kamikotization,
            KamikoData.STREAM_CODEC, KamikotizationData::kamikoData,
            LookData.STREAM_CODEC, KamikotizationData::lookData,
            UUIDUtil.STREAM_CODEC, KamikotizationData::revertibleId,
            ByteBufCodecs.optional(TommyLibExtraStreamCodecs.forEnum(EquipmentSlot.class)), KamikotizationData::kamikotizedSlot,
            ByteBufCodecs.optional(MiraculousData.TransformationState.STREAM_CODEC), KamikotizationData::transformationState,
            ByteBufCodecs.VAR_INT, KamikotizationData::originalStackCount,
            ByteBufCodecs.VAR_INT, KamikotizationData::remainingStackCount,
            ByteBufCodecs.BOOL, KamikotizationData::powerActive,
            ByteBufCodecs.BOOL, KamikotizationData::buffsActive,
            ByteBufCodecs.optional(ItemStack.STREAM_CODEC), KamikotizationData::brokenKamikotizedStack,
            KamikotizationData::new);
    public KamikotizationData(Holder<Kamikotization> kamikotization, KamikoData kamikoData, LookData lookData) {
        this(kamikotization, kamikoData, lookData, Util.NIL_UUID, Optional.empty(), Optional.empty(), 0, 0, false, false, Optional.empty());
    }

    /// The number of frames of transformation, decreasing every 2 ticks
    public static final int TRANSFORMATION_FRAMES = 10;

    /**
     * Transforms the provided entity with {@link Kamikotization},
     * powered by the provided {@link ItemStack}.
     *
     * @param entity        The entity to transform
     * @param level         The level to transform the entity in
     * @param originalStack The initial {@link ItemStack} used for transformation
     * @return The altered {@link ItemStack} used for transformation
     */
    public ItemStack transform(LivingEntity entity, ServerLevel level, ItemStack originalStack) {
        Kamikotization value = kamikotization.value();

        if (NeoForge.EVENT_BUS.post(new KamikotizationEvent.Transform.Pre(entity, this, originalStack)).isCanceled())
            return originalStack;

        ItemStack kamikotizationStack = originalStack.copy();

        if (value.powerSource().left().isPresent()) {
            ItemStack tool = value.powerSource().left().get();
            kamikotizationStack = tool.copyWithCount(Math.min(kamikotizationStack.getCount(), tool.getMaxStackSize()));
        }

        kamikotizationStack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS, Unit.INSTANCE);
        kamikotizationStack.set(MineraculousDataComponents.KAMIKO_DATA, kamikoData);
        kamikotizationStack.set(MineraculousDataComponents.KAMIKOTIZATION, kamikotization);
        kamikotizationStack.set(MineraculousDataComponents.OWNER, entity.getUUID());

        UUID revertibleId = UUID.randomUUID();
        ItemReversionData.get(level).putKamikotized(revertibleId, originalStack);
        kamikotizationStack.set(MineraculousDataComponents.REVERTIBLE_ITEM_ID, revertibleId);

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), MineraculousSoundEvents.KAMIKOTIZATION_TRANSFORM, entity.getSoundSource(), 1, 1);
        level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((effect, miraculousEffect) -> MineraculousEntityUtils.applyInfiniteHiddenEffect(entity, level.holderOrThrow(effect), miraculousEffect.amplifier() + ((!miraculousEffect.toggleable() || MineraculousServerConfig.get().enableBuffsOnTransformation.get()) ? kamikoData.powerLevel() / 10 : 0)));
        entity.getAttributes().addTransientAttributeModifiers(MiraculousData.getMiraculousAttributes(level, kamikoData.powerLevel()));

        AbilityData data = AbilityData.of(this);
        value.powerSource().right().ifPresent(ability -> ability.value().transform(data, level, entity));
        value.passiveAbilities().forEach(ability -> ability.value().transform(data, level, entity));
        EntityReversionData.get(level).startTracking(entity.getUUID());

        Optional<EquipmentSlot> kamikotizedSlot = Optional.empty();
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            ItemStack armorStack = entity.getItemBySlot(slot);
            if (originalStack == armorStack) {
                kamikotizedSlot = Optional.of(slot);
                break;
            }
        }

        KamikotizationData transformed = startTransformation(revertibleId, kamikotizedSlot, kamikotizationStack.getCount());
        Optional<Integer> transformationFrames = NeoForge.EVENT_BUS.post(new KamikotizationEvent.Transform.Start(entity, transformed, kamikotizationStack, Optional.of(TRANSFORMATION_FRAMES))).getTransformationFrames();
        ItemStack finalKamikotizationStack = kamikotizationStack;
        transformationFrames.ifPresentOrElse(frames -> transformed.withTransformationState(new MiraculousData.TransformationState(true, Optional.of(frames))).save(entity), () -> transformed.finishTransformation(entity, finalKamikotizationStack));

        if (entity instanceof ServerPlayer player) {
            MineraculousEntityUtils.refreshAndSyncDisplayName(player);
        }

        return kamikotizationStack;
    }

    /**
     * Detransforms the provided entity,
     * removing the present kamikotization.
     *
     * @param entity           The entity to detransform
     * @param level            The level to detransform the entity in
     * @param kamikoSpawnPos   The position to spawn the kamiko at
     * @param instant          Whether to detransform instantly
     * @param kamikotizedStack The {@link ItemStack} used for detransformation, if present
     */
    public void detransform(LivingEntity entity, ServerLevel level, Vec3 kamikoSpawnPos, boolean revertKamiko, boolean instant, @Nullable ItemStack kamikotizedStack) {
        if (NeoForge.EVENT_BUS.post(new KamikotizationEvent.Detransform.Pre(entity, this, kamikotizedStack)).isCanceled())
            return;

        if (revertKamiko) {
            EntityReversionData.get(level).revertConversionOrCopy(kamikoData.uuid(), level, reverted -> reverted.moveTo(kamikoSpawnPos));
        } else {
            Kamiko kamiko = kamikoData.summon(level, kamikoSpawnPos, kamikotization, lookData, originalStackCount, entity);
            if (kamiko == null) {
                MineraculousConstants.LOGGER.error("Kamiko could not be created for player {}", entity.getName().plainCopy().getString());
            }
        }

        LivingEntity owner = level.getEntity(kamikoData.owner()) instanceof LivingEntity l ? l : null;
        if (owner != null && owner.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).spectatingId().map(id -> id.equals(entity.getUUID())).orElse(false)) {
            owner.getData(MineraculousAttachmentTypes.TRANSIENT_ABILITY_EFFECTS).withSpectationInterrupted(true).save(owner);
        }

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), MineraculousSoundEvents.KAMIKOTIZATION_DETRANSFORM, entity.getSoundSource(), 1, 1);
        for (ResourceKey<MobEffect> effect : level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).keySet()) {
            entity.removeEffect(level.holderOrThrow(effect));
        }
        entity.getAttributes().removeAttributeModifiers(MiraculousData.getMiraculousAttributes(level, kamikoData.powerLevel()));

        Kamikotization value = kamikotization.value();
        AbilityData data = AbilityData.of(this);
        value.powerSource().right().ifPresent(ability -> ability.value().detransform(data, level, entity));
        value.passiveAbilities().forEach(ability -> ability.value().detransform(data, level, entity));
        if (instant) {
            finishDetransformation(entity, kamikotizedStack);
        } else {
            KamikotizationData detransformed = startDetransformation(kamikotizedStack);
            Optional<Integer> detransformationFrames = NeoForge.EVENT_BUS.post(new KamikotizationEvent.Detransform.Start(entity, detransformed, kamikotizedStack, Optional.of(TRANSFORMATION_FRAMES))).getDetransformationFrames();
            detransformationFrames.ifPresentOrElse(frames -> detransformed.withTransformationState(new MiraculousData.TransformationState(false, Optional.of(frames))).save(entity), () -> finishDetransformation(entity, kamikotizedStack));
        }

        if (entity instanceof ServerPlayer player) {
            MineraculousEntityUtils.refreshAndSyncDisplayName(player);
        }
    }

    @ApiStatus.Internal
    public void tick(LivingEntity entity, ServerLevel level) {
        transformationState.ifPresentOrElse(state -> state.remainingFrames().ifPresent(frames -> {
            if (frames > 0) {
                if (entity.tickCount % 2 == 0) {
                    decrementFrames().save(entity);
                }
                level.sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), entity.getX(), entity.getY() + 2 - ((Kamikotization.TRANSFORMATION_FRAMES + 1) - frames) / 5.0, entity.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
            } else {
                if (state.transforming()) {
                    finishTransformation(entity, null);
                } else {
                    finishDetransformation(entity, brokenKamikotizedStack.orElse(null));
                }
            }
        }), () -> {
            level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((key, miraculousEffect) -> {
                Holder<MobEffect> effect = level.holderOrThrow(key);
                if (!entity.hasEffect(effect)) {
                    MineraculousEntityUtils.applyInfiniteHiddenEffect(entity, effect, miraculousEffect.amplifier() + ((!miraculousEffect.toggleable() || buffsActive) ? kamikoData.powerLevel() / 10 : 0));
                }
            });

            performAbilities(level, entity, null);
        });
    }

    /**
     * Performs all abilities provided by the {@link Kamikotization} associated with this {@link KamikotizationData} with the provided {@link AbilityContext}.
     *
     * @param level   The level to perform the abilities in
     * @param entity  The entity performing the abilities
     * @param context The {@link AbilityContext} to use for the abilities ({@code null} for passive)
     */
    public void performAbilities(ServerLevel level, LivingEntity entity, @Nullable AbilityContext context) {
        AbilityData data = AbilityData.of(this);
        KamikotizationAbilityHandler handler = new KamikotizationAbilityHandler(kamikotization);
        Ability.State state = AbilityUtils.performPassiveAbilities(level, entity, data, handler, context, kamikotization.value().passiveAbilities());
        if (powerActive) {
            if (state.shouldStop()) {
                withPowerActive(false).save(entity);
            } else {
                state = AbilityUtils.performActiveAbility(level, entity, data, handler, context, kamikotization.value().powerSource().right());
                if (state.isSuccess()) {
                    if (context != null && entity instanceof ServerPlayer player) {
                        MineraculousCriteriaTriggers.PERFORMED_KAMIKOTIZATION_ACTIVE_ABILITY.get().trigger(player, kamikotization.getKey(), context.advancementContext());
                    }
                }
                withPowerActive(!state.shouldStop()).save(entity);
            }
        }
    }

    /**
     * Equips {@link MineraculousArmors#KAMIKOTIZATION} with the provided {@link Kamikotization} on the provided entity.
     *
     * @param entity         The entity to equip the armor on
     * @param kamikotization The kamikotization to assign to the armor
     */
    public static void equipKamikotizationArmor(LivingEntity entity, Holder<Kamikotization> kamikotization) {
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            ItemStack stack = Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.getForSlot(slot), kamikotization);
            stack.enchant(entity.level().holderOrThrow(Enchantments.BINDING_CURSE), 1);
            stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS, Unit.INSTANCE);
            entity.setItemSlot(slot, stack);
        }
    }

    private void finishTransformation(LivingEntity entity, @Nullable ItemStack kamikotizedStack) {
        entity.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(new ArmorData(entity)));
        equipKamikotizationArmor(entity, kamikotization);
        KamikotizationData transformed = clearTransformationFrames();
        transformed.save(entity);
        NeoForge.EVENT_BUS.post(new KamikotizationEvent.Transform.Finish(entity, transformed, kamikotizedStack));
    }

    private void finishDetransformation(LivingEntity entity, @Nullable ItemStack kamikotizedStack) {
        ArmorData.restoreOrClear(entity);
        if (entity.level() instanceof ServerLevel level) {
            ItemReversionData.get(level).revertKamikotized(entity, revertibleId, kamikotizedStack);
        }
        remove(entity);
        NeoForge.EVENT_BUS.post(new KamikotizationEvent.Detransform.Finish(entity, kamikotizedStack));
    }

    private KamikotizationData startTransformation(UUID revertibleId, Optional<EquipmentSlot> kamikotizedSlot, int stackCount) {
        return new KamikotizationData(kamikotization, kamikoData, lookData, revertibleId, kamikotizedSlot, Optional.empty(), stackCount, stackCount, false, MineraculousServerConfig.get().enableBuffsOnTransformation.get(), Optional.empty());
    }

    private KamikotizationData startDetransformation(@Nullable ItemStack brokenKamikotizedStack) {
        return new KamikotizationData(kamikotization, kamikoData, lookData, revertibleId, kamikotizedSlot, Optional.empty(), originalStackCount, 0, false, buffsActive, Optional.ofNullable(brokenKamikotizedStack).map(stack -> stack.copyWithCount(Math.max(1, stack.getCount()))));
    }

    private KamikotizationData withTransformationState(MiraculousData.TransformationState state) {
        return new KamikotizationData(kamikotization, kamikoData, lookData, revertibleId, kamikotizedSlot, Optional.of(state), originalStackCount, remainingStackCount, powerActive, buffsActive, brokenKamikotizedStack);
    }

    private KamikotizationData decrementFrames() {
        return new KamikotizationData(kamikotization, kamikoData, lookData, revertibleId, kamikotizedSlot, transformationState.map(MiraculousData.TransformationState::decrementFrames), originalStackCount, remainingStackCount, powerActive, buffsActive, brokenKamikotizedStack);
    }

    private KamikotizationData clearTransformationFrames() {
        return new KamikotizationData(kamikotization, kamikoData, lookData, revertibleId, kamikotizedSlot, Optional.empty(), originalStackCount, remainingStackCount, powerActive, buffsActive, brokenKamikotizedStack);
    }

    public KamikotizationData clearKamikotizedSlot() {
        return new KamikotizationData(kamikotization, kamikoData, lookData, revertibleId, Optional.empty(), transformationState, originalStackCount, remainingStackCount, powerActive, buffsActive, brokenKamikotizedStack);
    }

    public KamikotizationData decrementRemainingStackCount() {
        return new KamikotizationData(kamikotization, kamikoData, lookData, revertibleId, kamikotizedSlot, transformationState, originalStackCount, remainingStackCount - 1, powerActive, buffsActive, brokenKamikotizedStack);
    }

    public KamikotizationData withPowerActive(boolean powerActive) {
        return new KamikotizationData(kamikotization, kamikoData, lookData, revertibleId, kamikotizedSlot, transformationState, originalStackCount, remainingStackCount, powerActive, buffsActive, brokenKamikotizedStack);
    }

    public KamikotizationData toggleBuffsActive() {
        return new KamikotizationData(kamikotization, kamikoData, lookData, revertibleId, kamikotizedSlot, transformationState, originalStackCount, remainingStackCount, powerActive, !buffsActive, brokenKamikotizedStack);
    }

    /**
     * Saves this {@link KamikotizationData} to the provided {@link Entity} and syncs it,
     * also removing the {@link MineraculousAttachmentTypes#OLD_KAMIKOTIZATION} if present.
     *
     * @param entity The entity to save this {@link KamikotizationData} to
     */
    public void save(Entity entity) {
        entity.setData(MineraculousAttachmentTypes.KAMIKOTIZATION, Optional.of(this));
        if (entity.getData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION).isPresent())
            entity.setData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION, Optional.empty());
    }

    /**
     * Removes the provided entity's {@link KamikotizationData} and syncs it,
     * also setting the {@link MineraculousAttachmentTypes#OLD_KAMIKOTIZATION} to the current one.
     *
     * @param entity The entity to remove the {@link KamikotizationData} from
     */
    public static void remove(Entity entity) {
        entity.setData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION, entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION));
        entity.removeData(MineraculousAttachmentTypes.KAMIKOTIZATION);
    }
}
