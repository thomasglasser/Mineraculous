package dev.thomasglasser.mineraculous.api.world.miraculous;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.datamaps.MineraculousDataMaps;
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
import dev.thomasglasser.mineraculous.impl.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ToolIdData;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
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
 * @param kwamiData            The current {@link KwamiData} if present
 * @param curiosData           The current {@link CuriosData} if equipped
 * @param transformed          Whether the entity is currently transformed
 * @param transformationFrames The remaining transformation frames for the miraculous if present
 * @param remainingTicks       The remaining ticks before forced detransformation if present
 * @param toolId               The current tool ID for the entity for use in {@link ToolIdData}
 * @param powerLevel           The current power level of the miraculous
 * @param powerActive          Whether the miraculous holder's power is active
 * @param countdownStarted     Whether the detransformation countdown has been started
 * @param storedEntities       Any entities currently stored in the miraculous
 */
public record MiraculousData(Optional<KwamiData> kwamiData, Optional<CuriosData> curiosData, boolean transformed, Optional<Either<Integer, Integer>> transformationFrames, Optional<Integer> remainingTicks, int toolId, int powerLevel, boolean powerActive, boolean countdownStarted, List<CompoundTag> storedEntities) {

    public static final String NAME_NOT_SET = "miraculous_data.name.not_set";
    public static final int MAX_POWER_LEVEL = 100;

    public static final Codec<MiraculousData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            KwamiData.CODEC.optionalFieldOf("kwami_data").forGetter(MiraculousData::kwamiData),
            CuriosData.CODEC.optionalFieldOf("curios_data").forGetter(MiraculousData::curiosData),
            Codec.BOOL.fieldOf("transformed").forGetter(MiraculousData::transformed),
            Codec.either(Codec.INT, Codec.INT).optionalFieldOf("transformation_frames").forGetter(MiraculousData::transformationFrames),
            Codec.INT.optionalFieldOf("remaining_ticks").forGetter(MiraculousData::remainingTicks),
            Codec.INT.fieldOf("tool_id").forGetter(MiraculousData::toolId),
            Codec.INT.fieldOf("power_level").forGetter(MiraculousData::powerLevel),
            Codec.BOOL.fieldOf("power_active").forGetter(MiraculousData::powerActive),
            Codec.BOOL.fieldOf("countdown_started").forGetter(MiraculousData::countdownStarted),
            CompoundTag.CODEC.listOf().optionalFieldOf("stored_entities", new ObjectArrayList<>()).forGetter(MiraculousData::storedEntities)).apply(instance, MiraculousData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ByteBufCodecs.optional(KwamiData.STREAM_CODEC), MiraculousData::kwamiData,
            ByteBufCodecs.optional(CuriosData.STREAM_CODEC), MiraculousData::curiosData,
            ByteBufCodecs.BOOL, MiraculousData::transformed,
            ByteBufCodecs.optional(ByteBufCodecs.either(ByteBufCodecs.INT, ByteBufCodecs.INT)), MiraculousData::transformationFrames,
            ByteBufCodecs.optional(ByteBufCodecs.INT), MiraculousData::remainingTicks,
            ByteBufCodecs.INT, MiraculousData::toolId,
            ByteBufCodecs.INT, MiraculousData::powerLevel,
            ByteBufCodecs.BOOL, MiraculousData::powerActive,
            ByteBufCodecs.BOOL, MiraculousData::countdownStarted,
            ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list()), MiraculousData::storedEntities,
            MiraculousData::new);
    public MiraculousData(Optional<KwamiData> kwamiData, Optional<CuriosData> curiosData, boolean transformed, Optional<Either<Integer, Integer>> transformationFrames, Optional<Integer> remainingTicks, int toolId, int powerLevel, boolean powerActive, boolean countdownStarted, List<CompoundTag> storedEntities) {
        this.kwamiData = kwamiData;
        this.curiosData = curiosData;
        this.transformed = transformed;
        this.transformationFrames = transformationFrames;
        this.remainingTicks = remainingTicks;
        this.toolId = toolId;
        this.powerLevel = Math.clamp(powerLevel, 0, MAX_POWER_LEVEL);
        this.powerActive = powerActive;
        this.countdownStarted = countdownStarted;
        this.storedEntities = storedEntities;
    }

    public MiraculousData() {
        this(Optional.empty(), Optional.empty(), false, Optional.empty(), Optional.empty(), 0, 0, false, false, new ObjectArrayList<>());
    }

    public void transform(Entity entity, ServerLevel level, Holder<Miraculous> miraculous) {
        if (entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
            Mineraculous.LOGGER.error("Tried to transform currently powered entity {}", entity.getName().plainCopy().getString());
            return;
        }
        kwamiData.ifPresentOrElse(kwamiData -> {
            if (level.getEntity(kwamiData.uuid()) instanceof Kwami kwami) {
                if (kwami.isCharged()) {
                    kwami.setCharged(false);
                    kwami.discard();

                    ResourceKey<Miraculous> key = miraculous.getKey();
                    Miraculous value = miraculous.value();
                    Optional<Integer> transformationFrames = value.transformationFrames();

                    if (entity instanceof LivingEntity livingEntity) {
                        ArmorData armor = new ArmorData(livingEntity.getItemBySlot(EquipmentSlot.HEAD), livingEntity.getItemBySlot(EquipmentSlot.CHEST), livingEntity.getItemBySlot(EquipmentSlot.LEGS), livingEntity.getItemBySlot(EquipmentSlot.FEET));
                        livingEntity.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(armor));
                        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                            ItemStack stack = Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.getForSlot(slot), miraculous);
                            stack.enchant(entity.level().holderOrThrow(Enchantments.BINDING_CURSE), 1);
                            stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS, Unit.INSTANCE);
                            transformationFrames.ifPresent(frames -> stack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, frames));
                            livingEntity.setItemSlot(slot, stack);
                        }

                        curiosData.ifPresent(curiosData -> {
                            ItemStack miraculousStack = CuriosUtils.getStackInSlot(livingEntity, curiosData);
                            miraculousStack.set(MineraculousDataComponents.POWERED, Unit.INSTANCE);
                            CuriosUtils.setStackInSlot(livingEntity, curiosData, miraculousStack);
                        });
                    }

                    if (/*name.isEmpty()*/false && entity instanceof Player player) {
                        player.displayClientMessage(Component.translatable(MiraculousData.NAME_NOT_SET, Component.translatable(Miraculous.toLanguageKey(key)), key.location().getPath()), true);
                    }

                    level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), value.transformSound(), entity.getSoundSource(), 1, 1);
                    if (entity instanceof LivingEntity livingEntity) {
                        level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((effect, startAmplifier) -> MineraculousEntityUtils.applyInfiniteHiddenEffect(livingEntity, level.holderOrThrow(effect), startAmplifier.amplifier() + (powerLevel / 10)));
                        livingEntity.getAttributes().addTransientAttributeModifiers(getMiraculousAttributes(level, powerLevel));
                    }

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
                withKwamiData(Optional.empty()).save(miraculous, entity, true);
            }
        }, () -> Mineraculous.LOGGER.error("Tried to transform entity {} with no Kwami Data", entity.getName().plainCopy().getString()));
    }

    public void detransform(Entity entity, ServerLevel level, Holder<Miraculous> miraculous, boolean removed) {
        Kwami kwami = KwamiData.summon(kwamiData, level, miraculous.getKey(), entity);
        if (kwami != null) {
            kwami.setCharged(false);
        } else {
            Mineraculous.LOGGER.error("Kwami could not be created for entity {}", entity.getName().plainCopy().getString());
            return;
        }

        Miraculous value = miraculous.value();

        Optional<Integer> detransformationFrames = value.transformationFrames();

        Optional<KwamiData> kwamiData = this.kwamiData.map(data -> new KwamiData(data.uuid(), data.id(), false));

        if (entity instanceof LivingEntity livingEntity) {
            if (curiosData.isPresent()) {
                ItemStack stack = CuriosUtils.getStackInSlot(livingEntity, curiosData.get());
                stack.remove(MineraculousDataComponents.REMAINING_TICKS);
                if (removed) {
                    kwamiData = KwamiData.renounce(kwamiData, stack, level);
                } else {
                    stack.remove(MineraculousDataComponents.POWERED);
                }
                CuriosUtils.setStackInSlot(livingEntity, curiosData.get(), stack);
            }

            detransformationFrames.ifPresentOrElse(frames -> {
                for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                    livingEntity.getItemBySlot(slot).set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, frames);
                }
            }, () -> livingEntity.getData(MineraculousAttachmentTypes.STORED_ARMOR).ifPresent(armorData -> armorData.equipAndClear(livingEntity)));
        }

        for (ItemStack stack : EntityUtils.getInventory(entity)) {
            KwamiData stackData = stack.get(MineraculousDataComponents.KWAMI_DATA);
            if (stack.has(MineraculousDataComponents.TOOL_ID) && stackData != null && stackData.uuid().equals(kwami.getUUID())) {
                stack.setCount(0);
            }
        }
        if (entity instanceof LivingEntity livingEntity) {
            CuriosUtils.getAllItems(livingEntity).values().forEach(stack -> {
                KwamiData stackData = stack.get(MineraculousDataComponents.KWAMI_DATA);
                if (stack.has(MineraculousDataComponents.TOOL_ID) && stackData != null && stackData.uuid().equals(kwami.getUUID())) {
                    stack.setCount(0);
                }
            });
        }

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), value.detransformSound(), entity.getSoundSource(), 1, 1);
        if (entity instanceof LivingEntity livingEntity) {
            for (ResourceKey<MobEffect> effect : level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).keySet()) {
                livingEntity.removeEffect(level.holderOrThrow(effect));
            }
            livingEntity.getAttributes().removeAttributeModifiers(getMiraculousAttributes(level, powerLevel));
        }

        AbilityData data = new AbilityData(powerLevel, powerActive);
        value.activeAbility().value().detransform(data, level, entity);
        value.passiveAbilities().forEach(ability -> ability.value().detransform(data, level, entity));

        if (removed || detransformationFrames.isEmpty()) {
            finishDetransformation(entity, kwamiData, miraculous);
        } else {
            startDetransformation(detransformationFrames.get(), kwamiData).save(miraculous, entity, true);
        }

        if (entity instanceof Player player) {
            player.refreshDisplayName();
        }
    }

    @ApiStatus.Internal
    public void tick(Entity entity, ServerLevel level, Holder<Miraculous> miraculous) {
        transformationFrames.ifPresentOrElse(either -> either.ifLeft(transformationFrames -> {
            if (transformationFrames > 0) {
                if (entity.tickCount % 2 == 0) {
                    if (entity instanceof LivingEntity livingEntity) {
                        livingEntity.getArmorSlots().forEach(stack -> stack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, transformationFrames - 1));
                    }
                    decrementTransformationFrames().save(miraculous, entity, true);
                }
            } else {
                finishTransformation(entity, level, miraculous);
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.getArmorSlots().forEach(stack -> stack.remove(MineraculousDataComponents.TRANSFORMATION_FRAMES));
                }
                clearTransformationFrames().save(miraculous, entity, true);
            }
        }).ifRight(detransformationFrames -> {
            if (detransformationFrames > 0) {
                if (entity.tickCount % 2 == 0) {
                    if (entity instanceof LivingEntity livingEntity) {
                        livingEntity.getArmorSlots().forEach(stack -> stack.set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, detransformationFrames - 1));
                    }
                    decrementDetransformationFrames().save(miraculous, entity, true);
                }
            } else {
                finishDetransformation(entity, kwamiData, miraculous);
            }
        }), () -> {
            if (transformed) {
                Miraculous value = miraculous.value();

                Optional<Integer> remainingTicks = this.remainingTicks;

                if (remainingTicks.isPresent() && MineraculousServerConfig.get().enableMiraculousTimer.get()) {
                    if (remainingTicks.get() <= 0) {
                        level.playSound(null, entity, value.timerEndSound().value(), entity.getSoundSource(), 1, 1);
                        detransform(entity, level, miraculous, false);
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
                    if (curiosData.isPresent() && entity instanceof LivingEntity livingEntity) {
                        ItemStack stack = CuriosUtils.getStackInSlot(livingEntity, curiosData.get());
                        stack.set(MineraculousDataComponents.REMAINING_TICKS, remainingTicks.get());
                        CuriosUtils.setStackInSlot(livingEntity, curiosData.get(), stack);
                    }
                } else {
                    remainingTicks = Optional.empty();
                }

                if (entity instanceof LivingEntity livingEntity) {
                    level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((key, startAmplifier) -> {
                        Holder<MobEffect> effect = level.holderOrThrow(key);
                        if (!livingEntity.hasEffect(effect)) {
                            MineraculousEntityUtils.applyInfiniteHiddenEffect(livingEntity, effect, startAmplifier.amplifier() + (powerLevel / 10));
                        }
                    });
                }

                boolean powerActive = this.powerActive;
                boolean countdownStarted = this.countdownStarted;
                AbilityData data = new AbilityData(powerLevel, powerActive);
                AbilityHandler handler = new MiraculousAbilityHandler(miraculous);
                if (AbilityUtils.performPassiveAbilities(level, entity, data, handler, null, miraculous.value().passiveAbilities()) && powerActive) {
                    powerActive = false;
                } else if (powerActive && canUseMainPower()) {
                    if (AbilityUtils.performActiveAbility(level, entity, data, handler, null, Optional.of(miraculous.value().activeAbility()))) {
                        powerActive = false;
                        countdownStarted = powerLevel < MAX_POWER_LEVEL;
                    }
                    remainingTicks = remainingTicks.or(() -> Optional.of(MineraculousServerConfig.get().miraculousTimerDuration.get() * SharedConstants.TICKS_PER_SECOND));
                }

                tickTransformed(remainingTicks, powerActive, countdownStarted).save(miraculous, entity, true);
            }
        });
    }

    public void performAbilities(ServerLevel level, Entity entity, Holder<Miraculous> miraculous, @Nullable AbilityContext context) {
        AbilityData data = new AbilityData(powerLevel, powerActive);
        AbilityHandler handler = new MiraculousAbilityHandler(miraculous);
        if (AbilityUtils.performPassiveAbilities(level, entity, data, handler, context, miraculous.value().passiveAbilities()) && powerActive) {
            withPowerActive(false).save(miraculous, entity, true);
        } else if (powerActive && canUseMainPower()) {
            boolean consumeMainPower = AbilityUtils.performActiveAbility(level, entity, data, handler, context, Optional.of(miraculous.value().activeAbility()));
            if (consumeMainPower) {
                if (context != null && entity instanceof ServerPlayer player) {
                    MineraculousCriteriaTriggers.PERFORMED_MIRACULOUS_ACTIVE_ABILITY.get().trigger(player, miraculous.getKey(), context.advancementContext());
                }
            }
            usedMainPower(consumeMainPower).save(miraculous, entity, true);
        }
    }

    private boolean canUseMainPower() {
        return !countdownStarted || !MineraculousServerConfig.get().enableLimitedPower.get();
    }

    private void finishTransformation(Entity entity, ServerLevel level, Holder<Miraculous> miraculous) {
        if (entity instanceof ServerPlayer player) {
            MineraculousCriteriaTriggers.TRANSFORMED_MIRACULOUS.get().trigger(player, miraculous.getKey());
        }
        if (entity instanceof LivingEntity livingEntity) {
            for (ItemStack stack : livingEntity.getArmorSlots()) {
                stack.remove(MineraculousDataComponents.TRANSFORMATION_FRAMES);
            }
        }
        int id = createAndEquipTool(entity, level, miraculous);
        if (id > -1) {
            withToolId(id).save(miraculous, entity, true);
        } else {
            Mineraculous.LOGGER.error("Tool could not be created for entity {}", entity.getName().plainCopy().getString());
        }
    }

    private void finishDetransformation(Entity entity, Optional<KwamiData> kwamiData, Holder<Miraculous> miraculous) {
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.getData(MineraculousAttachmentTypes.STORED_ARMOR).ifPresent(data -> data.equipAndClear(livingEntity));
        }
        finishDetransformation(kwamiData).save(miraculous, entity, true);
    }

    private static Multimap<Holder<Attribute>, AttributeModifier> getMiraculousAttributes(ServerLevel level, int powerLevel) {
        Multimap<Holder<Attribute>, AttributeModifier> attributeModifiers = HashMultimap.create();
        Registry<Attribute> attributes = level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
        attributes.getDataMap(MineraculousDataMaps.MIRACULOUS_ATTRIBUTE_MODIFIERS).forEach((attribute, settings) -> attributeModifiers.put(attributes.getHolderOrThrow(attribute), new AttributeModifier(Mineraculous.modLoc("miraculous_buff"), (settings.amount() * (powerLevel / 10.0)), settings.operation())));
        return attributeModifiers;
    }

    private int createAndEquipTool(Entity entity, ServerLevel level, Holder<Miraculous> miraculous) {
        if (kwamiData.isPresent()) {
            KwamiData kwamiData = this.kwamiData.get();
            Miraculous value = miraculous.value();
            ItemStack tool = value.tool();
            if (!tool.isEmpty()) {
                int id = ToolIdData.get(level).incrementToolId(kwamiData);
                tool.set(MineraculousDataComponents.OWNER, entity.getUUID());
                tool.set(MineraculousDataComponents.KWAMI_DATA, kwamiData);
                tool.set(MineraculousDataComponents.TOOL_ID, id);
                value.toolSlot().ifPresentOrElse(slot -> {
                    boolean added = entity instanceof LivingEntity livingEntity && CuriosUtils.setStackInFirstValidSlot(livingEntity, slot, tool);
                    if (!added) {
                        EntityUtils.addToInventoryOrDrop(entity, tool);
                    }
                }, () -> EntityUtils.addToInventoryOrDrop(entity, tool));
                return id;
            }
        }
        return -1;
    }

    private MiraculousData startTransformation(int transformationFrames) {
        Optional<KwamiData> kwamiData = this.kwamiData.map(data -> new KwamiData(data.uuid(), data.id(), false));
        return new MiraculousData(kwamiData, curiosData, true, Optional.of(Either.left(transformationFrames)), Optional.empty(), toolId, powerLevel, false, false, storedEntities);
    }

    private MiraculousData startDetransformation(int detransformationFrames, Optional<KwamiData> kwamiData) {
        return new MiraculousData(kwamiData, curiosData, false, Optional.of(Either.right(detransformationFrames)), Optional.of(0), toolId, powerLevel, false, false, storedEntities);
    }

    private MiraculousData finishDetransformation(Optional<KwamiData> kwamiData) {
        return new MiraculousData(kwamiData, curiosData, false, Optional.empty(), Optional.empty(), toolId, powerLevel, false, false, storedEntities);
    }

    private MiraculousData tickTransformed(Optional<Integer> remainingTicks, boolean powerActive, boolean countdownStarted) {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames, remainingTicks, toolId, countdownStarted && !this.countdownStarted ? powerLevel + 1 : powerLevel, powerActive, countdownStarted, storedEntities);
    }

    private MiraculousData decrementTransformationFrames() {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames.map(either -> either.mapLeft(frames -> frames - 1)), remainingTicks, toolId, powerLevel, powerActive, countdownStarted, storedEntities);
    }

    private MiraculousData decrementDetransformationFrames() {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames.map(either -> either.mapRight(frames -> frames - 1)), remainingTicks, toolId, powerLevel, powerActive, countdownStarted, storedEntities);
    }

    private MiraculousData clearTransformationFrames() {
        return new MiraculousData(kwamiData, curiosData, transformed, Optional.empty(), remainingTicks, toolId, powerLevel, powerActive, countdownStarted, storedEntities);
    }

    private MiraculousData usedMainPower(boolean consume) {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames, remainingTicks.or(() -> Optional.of(MineraculousServerConfig.get().miraculousTimerDuration.get() * SharedConstants.TICKS_PER_SECOND)), toolId, powerLevel, !consume && powerActive, consume, storedEntities);
    }

    private MiraculousData withToolId(int toolId) {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames, remainingTicks, toolId, powerLevel, powerActive, countdownStarted, storedEntities);
    }

    public MiraculousData equip(KwamiData kwamiData, CuriosData curiosData) {
        return new MiraculousData(Optional.of(kwamiData), Optional.of(curiosData), false, Optional.empty(), Optional.empty(), toolId, powerLevel, false, false, storedEntities);
    }

    public MiraculousData unequip() {
        return new MiraculousData(kwamiData, Optional.empty(), false, Optional.empty(), Optional.empty(), toolId, powerLevel, false, false, storedEntities);
    }

    public MiraculousData withPowerActive(boolean powerActive) {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames, remainingTicks, toolId, powerLevel, powerActive, countdownStarted, storedEntities);
    }

    public MiraculousData withKwamiData(Optional<KwamiData> kwamiData) {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames, remainingTicks, toolId, powerLevel, powerActive, countdownStarted, storedEntities);
    }

    public MiraculousData withPowerLevel(int powerLevel) {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames, remainingTicks, toolId, Math.clamp(powerLevel, 0, MAX_POWER_LEVEL), powerActive, countdownStarted, storedEntities);
    }

    public void save(Holder<Miraculous> miraculous, Entity entity, boolean sync) {
        MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        miraculousesData.put(entity, miraculous, this, sync);
    }
}
