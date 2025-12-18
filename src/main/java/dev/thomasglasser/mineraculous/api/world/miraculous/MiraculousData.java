package dev.thomasglasser.mineraculous.api.world.miraculous;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
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
import dev.thomasglasser.mineraculous.api.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.api.world.level.storage.EntityReversionData;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import dev.thomasglasser.mineraculous.impl.world.item.KwamiItem;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
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
import net.minecraft.util.ExtraCodecs;
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
 * @param lookData            The persistent {@link LookData} for the entity
 * @param curiosData          The current {@link CuriosData} if equipped
 * @param transformed         Whether the entity is currently transformed
 * @param transformationState The transformation state for the miraculous if present
 * @param remainingTicks      The remaining ticks before forced detransformation if present
 * @param toolId              The current tool ID for the entity for use in {@link ToolIdData}
 * @param powerLevel          The current power level of the miraculous
 * @param powerActive         Whether the miraculous holder's power is active
 * @param countdownStarted    Whether the detransformation countdown has been started
 * @param storedEntities      Any entities currently stored in the miraculous
 * @param buffsActive         Whether buffs are currently active
 */
public record MiraculousData(LookData lookData, Optional<CuriosData> curiosData, boolean transformed, Optional<TransformationState> transformationState, Optional<Integer> remainingTicks, int toolId, int powerLevel, boolean powerActive, boolean countdownStarted, List<CompoundTag> storedEntities, boolean buffsActive) {

    public static final String NAME_NOT_SET = "miraculous_data.name.not_set";
    public static final String KWAMI_NOT_FOUND = "miraculous_data.kwami_not_found";
    public static final int MAX_POWER_LEVEL = 100;

    public static final Codec<MiraculousData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LookData.CODEC.fieldOf("look_data").forGetter(MiraculousData::lookData),
            CuriosData.CODEC.optionalFieldOf("curios_data").forGetter(MiraculousData::curiosData),
            Codec.BOOL.fieldOf("transformed").forGetter(MiraculousData::transformed),
            TransformationState.CODEC.optionalFieldOf("transformation_state").forGetter(MiraculousData::transformationState),
            Codec.INT.optionalFieldOf("remaining_ticks").forGetter(MiraculousData::remainingTicks),
            Codec.INT.fieldOf("tool_id").forGetter(MiraculousData::toolId),
            Codec.INT.fieldOf("power_level").forGetter(MiraculousData::powerLevel),
            Codec.BOOL.fieldOf("power_active").forGetter(MiraculousData::powerActive),
            Codec.BOOL.fieldOf("countdown_started").forGetter(MiraculousData::countdownStarted),
            CompoundTag.CODEC.listOf().optionalFieldOf("stored_entities", new ObjectArrayList<>()).forGetter(MiraculousData::storedEntities),
            Codec.BOOL.fieldOf("buffs_active").forGetter(MiraculousData::buffsActive)).apply(instance, MiraculousData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            LookData.STREAM_CODEC, MiraculousData::lookData,
            ByteBufCodecs.optional(CuriosData.STREAM_CODEC), MiraculousData::curiosData,
            ByteBufCodecs.BOOL, MiraculousData::transformed,
            ByteBufCodecs.optional(TransformationState.STREAM_CODEC), MiraculousData::transformationState,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), MiraculousData::remainingTicks,
            ByteBufCodecs.VAR_INT, MiraculousData::toolId,
            ByteBufCodecs.VAR_INT, MiraculousData::powerLevel,
            ByteBufCodecs.BOOL, MiraculousData::powerActive,
            ByteBufCodecs.BOOL, MiraculousData::countdownStarted,
            ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list()), MiraculousData::storedEntities,
            ByteBufCodecs.BOOL, MiraculousData::buffsActive,
            MiraculousData::new);

    public MiraculousData(LookData lookData, Optional<CuriosData> curiosData, boolean transformed, Optional<TransformationState> transformationState, Optional<Integer> remainingTicks, int toolId, int powerLevel, boolean powerActive, boolean countdownStarted, List<CompoundTag> storedEntities, boolean buffsActive) {
        this.lookData = lookData;
        this.curiosData = curiosData;
        this.transformed = transformed;
        this.transformationState = transformationState;
        this.remainingTicks = remainingTicks;
        this.toolId = toolId;
        this.powerLevel = Math.clamp(powerLevel, 0, MAX_POWER_LEVEL);
        this.powerActive = powerActive;
        this.countdownStarted = countdownStarted;
        this.storedEntities = storedEntities;
        this.buffsActive = buffsActive;
    }

    public MiraculousData() {
        this(LookData.DEFAULT, Optional.empty(), false, Optional.empty(), Optional.empty(), 0, 0, false, false, new ObjectArrayList<>(), false);
    }

    /**
     * Transforms the provided entity with the provided {@link Miraculous}.
     *
     * @param entity     The entity to transform
     * @param level      The level to transform the entity in
     * @param miraculous The miraculous to transform with
     */
    public void transform(LivingEntity entity, ServerLevel level, Holder<Miraculous> miraculous) {
        if (entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
            MineraculousConstants.LOGGER.error("Tried to transform currently powered entity {}", entity.getName().plainCopy().getString());
            return;
        }
        curiosData.ifPresentOrElse(curiosData -> {
            ItemStack miraculousStack = CuriosUtils.getStackInSlot(entity, curiosData);
            UUID kwamiId = miraculousStack.get(MineraculousDataComponents.KWAMI_ID);
            if (kwamiId != null) {
                for (ItemStack stack : MineraculousEntityUtils.getInventoryAndCurios(entity)) {
                    if (stack.getItem() instanceof KwamiItem && stack.get(MineraculousDataComponents.KWAMI_ID).equals(kwamiId)) {
                        KwamiItem.summonKwami(stack, entity);
                    }
                }
                if (level.getEntity(kwamiId) instanceof Kwami kwami) {
                    if (kwami.isCharged() && kwami.getMainHandItem().isEmpty() && !kwami.isInCubeForm()) {
                        kwami.setTransforming(true);

                        ResourceKey<Miraculous> key = miraculous.getKey();
                        Miraculous value = miraculous.value();
                        Optional<Integer> transformationFrames = value.transformationFrames();

                        miraculousStack.set(MineraculousDataComponents.POWERED, Unit.INSTANCE);
                        miraculousStack.set(MineraculousDataComponents.TEXTURE_STATE, MiraculousItem.TextureState.POWERED);
                        CuriosUtils.setStackInSlot(entity, curiosData, miraculousStack);

                        ArmorData armor = new ArmorData(entity.getItemBySlot(EquipmentSlot.HEAD), entity.getItemBySlot(EquipmentSlot.CHEST), entity.getItemBySlot(EquipmentSlot.LEGS), entity.getItemBySlot(EquipmentSlot.FEET));
                        entity.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(armor));
                        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                            ItemStack stack = Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.getForSlot(slot), miraculous);
                            stack.enchant(entity.level().holderOrThrow(Enchantments.BINDING_CURSE), 1);
                            stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS, Unit.INSTANCE);
                            transformationFrames.ifPresent(frames -> stack.set(MineraculousDataComponents.TRANSFORMATION_STATE, new TransformationState(true, frames)));
                            entity.setItemSlot(slot, stack);
                        }

                        if (/*name.isEmpty()*/false && entity instanceof Player player) {
                            player.displayClientMessage(Component.translatable(MiraculousData.NAME_NOT_SET, Component.translatable(MineraculousConstants.toLanguageKey(key)), key.location().getPath()), true);
                        }

                        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), value.transformSound(), entity.getSoundSource(), 1, 1);
                        level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((effect, miraculousEffect) -> MineraculousEntityUtils.applyInfiniteHiddenEffect(entity, level.holderOrThrow(effect), miraculousEffect.amplifier() + ((!miraculousEffect.toggleable() || MineraculousServerConfig.get().enableBuffsOnTransformation.get()) ? powerLevel / 10 : 0)));
                        entity.getAttributes().addTransientAttributeModifiers(getMiraculousAttributes(level, powerLevel));

                        AbilityData data = AbilityData.of(this);
                        value.activeAbility().value().transform(data, level, entity);
                        value.passiveAbilities().forEach(ability -> ability.value().transform(data, level, entity));
                        EntityReversionData.get(level).startTracking(entity.getUUID());

                        transformationFrames.ifPresentOrElse(frames -> {
                            startTransformation(frames).save(miraculous, entity);
                        }, () -> finishTransformation(entity, level, miraculous));

                        if (entity instanceof ServerPlayer player) {
                            MineraculousEntityUtils.refreshAndSyncDisplayName(player);
                        }
                    } else {
                        kwami.playHurtSound(level.damageSources().starve());
                    }
                } else {
                    if (entity instanceof Player player) {
                        player.displayClientMessage(Component.translatable(KWAMI_NOT_FOUND, Component.translatable(MineraculousConstants.toLanguageKey(miraculous.getKey()))), true);
                    } else {
                        MineraculousConstants.LOGGER.error("Tried to transform entity {} with invalid kwami id {}", entity.getName().plainCopy().getString(), kwamiId);
                    }
                }
            } else {
                MineraculousConstants.LOGGER.error("Tried to transform entity {} with no Kwami Data", entity.getName().plainCopy().getString());
            }
        }, () -> MineraculousConstants.LOGGER.error("Tried to transform entity {} with no curios data", entity.getName().plainCopy().getString()));
    }

    /**
     * Detransforms the provided entity with the provided {@link Miraculous}.
     *
     * @param entity     The entity to detransform
     * @param level      The level to detransform the entity in
     * @param miraculous The miraculous to detransform with
     * @param stack      The miraculous stack to detransform with. If {@code null}, the stack will be fetched from the curios data of the entity, or fail if it's not present
     * @param removed    Whether the miraculous stack was removed to cause the detransformation
     */
    public void detransform(LivingEntity entity, ServerLevel level, Holder<Miraculous> miraculous, @Nullable ItemStack stack, boolean removed) {
        Miraculous value = miraculous.value();
        Optional<Integer> detransformationFrames = value.transformationFrames();
        ItemStack miraculousStack;

        if (stack != null) {
            miraculousStack = stack;
        } else if (curiosData.isPresent()) {
            miraculousStack = CuriosUtils.getStackInSlot(entity, curiosData.get());
            if (miraculousStack.isEmpty()) {
                MineraculousConstants.LOGGER.error("Tried to detransform entity {} with no miraculous in {} curios", entity.getName().plainCopy().getString(), curiosData.get().identifier());
                return;
            }
        } else {
            MineraculousConstants.LOGGER.error("Tried to detransform entity {} with no curios data", entity.getName().plainCopy().getString());
            return;
        }

        miraculousStack.remove(MineraculousDataComponents.REMAINING_TICKS);
        miraculousStack.set(MineraculousDataComponents.TEXTURE_STATE, removed ? MiraculousItem.TextureState.ACTIVE : MiraculousItem.TextureState.HIDDEN);

        if (removed) {
            MineraculousEntityUtils.renounceKwami(miraculousStack.get(MineraculousDataComponents.KWAMI_ID), miraculousStack, level);
        } else {
            miraculousStack.remove(MineraculousDataComponents.POWERED);
        }

        miraculousStack.set(MineraculousDataComponents.CHARGED, false);

        detransformationFrames.ifPresent(frames -> {
            for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                entity.getItemBySlot(slot).set(MineraculousDataComponents.TRANSFORMATION_STATE, new TransformationState(false, frames));
            }
        });

        UUID miraculousId = miraculousStack.get(MineraculousDataComponents.MIRACULOUS_ID);
        if (miraculousId != null) {
            for (ItemStack i : MineraculousEntityUtils.getInventoryAndCurios(entity)) {
                UUID stackId = i.get(MineraculousDataComponents.MIRACULOUS_ID);
                if (i.has(MineraculousDataComponents.TOOL_ID) && stackId != null && stackId.equals(miraculousId)) {
                    i.setCount(0);
                }
            }
        } else {
            MineraculousConstants.LOGGER.error("Tried to detransform entity {} with no miraculous id", entity.getName().plainCopy().getString());
        }

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), value.detransformSound(), entity.getSoundSource(), 1, 1);
        for (ResourceKey<MobEffect> effect : level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).keySet()) {
            entity.removeEffect(level.holderOrThrow(effect));
        }
        entity.getAttributes().removeAttributeModifiers(getMiraculousAttributes(level, powerLevel));

        AbilityData data = AbilityData.of(this);
        value.activeAbility().value().detransform(data, level, entity);
        value.passiveAbilities().forEach(ability -> ability.value().detransform(data, level, entity));

        if (!removed) {
            Kwami kwami = MineraculousEntityUtils.summonKwami(entity, false, miraculousId, miraculous, true, miraculousStack.get(MineraculousDataComponents.KWAMI_ID));
            if (kwami == null) {
                MineraculousConstants.LOGGER.error("Kwami could not be created for entity {}", entity.getName().plainCopy().getString());
            }
            miraculousStack.set(MineraculousDataComponents.KWAMI_ID, kwami == null ? null : kwami.getUUID());
            if (detransformationFrames.isEmpty()) {
                finishDetransformation(entity, miraculous);
            } else {
                startDetransformation(detransformationFrames.get()).save(miraculous, entity);
            }
        } else {
            ArmorData.restoreOrClear(entity);
            finishRemovedDetransformation().save(miraculous, entity);
        }

        if (stack == null) {
            CuriosUtils.setStackInSlot(entity, curiosData.get(), miraculousStack);
        }

        if (entity instanceof ServerPlayer player) {
            MineraculousEntityUtils.refreshAndSyncDisplayName(player);
        }
    }

    @ApiStatus.Internal
    public void tick(LivingEntity entity, ServerLevel level, Holder<Miraculous> miraculous) {
        transformationState.ifPresentOrElse(state -> {
            int frames = state.remainingFrames();
            if (state.transforming()) {
                if (frames > 0) {
                    if (entity.tickCount % 2 == 0) {
                        entity.getArmorSlots().forEach(stack -> stack.set(MineraculousDataComponents.TRANSFORMATION_STATE, new TransformationState(true, frames - 1)));
                        decrementFrames().save(miraculous, entity);
                    }
                } else {
                    finishTransformation(entity, level, miraculous);
                    entity.getArmorSlots().forEach(stack -> stack.remove(MineraculousDataComponents.TRANSFORMATION_STATE));
                }
            } else {
                if (frames > 0) {
                    if (entity.tickCount % 2 == 0) {
                        entity.getArmorSlots().forEach(stack -> stack.set(MineraculousDataComponents.TRANSFORMATION_STATE, new TransformationState(false, frames - 1)));
                        decrementFrames().save(miraculous, entity);
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
                        MiraculousItem.TextureState textureState;
                        remainingTicks = remainingTicks.map(i -> i - 1);
                        int ticks = remainingTicks.get();
                        int seconds = ticks / SharedConstants.TICKS_PER_SECOND;
                        if (seconds < 10) {
                            if (ticks % 4 == 0) {
                                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), value.timerWarningSound().value(), entity.getSoundSource(), 1, 1);
                                textureState = MiraculousItem.TextureState.ACTIVE;
                            } else {
                                textureState = MiraculousItem.TextureState.POWERED_1;
                            }
                        } else {
                            int maxSeconds = MineraculousServerConfig.get().miraculousTimerDuration.get();
                            int threshold = Math.max(maxSeconds / 5, 1);
                            int frame = seconds / threshold + 1;
                            if (seconds % threshold == 0) {
                                if (ticks % (20 / frame + (frame > 3 ? 2 : 3)) == 0) {
                                    level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), value.timerWarningSound().value(), entity.getSoundSource(), 1, 1);
                                    textureState = MiraculousItem.TextureState.forFrame(frame - 1);
                                } else {
                                    textureState = MiraculousItem.TextureState.forFrame(frame - 2);
                                }
                            } else {
                                if (seconds % 2 == 0) {
                                    textureState = MiraculousItem.TextureState.forFrame(frame - 1);
                                } else {
                                    textureState = MiraculousItem.TextureState.forFrame(frame);
                                }
                            }
                        }
                        if (curiosData.isPresent()) {
                            ItemStack stack = CuriosUtils.getStackInSlot(entity, curiosData.get());
                            stack.set(MineraculousDataComponents.REMAINING_TICKS, remainingTicks.get());
                            stack.set(MineraculousDataComponents.TEXTURE_STATE, textureState);
                            CuriosUtils.setStackInSlot(entity, curiosData.get(), stack);
                        }
                    }
                } else {
                    remainingTicks = Optional.empty();
                    if (curiosData.isPresent()) {
                        ItemStack stack = CuriosUtils.getStackInSlot(entity, curiosData.get());
                        stack.remove(MineraculousDataComponents.REMAINING_TICKS);
                        stack.set(MineraculousDataComponents.TEXTURE_STATE, MiraculousItem.TextureState.POWERED);
                        CuriosUtils.setStackInSlot(entity, curiosData.get(), stack);
                    }
                }

                level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((key, miraculousEffect) -> {
                    Holder<MobEffect> effect = level.holderOrThrow(key);
                    if (!entity.hasEffect(effect)) {
                        MineraculousEntityUtils.applyInfiniteHiddenEffect(entity, effect, miraculousEffect.amplifier() + ((!miraculousEffect.toggleable() || buffsActive) ? powerLevel / 10 : 0));
                    }
                });

                boolean powerActive = this.powerActive;
                boolean countdownStarted = this.countdownStarted;
                AbilityData data = AbilityData.of(this);
                AbilityHandler handler = new MiraculousAbilityHandler(miraculous);
                Ability.State state = AbilityUtils.performPassiveAbilities(level, entity, data, handler, null, miraculous.value().passiveAbilities());
                if (powerActive) {
                    if (state.shouldStop()) {
                        powerActive = false;
                    } else if (canUseMainPower()) {
                        state = AbilityUtils.performActiveAbility(level, entity, data, handler, null, Optional.of(miraculous.value().activeAbility()));
                        if (state.shouldStop()) {
                            powerActive = false;
                            if (state.isSuccess()) {
                                countdownStarted = hasLimitedPower();
                            }
                        }
                        remainingTicks = countdownStarted ? remainingTicks.or(() -> Optional.of(MineraculousServerConfig.get().miraculousTimerDuration.get() * SharedConstants.TICKS_PER_SECOND)) : Optional.empty();
                    }
                }

                tickTransformed(remainingTicks, powerActive, countdownStarted).save(miraculous, entity);
            } else if (curiosData.isPresent()) {
                ItemStack stack = CuriosUtils.getStackInSlot(entity, curiosData.get());
                stack.set(MineraculousDataComponents.TEXTURE_STATE, entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() ? MiraculousItem.TextureState.ACTIVE : MiraculousItem.TextureState.HIDDEN);
                CuriosUtils.setStackInSlot(entity, curiosData.get(), stack);
            }
        });
    }

    private boolean hasLimitedPower() {
        return powerLevel < MAX_POWER_LEVEL;
    }

    /**
     * Performs all abilities provided by the provided {@link Miraculous} with the provided {@link AbilityContext}.
     *
     * @param level      The level to perform the abilities in
     * @param entity     The performer of the abilities
     * @param miraculous The miraculous to fetch abilities from
     * @param context    The context to perform the abilities with. If {@code null}, abilities will be performed passively
     */
    public void performAbilities(ServerLevel level, LivingEntity entity, Holder<Miraculous> miraculous, @Nullable AbilityContext context) {
        AbilityData data = AbilityData.of(this);
        AbilityHandler handler = new MiraculousAbilityHandler(miraculous);
        Ability.State state = AbilityUtils.performPassiveAbilities(level, entity, data, handler, context, miraculous.value().passiveAbilities());
        if (powerActive) {
            if (state.shouldStop()) {
                withPowerActive(false).save(miraculous, entity);
            } else if (canUseMainPower()) {
                state = AbilityUtils.performActiveAbility(level, entity, data, handler, context, Optional.of(miraculous.value().activeAbility()));
                if (state.isSuccess()) {
                    if (context != null && entity instanceof ServerPlayer player) {
                        MineraculousCriteriaTriggers.PERFORMED_MIRACULOUS_ACTIVE_ABILITY.get().trigger(player, miraculous.getKey(), context.advancementContext());
                    }
                    usedMainPower().save(miraculous, entity);
                } else if (state.shouldStop()) {
                    withPowerActive(false).save(miraculous, entity);
                }
            }
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
            stack.remove(MineraculousDataComponents.TRANSFORMATION_STATE);
        }
        int id = createAndEquipTool(entity, level, miraculous);
        if (id > -1) {
            finishTransformation(id).save(miraculous, entity);
        } else {
            MineraculousConstants.LOGGER.error("Tool could not be created for entity {}", entity.getName().plainCopy().getString());
        }
    }

    private void finishDetransformation(LivingEntity entity, Holder<Miraculous> miraculous) {
        ArmorData.restoreOrClear(entity);
        finishDetransformation().save(miraculous, entity);
    }

    /**
     * Collects all attribute modifiers provided by {@link MineraculousDataMaps#MIRACULOUS_ATTRIBUTE_MODIFIERS}.
     *
     * @param level      The level to fetch the data map from
     * @param powerLevel The power level to apply to the attribute modifiers
     * @return A multimap containing all attribute modifiers provided by {@link MineraculousDataMaps#MIRACULOUS_ATTRIBUTE_MODIFIERS}
     */
    public static Multimap<Holder<Attribute>, AttributeModifier> getMiraculousAttributes(ServerLevel level, int powerLevel) {
        Multimap<Holder<Attribute>, AttributeModifier> attributeModifiers = HashMultimap.create();
        Registry<Attribute> attributes = level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
        attributes.getDataMap(MineraculousDataMaps.MIRACULOUS_ATTRIBUTE_MODIFIERS).forEach((attribute, settings) -> attributeModifiers.put(attributes.getHolderOrThrow(attribute), new AttributeModifier(MineraculousConstants.modLoc("miraculous_buff"), (settings.amount() * (powerLevel / 10.0)), settings.operation())));
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
                MineraculousConstants.LOGGER.error("Tried to create tool for entity {} with no miraculous id", entity.getName().plainCopy().getString());
            }
        } else {
            MineraculousConstants.LOGGER.error("Tried to create tool for entity {} with no curios data", entity.getName().plainCopy().getString());
        }
        return -1;
    }

    private MiraculousData startTransformation(int transformationFrames) {
        return new MiraculousData(lookData, curiosData, true, Optional.of(new TransformationState(true, transformationFrames)), Optional.empty(), toolId, powerLevel, false, false, storedEntities, MineraculousServerConfig.get().enableBuffsOnTransformation.get());
    }

    private MiraculousData finishTransformation(int toolId) {
        return new MiraculousData(lookData, curiosData, true, Optional.empty(), Optional.empty(), toolId, powerLevel, false, false, storedEntities, buffsActive);
    }

    private MiraculousData startDetransformation(int detransformationFrames) {
        return new MiraculousData(lookData, curiosData, false, Optional.of(new TransformationState(false, detransformationFrames)), Optional.of(0), toolId, powerLevel, false, false, storedEntities, false);
    }

    private MiraculousData finishRemovedDetransformation() {
        return new MiraculousData(lookData, Optional.empty(), false, Optional.empty(), Optional.empty(), toolId, powerLevel, false, false, storedEntities, false);
    }

    private MiraculousData finishDetransformation() {
        return new MiraculousData(lookData, curiosData, false, Optional.empty(), Optional.empty(), toolId, powerLevel, false, false, storedEntities, false);
    }

    private MiraculousData tickTransformed(Optional<Integer> remainingTicks, boolean powerActive, boolean countdownStarted) {
        return new MiraculousData(lookData, curiosData, transformed, transformationState, remainingTicks, toolId, countdownStarted && !this.countdownStarted ? powerLevel + 1 : powerLevel, powerActive, countdownStarted, storedEntities, buffsActive);
    }

    private MiraculousData decrementFrames() {
        return new MiraculousData(lookData, curiosData, transformed, transformationState.map(TransformationState::decrementFrames), remainingTicks, toolId, powerLevel, powerActive, countdownStarted, storedEntities, buffsActive);
    }

    private MiraculousData usedMainPower() {
        return new MiraculousData(lookData, curiosData, transformed, transformationState, hasLimitedPower() ? remainingTicks.or(() -> Optional.of(MineraculousServerConfig.get().miraculousTimerDuration.get() * SharedConstants.TICKS_PER_SECOND)) : Optional.empty(), toolId, powerLevel, false, hasLimitedPower(), storedEntities, buffsActive);
    }

    public MiraculousData equip(CuriosData curiosData) {
        return new MiraculousData(lookData, Optional.of(curiosData), false, transformationState, Optional.empty(), toolId, powerLevel, false, false, storedEntities, buffsActive);
    }

    public MiraculousData unequip() {
        return new MiraculousData(lookData, Optional.empty(), false, Optional.empty(), Optional.empty(), toolId, powerLevel, false, false, storedEntities, buffsActive);
    }

    public MiraculousData withLookData(LookData lookData) {
        return new MiraculousData(lookData, curiosData, transformed, transformationState, Optional.empty(), toolId, powerLevel, powerActive, countdownStarted, storedEntities, buffsActive);
    }

    public MiraculousData withPowerActive(boolean powerActive) {
        return new MiraculousData(lookData, curiosData, transformed, transformationState, remainingTicks, toolId, powerLevel, powerActive, countdownStarted, storedEntities, buffsActive);
    }

    public MiraculousData withPowerLevel(int powerLevel) {
        return new MiraculousData(lookData, curiosData, transformed, transformationState, remainingTicks, toolId, Math.clamp(powerLevel, 0, MAX_POWER_LEVEL), powerActive, countdownStarted, storedEntities, buffsActive);
    }

    public MiraculousData toggleBuffsActive() {
        return new MiraculousData(lookData, curiosData, transformed, transformationState, remainingTicks, toolId, powerLevel, powerActive, countdownStarted, storedEntities, !buffsActive);
    }

    /**
     * Saves this {@link MiraculousData} to the provided entity and syncs it.
     *
     * @param miraculous The miraculous to save this data for
     * @param entity     The entity to save this data to
     */
    public void save(Holder<Miraculous> miraculous, Entity entity) {
        entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).put(entity, miraculous, this);
    }
    /**
     * Represents an ongoing de/transformation and remaining frames for it.
     *
     * @param transforming    Whether this is a transformation or detransformation
     * @param remainingFrames The remaining frames for this de/transformation
     */
    public record TransformationState(boolean transforming, int remainingFrames) {
        public static final Codec<TransformationState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("transforming").forGetter(TransformationState::transforming),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("remainingFrames").forGetter(TransformationState::remainingFrames)).apply(instance, TransformationState::new));
        public static final StreamCodec<ByteBuf, TransformationState> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, TransformationState::transforming,
                ByteBufCodecs.VAR_INT, TransformationState::remainingFrames,
                TransformationState::new);

        public TransformationState decrementFrames() {
            return new TransformationState(transforming, remainingFrames - 1);
        }
    }

    public record LookData(Optional<String> name, Optional<String> hash) {
        public static final LookData DEFAULT = new LookData(Optional.empty(), Optional.empty());
        public static final Codec<LookData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("name").forGetter(LookData::name),
                Codec.STRING.optionalFieldOf("hash").forGetter(LookData::hash)).apply(instance, LookData::new));
        public static final StreamCodec<ByteBuf, LookData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), LookData::name,
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), LookData::hash,
                LookData::new);
    }
}
