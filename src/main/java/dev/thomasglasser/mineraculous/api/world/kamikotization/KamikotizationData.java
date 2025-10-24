package dev.thomasglasser.mineraculous.api.world.kamikotization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityData;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityUtils;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.KamikotizationAbilityHandler;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionItemData;
import dev.thomasglasser.mineraculous.api.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.impl.world.item.component.KamikoData;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
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
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Performs functions of a {@link Kamikotization}.
 *
 * @param kamikotization      The current Kamikotization
 * @param kamikoData          The current {@link KamikoData}
 * @param name                The name override of the kamikotized entity
 * @param revertibleId        The unique identifier for the kamikotized item
 * @param transformationState The remaining transformation frames for the current kamikotization if present
 * @param remainingStackCount The remaining number of stacks to be broken for the kamikotization to end
 * @param powerActive         Whether the kamikotized entity's power is active
 */
public record KamikotizationData(Holder<Kamikotization> kamikotization, KamikoData kamikoData, String name, UUID revertibleId, Optional<EquipmentSlot> kamikotizedSlot, Optional<MiraculousData.TransformationState> transformationState, int remainingStackCount, boolean powerActive, boolean buffsActive) {

    public static final Codec<KamikotizationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Kamikotization.CODEC.fieldOf("kamikotization").forGetter(KamikotizationData::kamikotization),
            KamikoData.CODEC.fieldOf("kamiko_data").forGetter(KamikotizationData::kamikoData),
            Codec.STRING.optionalFieldOf("name", "").forGetter(KamikotizationData::name),
            UUIDUtil.CODEC.fieldOf("revertible_id").forGetter(KamikotizationData::revertibleId),
            EquipmentSlot.CODEC.optionalFieldOf("kamikotized_slot").forGetter(KamikotizationData::kamikotizedSlot),
            MiraculousData.TransformationState.CODEC.optionalFieldOf("transformation_frames").forGetter(KamikotizationData::transformationState),
            Codec.INT.fieldOf("remaining_stack_count").forGetter(KamikotizationData::remainingStackCount),
            Codec.BOOL.fieldOf("power_active").forGetter(KamikotizationData::powerActive),
            Codec.BOOL.fieldOf("buffs_active").forGetter(KamikotizationData::buffsActive)).apply(instance, KamikotizationData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KamikotizationData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            Kamikotization.STREAM_CODEC, KamikotizationData::kamikotization,
            KamikoData.STREAM_CODEC, KamikotizationData::kamikoData,
            ByteBufCodecs.STRING_UTF8, KamikotizationData::name,
            UUIDUtil.STREAM_CODEC, KamikotizationData::revertibleId,
            ByteBufCodecs.optional(TommyLibExtraStreamCodecs.forEnum(EquipmentSlot.class)), KamikotizationData::kamikotizedSlot,
            ByteBufCodecs.optional(MiraculousData.TransformationState.STREAM_CODEC), KamikotizationData::transformationState,
            ByteBufCodecs.INT, KamikotizationData::remainingStackCount,
            ByteBufCodecs.BOOL, KamikotizationData::powerActive,
            ByteBufCodecs.BOOL, KamikotizationData::buffsActive,
            KamikotizationData::new);
    public KamikotizationData(Holder<Kamikotization> kamikotization, KamikoData kamikoData, String name, int slotCount) {
        this(kamikotization, kamikoData, name, Util.NIL_UUID, Optional.empty(), Optional.empty(), slotCount, false, false);
    }

    private static final int TRANSFORMATION_FRAMES = 10;

    public ItemStack transform(LivingEntity entity, ServerLevel level, ItemStack originalStack) {
        originalStack.remove(MineraculousDataComponents.KAMIKOTIZING);

        if (entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
            MineraculousConstants.LOGGER.error("Tried to kamikotize currently powered entity: {}", entity.getName().plainCopy().getString());
            return originalStack;
        }

        Kamikotization value = kamikotization.value();

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
        AbilityReversionItemData.get(level).putKamikotized(entity.getUUID(), revertibleId, originalStack);
        kamikotizationStack.set(MineraculousDataComponents.REVERTIBLE_ITEM_ID, revertibleId);

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), MineraculousSoundEvents.KAMIKOTIZATION_TRANSFORM, entity.getSoundSource(), 1, 1);
        level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((effect, miraculousEffect) -> MineraculousEntityUtils.applyInfiniteHiddenEffect(entity, level.holderOrThrow(effect), miraculousEffect.amplifier() + ((!miraculousEffect.toggleable() || MineraculousServerConfig.get().enableBuffsOnTransformation.get()) ? kamikoData.powerLevel() / 10 : 0)));
        entity.getAttributes().addTransientAttributeModifiers(MiraculousData.getMiraculousAttributes(level, kamikoData.powerLevel()));

        AbilityData data = AbilityData.of(this);
        value.powerSource().right().ifPresent(ability -> ability.value().transform(data, level, entity));
        value.passiveAbilities().forEach(ability -> ability.value().transform(data, level, entity));
        AbilityReversionEntityData.get(level).startTracking(entity.getUUID());

        Optional<EquipmentSlot> kamikotizedSlot = Optional.empty();
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            ItemStack armorStack = entity.getItemBySlot(slot);
            if (originalStack == armorStack) {
                kamikotizedSlot = Optional.of(slot);
                break;
            }
        }

        startTransformation(revertibleId, kamikotizedSlot, kamikotizationStack.getCount()).save(entity, true);

        if (entity instanceof ServerPlayer player) {
            MineraculousEntityUtils.refreshAndSyncDisplayName(player);
        }

        return kamikotizationStack;
    }

    public void detransform(LivingEntity entity, ServerLevel level, Vec3 kamikoSpawnPos, boolean instant) {
        Kamiko kamiko = kamikoData.summon(level, kamikoSpawnPos);
        if (kamiko == null) {
            MineraculousConstants.LOGGER.error("Kamiko could not be created for player {}", entity.getName().plainCopy().getString());
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
            finishDetransformation(entity);
        } else {
            startDetransformation().save(entity, true);
        }

        if (entity instanceof ServerPlayer player) {
            MineraculousEntityUtils.refreshAndSyncDisplayName(player);
        }
    }

    @ApiStatus.Internal
    public void tick(LivingEntity entity, ServerLevel level) {
        transformationState.ifPresentOrElse(state -> {
            int frames = state.remainingFrames();
            if (state.transforming()) {
                if (frames > 0) {
                    if (entity.tickCount % 2 == 0) {
                        decrementTransformationFrames().save(entity, true);
                    }
                    level.sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), entity.getX(), entity.getY() + 2 - ((Kamikotization.TRANSFORMATION_FRAMES + 1) - frames) / 5.0, entity.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
                } else {
                    finishTransformation(entity);
                    clearTransformationFrames().save(entity, true);
                }
            } else {
                if (frames > 0) {
                    if (entity.tickCount % 2 == 0) {
                        decrementDetransformationFrames().save(entity, true);
                    }
                    level.sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), entity.getX(), entity.getY() + 2 - ((Kamikotization.TRANSFORMATION_FRAMES + 1) - frames) / 5.0, entity.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
                } else {
                    finishDetransformation(entity);
                }
            }
        }, () -> {
            level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((key, miraculousEffect) -> {
                Holder<MobEffect> effect = level.holderOrThrow(key);
                if (!entity.hasEffect(effect)) {
                    MineraculousEntityUtils.applyInfiniteHiddenEffect(entity, effect, miraculousEffect.amplifier() + ((!miraculousEffect.toggleable() || buffsActive) ? kamikoData.powerLevel() / 10 : 0));
                }
            });

            performAbilities(level, entity, null);
        });
    }

    public void performAbilities(ServerLevel level, LivingEntity entity, @Nullable AbilityContext context) {
        AbilityData data = AbilityData.of(this);
        KamikotizationAbilityHandler handler = new KamikotizationAbilityHandler(kamikotization);
        if (AbilityUtils.performPassiveAbilities(level, entity, data, handler, context, kamikotization.value().passiveAbilities()).isSuccess() && powerActive) {
            withPowerActive(false).save(entity, true);
        } else if (powerActive) {
            boolean success = AbilityUtils.performActiveAbility(level, entity, data, handler, context, kamikotization.value().powerSource().right()).isSuccess();
            if (success) {
                if (context != null && entity instanceof ServerPlayer player) {
                    MineraculousCriteriaTriggers.PERFORMED_KAMIKOTIZATION_ACTIVE_ABILITY.get().trigger(player, kamikotization.getKey(), context.advancementContext());
                }
            }
            withPowerActive(!success).save(entity, true);
        }
    }

    private void finishTransformation(LivingEntity entity) {
        ArmorData armor = new ArmorData(entity.getItemBySlot(EquipmentSlot.HEAD), entity.getItemBySlot(EquipmentSlot.CHEST), entity.getItemBySlot(EquipmentSlot.LEGS), entity.getItemBySlot(EquipmentSlot.FEET));
        entity.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(armor));
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            ItemStack stack = Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.getForSlot(slot), kamikotization);
            stack.enchant(entity.level().holderOrThrow(Enchantments.BINDING_CURSE), 1);
            stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS, Unit.INSTANCE);
            entity.setItemSlot(slot, stack);
        }
    }

    private void finishDetransformation(LivingEntity entity) {
        ArmorData.restoreOrClear(entity);
        if (entity.level() instanceof ServerLevel level) {
            AbilityReversionItemData.get(level).revertKamikotized(entity, revertibleId);
        }
        remove(entity, true);
    }

    private KamikotizationData startTransformation(UUID revertibleId, Optional<EquipmentSlot> kamikotizedSlot, int stackCount) {
        return new KamikotizationData(kamikotization, kamikoData, name, revertibleId, kamikotizedSlot, Optional.of(new MiraculousData.TransformationState(true, TRANSFORMATION_FRAMES)), stackCount, false, MineraculousServerConfig.get().enableBuffsOnTransformation.get());
    }

    private KamikotizationData startDetransformation() {
        return new KamikotizationData(kamikotization, kamikoData, name, revertibleId, kamikotizedSlot, Optional.of(new MiraculousData.TransformationState(false, TRANSFORMATION_FRAMES)), 0, false, buffsActive);
    }

    private KamikotizationData decrementTransformationFrames() {
        return new KamikotizationData(kamikotization, kamikoData, name, revertibleId, kamikotizedSlot, transformationState.map(MiraculousData.TransformationState::decrementFrames), remainingStackCount, powerActive, buffsActive);
    }

    private KamikotizationData decrementDetransformationFrames() {
        return new KamikotizationData(kamikotization, kamikoData, name, revertibleId, kamikotizedSlot, transformationState.map(MiraculousData.TransformationState::decrementFrames), remainingStackCount, powerActive, buffsActive);
    }

    private KamikotizationData clearTransformationFrames() {
        return new KamikotizationData(kamikotization, kamikoData, name, revertibleId, kamikotizedSlot, Optional.empty(), remainingStackCount, powerActive, buffsActive);
    }

    public KamikotizationData clearKamikotizedSlot() {
        return new KamikotizationData(kamikotization, kamikoData, name, revertibleId, Optional.empty(), transformationState, remainingStackCount, powerActive, buffsActive);
    }

    public KamikotizationData decrementRemainingStackCount() {
        return new KamikotizationData(kamikotization, kamikoData, name, revertibleId, kamikotizedSlot, transformationState, remainingStackCount - 1, powerActive, buffsActive);
    }

    public KamikotizationData withPowerActive(boolean powerActive) {
        return new KamikotizationData(kamikotization, kamikoData, name, revertibleId, kamikotizedSlot, transformationState, remainingStackCount, powerActive, buffsActive);
    }

    public KamikotizationData toggleBuffsActive() {
        return new KamikotizationData(kamikotization, kamikoData, name, revertibleId, kamikotizedSlot, transformationState, remainingStackCount, powerActive, !buffsActive);
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.KAMIKOTIZATION, Optional.of(this));
        if (entity.getData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION).isPresent())
            entity.setData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION, Optional.empty());
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.KAMIKOTIZATION, Optional.of(this)), entity.getServer());
    }

    public static void remove(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION, entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION));
        entity.setData(MineraculousAttachmentTypes.KAMIKOTIZATION, Optional.empty());
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.KAMIKOTIZATION, Optional.<Optional<KamikotizationData>>empty()), entity.getServer());
    }
}
