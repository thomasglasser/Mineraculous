package dev.thomasglasser.mineraculous.api.world.miraculous;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityData;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityUtils;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.ability.handler.MiraculousAbilityHandler;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.api.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ToolIdData;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Performs functions of a {@link Miraculous}.
 *
 * @param curiosData          The current {@link CuriosData} if equipped
 * @param transformed         Whether the entity is currently transformed
 * @param transformationState The transformation state for the miraculous if present
 * @param remainingTicks      The remaining ticks before forced detransformation if present
 * @param toolId              The current tool ID for the entity for use in {@link ToolIdData}
 * @param powerLevel          The current power level of the miraculous
 * @param powerActive         Whether the miraculous holder's power is active
 * @param countdownStarted    Whether the detransformation countdown has been started
 * @param storedEntities      Any entities currently stored in the miraculous
 */
public record MiraculousData(Optional<CuriosData> curiosData, boolean transformed, Optional<TransformationState> transformationState, Optional<Integer> remainingTicks, int toolId, int powerLevel, boolean powerActive, boolean countdownStarted, List<CompoundTag> storedEntities) {

    public static final String NAME_NOT_SET = "miraculous_data.name.not_set";
    public static final int MAX_POWER_LEVEL = 100;

    public static final Codec<MiraculousData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CuriosData.CODEC.optionalFieldOf("curios_data").forGetter(MiraculousData::curiosData),
            Codec.BOOL.fieldOf("transformed").forGetter(MiraculousData::transformed),
            TransformationState.CODEC.optionalFieldOf("transformation_state").forGetter(MiraculousData::transformationState),
            Codec.INT.optionalFieldOf("remaining_ticks").forGetter(MiraculousData::remainingTicks),
            Codec.INT.fieldOf("tool_id").forGetter(MiraculousData::toolId),
            Codec.INT.fieldOf("power_level").forGetter(MiraculousData::powerLevel),
            Codec.BOOL.fieldOf("power_active").forGetter(MiraculousData::powerActive),
            Codec.BOOL.fieldOf("countdown_started").forGetter(MiraculousData::countdownStarted),
            CompoundTag.CODEC.listOf().optionalFieldOf("stored_entities", new ObjectArrayList<>()).forGetter(MiraculousData::storedEntities)).apply(instance, MiraculousData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ByteBufCodecs.optional(CuriosData.STREAM_CODEC), MiraculousData::curiosData,
            ByteBufCodecs.BOOL, MiraculousData::transformed,
            ByteBufCodecs.optional(TransformationState.STREAM_CODEC), MiraculousData::transformationState,
            ByteBufCodecs.optional(ByteBufCodecs.INT), MiraculousData::remainingTicks,
            ByteBufCodecs.INT, MiraculousData::toolId,
            ByteBufCodecs.INT, MiraculousData::powerLevel,
            ByteBufCodecs.BOOL, MiraculousData::powerActive,
            ByteBufCodecs.BOOL, MiraculousData::countdownStarted,
            ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list()), MiraculousData::storedEntities,
            MiraculousData::new);

    public MiraculousData(Optional<CuriosData> curiosData, boolean transformed, Optional<TransformationState> transformationState, Optional<Integer> remainingTicks, int toolId, int powerLevel, boolean powerActive, boolean countdownStarted, List<CompoundTag> storedEntities) {
        this.curiosData = curiosData;
        this.transformed = transformed;
        this.transformationState = transformationState;
        this.remainingTicks = remainingTicks;
        this.toolId = toolId;
        this.powerLevel = Math.clamp(powerLevel, 0, MAX_POWER_LEVEL);
        this.powerActive = powerActive;
        this.countdownStarted = countdownStarted;
        this.storedEntities = storedEntities;
    }

    public MiraculousData() {
        this(Optional.empty(), false, Optional.empty(), Optional.empty(), 0, 0, false, false, new ObjectArrayList<>());
    }

    public void transform(LivingEntity entity, ServerLevel level, Holder<Miraculous> miraculous) {
        if (entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
            Mineraculous.LOGGER.error("Tried to transform currently powered entity {}", entity.getName().plainCopy().getString());
            return;
        }
        curiosData.ifPresentOrElse(curiosData -> {
            ItemStack miraculousStack = CuriosUtils.getStackInSlot(entity, curiosData);
            UUID kwamiId = miraculousStack.get(MineraculousDataComponents.KWAMI_ID);
            if (kwamiId != null) {
                if (level.getEntity(kwamiId) instanceof Kwami kwami) {
                    if (kwami.isCharged() && kwami.getMainHandItem().isEmpty() && kwami.getSummonTicks() <= 0) {
                        kwami.setTransforming(true);

                        ResourceKey<Miraculous> key = miraculous.getKey();
                        Miraculous value = miraculous.value();
                        Optional<Integer> transformationFrames = value.transformationFrames();

                        miraculousStack.set(MineraculousDataComponents.POWERED, Unit.INSTANCE);
                        CuriosUtils.setStackInSlot(entity, curiosData, miraculousStack);

                        ArmorData armor = new ArmorData(entity.getItemBySlot(EquipmentSlot.HEAD), entity.getItemBySlot(EquipmentSlot.CHEST), entity.getItemBySlot(EquipmentSlot.LEGS), entity.getItemBySlot(EquipmentSlot.FEET));
                        entity.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(armor));
                        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                            ItemStack stack = Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.getForSlot(slot), miraculous);
                            stack.enchant(entity.level().holderOrThrow(Enchantments.BINDING_CURSE), 1);
                            stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS, Unit.INSTANCE);
                            transformationFrames.ifPresent(frames -> stack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, frames));
                            entity.setItemSlot(slot, stack);
                        }

                        if (/*name.isEmpty()*/false && entity instanceof Player player) {
                            player.displayClientMessage(Component.translatable(MiraculousData.NAME_NOT_SET, Component.translatable(Miraculous.toLanguageKey(key)), key.location().getPath()), true);
                        }

                        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), value.transformSound(), entity.getSoundSource(), 1, 1);
                        level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((effect, startAmplifier) -> MineraculousEntityUtils.applyInfiniteHiddenEffect(entity, level.holderOrThrow(effect), startAmplifier.amplifier() + (powerLevel / 10)));
                        entity.getAttributes().addTransientAttributeModifiers(getMiraculousAttributes(level, powerLevel));

                        AbilityData data = new AbilityData(powerLevel, false);
                        value.activeAbility().value().transform(data, level, entity);
                        value.passiveAbilities().forEach(ability -> ability.value().transform(data, level, entity));
                        AbilityReversionEntityData.get(level).startTracking(entity.getUUID());

                        transformationFrames.ifPresentOrElse(frames -> {
                            startTransformation(frames).save(miraculous, entity, true);
                        }, () -> finishTransformation(entity, level, miraculous));

                        if (entity instanceof Player player) {
                            player.refreshDisplayName();
                        }
                    } else {
                        kwami.playHurtSound(level.damageSources().starve());
                    }
                } else {
                    Mineraculous.LOGGER.error("Tried to transform entity {} with invalid kwami id {}", entity.getName().plainCopy().getString(), kwamiId);
                }
            } else {
                Mineraculous.LOGGER.error("Tried to transform entity {} with no Kwami Data", entity.getName().plainCopy().getString());
            }
        }, () -> Mineraculous.LOGGER.error("Tried to transform entity {} with no curios data", entity.getName().plainCopy().getString()));
    }

    public void detransform(LivingEntity entity, ServerLevel level, Holder<Miraculous> miraculous, ItemStack stack, boolean removed) {
        Miraculous value = miraculous.value();
        Optional<Integer> detransformationFrames = value.transformationFrames();
        ItemStack miraculousStack;

        if (stack != null) {
            miraculousStack = stack;
        } else if (curiosData.isPresent()) {
            miraculousStack = CuriosUtils.getStackInSlot(entity, curiosData.get());
            if (miraculousStack.isEmpty()) {
                Mineraculous.LOGGER.error("Tried to detransform entity {} with no miraculous in {} curios", entity.getName().plainCopy().getString(), curiosData.get().identifier());
                return;
            }
        } else {
            Mineraculous.LOGGER.error("Tried to detransform entity {} with no curios data", entity.getName().plainCopy().getString());
            return;
        }

        miraculousStack.remove(MineraculousDataComponents.REMAINING_TICKS);

        if (removed) {
            MineraculousEntityUtils.renounceKwami(miraculousStack.get(MineraculousDataComponents.KWAMI_ID), miraculousStack, level);
        } else {
            miraculousStack.remove(MineraculousDataComponents.POWERED);
        }

        miraculousStack.set(MineraculousDataComponents.CHARGED, false);

        detransformationFrames.ifPresentOrElse(frames -> {
            for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                entity.getItemBySlot(slot).set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, frames);
            }
        }, () -> entity.getData(MineraculousAttachmentTypes.STORED_ARMOR).ifPresent(armorData -> armorData.equipAndClear(entity)));

        UUID miraculousId = miraculousStack.get(MineraculousDataComponents.MIRACULOUS_ID);
        if (miraculousId != null) {
            for (ItemStack i : MineraculousEntityUtils.getInventoryAndCurios(entity)) {
                UUID stackId = i.get(MineraculousDataComponents.MIRACULOUS_ID);
                if (i.has(MineraculousDataComponents.TOOL_ID) && stackId != null && stackId.equals(miraculousId)) {
                    i.setCount(0);
                }
            }
        } else {
            Mineraculous.LOGGER.error("Tried to detransform entity {} with no miraculous id", entity.getName().plainCopy().getString());
        }

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), value.detransformSound(), entity.getSoundSource(), 1, 1);
        for (ResourceKey<MobEffect> effect : level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).keySet()) {
            entity.removeEffect(level.holderOrThrow(effect));
        }
        entity.getAttributes().removeAttributeModifiers(getMiraculousAttributes(level, powerLevel));

        AbilityData data = new AbilityData(powerLevel, powerActive);
        value.activeAbility().value().detransform(data, level, entity);
        value.passiveAbilities().forEach(ability -> ability.value().detransform(data, level, entity));

        if (!removed) {
            Kwami kwami = MineraculousEntityUtils.summonKwami(false, miraculousId, level, miraculous, entity);
            if (kwami == null) {
                Mineraculous.LOGGER.error("Kwami could not be created for entity {}", entity.getName().plainCopy().getString());
            }
            miraculousStack.set(MineraculousDataComponents.KWAMI_ID, kwami == null ? null : kwami.getUUID());
            if (detransformationFrames.isEmpty()) {
                finishDetransformation(entity, miraculous);
            } else {
                startDetransformation(detransformationFrames.get()).save(miraculous, entity, true);
            }
        } else {
            finishRemovedDetransformation(entity, miraculous);
        }

        if (stack == null) {
            CuriosUtils.setStackInSlot(entity, curiosData.get(), miraculousStack);
        }

        if (entity instanceof Player player) {
            player.refreshDisplayName();
        }
    }

    @ApiStatus.Internal
    public void tick(LivingEntity entity, ServerLevel level, Holder<Miraculous> miraculous) {
        transformationState.ifPresentOrElse(state -> {
            int frames = state.remainingFrames();
            if (state.transforming()) {
                if (frames > 0) {
                    if (entity.tickCount % 2 == 0) {
                        entity.getArmorSlots().forEach(stack -> stack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, frames - 1));
                        decrementFrames().save(miraculous, entity, true);
                    }
                } else {
                    finishTransformation(entity, level, miraculous);
                    entity.getArmorSlots().forEach(stack -> stack.remove(MineraculousDataComponents.TRANSFORMATION_FRAMES));
                }
            } else {
                if (frames > 0) {
                    if (entity.tickCount % 2 == 0) {
                        entity.getArmorSlots().forEach(stack -> stack.set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, frames - 1));
                        decrementFrames().save(miraculous, entity, true);
                    }
                } else {
                    finishDetransformation(entity, miraculous);
                }
            }
        }, () -> {
            if (transformed) {
                Miraculous value = miraculous.value();

                Optional<Integer> remainingTicks = this.remainingTicks;

                if (remainingTicks.isPresent() && MineraculousServerConfig.get().enableMiraculousTimer.get()) {
                    if (remainingTicks.get() <= 0) {
                        level.playSound(null, entity, value.timerEndSound().value(), entity.getSoundSource(), 1, 1);
                        detransform(entity, level, miraculous, null, false);
                        return;
                    } else {
                        remainingTicks = remainingTicks.map(i -> i - 1);
                        int ticks = remainingTicks.get();
                        int seconds = ticks / SharedConstants.TICKS_PER_SECOND;
                        if (seconds < 10) {
                            if (ticks % 4 == 0) {
                                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), value.timerWarningSound().value(), entity.getSoundSource(), 1, 1);
                            }
                        } else {
                            int maxSeconds = MineraculousServerConfig.get().miraculousTimerDuration.get();
                            int threshold = Math.max(maxSeconds / 5, 1);
                            int frame = seconds / threshold + 1;
                            if (seconds % threshold == 0 && ticks % (20 / frame + (frame > 3 ? 2 : 3)) == 0) {
                                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), value.timerWarningSound().value(), entity.getSoundSource(), 1, 1);
                            }
                        }
                    }
                    if (curiosData.isPresent()) {
                        ItemStack stack = CuriosUtils.getStackInSlot(entity, curiosData.get());
                        stack.set(MineraculousDataComponents.REMAINING_TICKS, remainingTicks.get());
                        CuriosUtils.setStackInSlot(entity, curiosData.get(), stack);
                    }
                } else {
                    remainingTicks = Optional.empty();
                }

                level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((key, startAmplifier) -> {
                    Holder<MobEffect> effect = level.holderOrThrow(key);
                    if (!entity.hasEffect(effect)) {
                        MineraculousEntityUtils.applyInfiniteHiddenEffect(entity, effect, startAmplifier.amplifier() + (powerLevel / 10));
                    }
                });

                boolean powerActive = this.powerActive;
                boolean countdownStarted = this.countdownStarted;
                AbilityData data = new AbilityData(powerLevel, powerActive);
                AbilityHandler handler = new MiraculousAbilityHandler(miraculous);
                Ability.State passiveState = AbilityUtils.performPassiveAbilities(level, entity, data, handler, null, miraculous.value().passiveAbilities());
                if (powerActive) {
                    if (passiveState.isSuccess()) {
                        powerActive = false;
                    } else if (canUseMainPower()) {
                        Ability.State state = AbilityUtils.performActiveAbility(level, entity, data, handler, null, Optional.of(miraculous.value().activeAbility()));
                        if (state.shouldConsume()) {
                            powerActive = false;
                            if (state.isSuccess()) {
                                countdownStarted = hasLimitedPower();
                            }
                        }
                        remainingTicks = countdownStarted ? remainingTicks.or(() -> Optional.of(MineraculousServerConfig.get().miraculousTimerDuration.get() * SharedConstants.TICKS_PER_SECOND)) : Optional.empty();
                    } else if (passiveState.shouldConsume()) {
                        powerActive = false;
                    }
                }

                tickTransformed(remainingTicks, powerActive, countdownStarted).save(miraculous, entity, true);
            }
        });
    }

    private boolean hasLimitedPower() {
        return powerLevel < MAX_POWER_LEVEL;
    }

    public void performAbilities(ServerLevel level, LivingEntity entity, Holder<Miraculous> miraculous, @Nullable AbilityContext context) {
        AbilityData data = new AbilityData(powerLevel, powerActive);
        AbilityHandler handler = new MiraculousAbilityHandler(miraculous);
        Ability.State state = AbilityUtils.performPassiveAbilities(level, entity, data, handler, context, miraculous.value().passiveAbilities());
        if (state.isSuccess() && powerActive) {
            withPowerActive(false).save(miraculous, entity, true);
        } else if (powerActive && canUseMainPower()) {
            boolean success = AbilityUtils.performActiveAbility(level, entity, data, handler, context, Optional.of(miraculous.value().activeAbility())).isSuccess();
            if (success) {
                if (context != null && entity instanceof ServerPlayer player) {
                    MineraculousCriteriaTriggers.PERFORMED_MIRACULOUS_ACTIVE_ABILITY.get().trigger(player, miraculous.getKey(), context.advancementContext());
                }
            }
            usedMainPower(success).save(miraculous, entity, true);
        }
    }

    private boolean canUseMainPower() {
        return !countdownStarted || !MineraculousServerConfig.get().enableLimitedPower.get();
    }

    private void finishTransformation(LivingEntity entity, ServerLevel level, Holder<Miraculous> miraculous) {
        if (entity instanceof ServerPlayer player) {
            MineraculousCriteriaTriggers.TRANSFORMED_MIRACULOUS.get().trigger(player, miraculous.getKey());
        }
        for (ItemStack stack : entity.getArmorSlots()) {
            stack.remove(MineraculousDataComponents.TRANSFORMATION_FRAMES);
        }
        int id = createAndEquipTool(entity, level, miraculous);
        if (id > -1) {
            finishTransformation(id).save(miraculous, entity, true);
        } else {
            Mineraculous.LOGGER.error("Tool could not be created for entity {}", entity.getName().plainCopy().getString());
        }
    }

    private void finishRemovedDetransformation(LivingEntity entity, Holder<Miraculous> miraculous) {
        restoreArmor(entity);
        finishRemovedDetransformation().save(miraculous, entity, true);
    }

    private void finishDetransformation(LivingEntity entity, Holder<Miraculous> miraculous) {
        restoreArmor(entity);
        finishDetransformation().save(miraculous, entity, true);
    }

    private void restoreArmor(LivingEntity entity) {
        entity.getData(MineraculousAttachmentTypes.STORED_ARMOR).ifPresent(data -> data.equipAndClear(entity));
    }

    private static Multimap<Holder<Attribute>, AttributeModifier> getMiraculousAttributes(ServerLevel level, int powerLevel) {
        Multimap<Holder<Attribute>, AttributeModifier> attributeModifiers = HashMultimap.create();
        Registry<Attribute> attributes = level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
        attributes.getDataMap(MineraculousDataMaps.MIRACULOUS_ATTRIBUTE_MODIFIERS).forEach((attribute, settings) -> attributeModifiers.put(attributes.getHolderOrThrow(attribute), new AttributeModifier(Mineraculous.modLoc("miraculous_buff"), (settings.amount() * (powerLevel / 10.0)), settings.operation())));
        return attributeModifiers;
    }

    private int createAndEquipTool(LivingEntity entity, ServerLevel level, Holder<Miraculous> miraculous) {
        if (curiosData.isPresent()) {
            CuriosData curiosData = this.curiosData.get();
            ItemStack miraculousStack = CuriosUtils.getStackInSlot(entity, curiosData);
            UUID miraculousId = miraculousStack.get(MineraculousDataComponents.MIRACULOUS_ID);
            if (miraculousId != null) {
                Miraculous value = miraculous.value();
                ItemStack tool = value.tool();
                int id = ToolIdData.get(level).incrementToolId(miraculousId);
                tool.set(MineraculousDataComponents.OWNER, entity.getUUID());
                tool.set(MineraculousDataComponents.MIRACULOUS_ID, miraculousId);
                tool.set(MineraculousDataComponents.TOOL_ID, id);
                value.toolSlot().ifPresentOrElse(slot -> {
                    boolean added = CuriosUtils.setStackInFirstValidSlot(entity, slot, tool);
                    if (!added) {
                        EntityUtils.addToInventoryOrDrop(entity, tool);
                    }
                }, () -> EntityUtils.addToInventoryOrDrop(entity, tool));
                return id;
            } else {
                Mineraculous.LOGGER.error("Tried to create tool for entity {} with no miraculous id", entity.getName().plainCopy().getString());
            }
        } else {
            Mineraculous.LOGGER.error("Tried to create tool for entity {} with no curios data", entity.getName().plainCopy().getString());
        }
        return -1;
    }

    private MiraculousData startTransformation(int transformationFrames) {
        return new MiraculousData(curiosData, true, Optional.of(new TransformationState(true, transformationFrames)), Optional.empty(), toolId, powerLevel, false, false, storedEntities);
    }

    private MiraculousData finishTransformation(int toolId) {
        return new MiraculousData(curiosData, true, Optional.empty(), Optional.empty(), toolId, powerLevel, false, false, storedEntities);
    }

    private MiraculousData startDetransformation(int detransformationFrames) {
        return new MiraculousData(curiosData, false, Optional.of(new TransformationState(false, detransformationFrames)), Optional.of(0), toolId, powerLevel, false, false, storedEntities);
    }

    private MiraculousData finishRemovedDetransformation() {
        return new MiraculousData(Optional.empty(), false, Optional.empty(), Optional.empty(), toolId, powerLevel, false, false, storedEntities);
    }

    private MiraculousData finishDetransformation() {
        return new MiraculousData(curiosData, false, Optional.empty(), Optional.empty(), toolId, powerLevel, false, false, storedEntities);
    }

    private MiraculousData tickTransformed(Optional<Integer> remainingTicks, boolean powerActive, boolean countdownStarted) {
        return new MiraculousData(curiosData, transformed, transformationState, remainingTicks, toolId, countdownStarted && !this.countdownStarted ? powerLevel + 1 : powerLevel, powerActive, countdownStarted, storedEntities);
    }

    private MiraculousData decrementFrames() {
        return new MiraculousData(curiosData, transformed, transformationState.map(TransformationState::decrementFrames), remainingTicks, toolId, powerLevel, powerActive, countdownStarted, storedEntities);
    }

    private MiraculousData usedMainPower(boolean consume) {
        return new MiraculousData(curiosData, transformed, transformationState, hasLimitedPower() ? remainingTicks.or(() -> Optional.of(MineraculousServerConfig.get().miraculousTimerDuration.get() * SharedConstants.TICKS_PER_SECOND)) : Optional.empty(), toolId, powerLevel, !consume && powerActive, consume, storedEntities);
    }

    public MiraculousData equip(CuriosData curiosData) {
        return new MiraculousData(Optional.of(curiosData), false, transformationState, Optional.empty(), toolId, powerLevel, false, false, storedEntities);
    }

    public MiraculousData unequip() {
        return new MiraculousData(Optional.empty(), false, Optional.empty(), Optional.empty(), toolId, powerLevel, false, false, storedEntities);
    }

    public MiraculousData withPowerActive(boolean powerActive) {
        return new MiraculousData(curiosData, transformed, transformationState, remainingTicks, toolId, powerLevel, powerActive, countdownStarted, storedEntities);
    }

    public MiraculousData withPowerLevel(int powerLevel) {
        return new MiraculousData(curiosData, transformed, transformationState, remainingTicks, toolId, Math.clamp(powerLevel, 0, MAX_POWER_LEVEL), powerActive, countdownStarted, storedEntities);
    }

    public void save(Holder<Miraculous> miraculous, Entity entity, boolean sync) {
        MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        miraculousesData.put(entity, miraculous, this, sync);
    }
    public record TransformationState(boolean transforming, int remainingFrames) {
        public static final Codec<TransformationState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("transforming").forGetter(TransformationState::transforming),
                Codec.INT.fieldOf("remainingFrames").forGetter(TransformationState::remainingFrames)).apply(instance, TransformationState::new));
        public static final StreamCodec<ByteBuf, TransformationState> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, TransformationState::transforming,
                ByteBufCodecs.INT, TransformationState::remainingFrames,
                TransformationState::new);

        public TransformationState decrementFrames() {
            return new TransformationState(transforming, remainingFrames - 1);
        }
    }
}
