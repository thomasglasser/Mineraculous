package dev.thomasglasser.mineraculous.world.level.storage;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kwami;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.world.entity.ability.AbilityUtils;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MiraculousUtils;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.Nullable;

public record MiraculousData(Optional<KwamiData> kwamiData, Optional<CuriosData> curiosData, boolean transformed, Optional<Either<Integer, Integer>> transformationFrames, Optional<Integer> remainingTicks, int toolId, int powerLevel, boolean powerActive, boolean mainPowerUsed, List<CompoundTag> storedEntities) {

    public static final String CHARGED_TRUE = "miraculous.charged.true";
    public static final String CHARGED_FALSE = "miraculous.charged.false";
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
            Codec.BOOL.fieldOf("main_power_used").forGetter(MiraculousData::mainPowerUsed),
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
            ByteBufCodecs.BOOL, MiraculousData::mainPowerUsed,
            ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list()), MiraculousData::storedEntities,
            MiraculousData::new);
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
                            ItemStack stack = Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.getForSlot(slot).get(), miraculous);
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
                        level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((effect, startLevel) -> MineraculousEntityUtils.applyInfiniteHiddenEffect(livingEntity, level.holderOrThrow(effect), startLevel + (powerLevel / 10)));
                        livingEntity.getAttributes().addTransientAttributeModifiers(getMiraculousAttributes(level, powerLevel));
                    }

                    AbilityData abilityData = new AbilityData(powerLevel, Either.left(miraculous), false);
                    value.activeAbility().value().transform(abilityData, level, entity);
                    value.passiveAbilities().forEach(ability -> ability.value().transform(abilityData, level, entity));
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
                    kwamiData = MiraculousUtils.renounce(stack, level, kwamiData);
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

        AbilityData abilityData = new AbilityData(powerLevel, Either.left(miraculous), powerActive);
        value.activeAbility().value().detransform(abilityData, level, entity);
        value.passiveAbilities().forEach(ability -> ability.value().detransform(abilityData, level, entity));

        if (removed || detransformationFrames.isEmpty()) {
            finishDetransformation(entity, kwamiData, miraculous);
        } else {
            startDetransformation(detransformationFrames.get(), kwamiData).save(miraculous, entity, true);
        }

        if (entity instanceof Player player) {
            player.refreshDisplayName();
        }
    }

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

                if (remainingTicks.isPresent()) {
                    if (remainingTicks.get() <= 0) {
                        level.playSound(null, entity, value.timerEndSound().value(), entity.getSoundSource(), 1, 1);
                        detransform(entity, level, miraculous, false);
                        return;
                    } else {
                        remainingTicks = remainingTicks.map(i -> i - 1);
                        int maxTicks = MineraculousServerConfig.get().miraculousTimerDuration.get() * SharedConstants.TICKS_PER_SECOND;
                        int ticks = remainingTicks.get();
                        int seconds = ticks / SharedConstants.TICKS_PER_SECOND;
                        int minutes = seconds / 60;
                        if (seconds < 10) {
                            if (ticks % 10 == 0) {
                                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), value.timerWarningSound().value(), entity.getSoundSource(), 1, 1);
                            }
                        } else if (ticks % SharedConstants.TICKS_PER_MINUTE <= minutes * 5 && ticks % 5 == 0) {
                            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), value.timerWarningSound().value(), entity.getSoundSource(), 1, 1);
                        }
                    }
                    if (curiosData.isPresent() && entity instanceof LivingEntity livingEntity) {
                        ItemStack stack = CuriosUtils.getStackInSlot(livingEntity, curiosData.get());
                        stack.set(MineraculousDataComponents.REMAINING_TICKS, remainingTicks.get());
                        CuriosUtils.setStackInSlot(livingEntity, curiosData.get(), stack);
                    }
                }

                if (entity instanceof LivingEntity livingEntity) {
                    level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((key, startLevel) -> {
                        Holder<MobEffect> effect = level.holderOrThrow(key);
                        if (!livingEntity.hasEffect(effect)) {
                            MineraculousEntityUtils.applyInfiniteHiddenEffect(livingEntity, effect, startLevel);
                        }
                    });
                }

                boolean powerActive = this.powerActive;
                boolean mainPowerUsed = this.mainPowerUsed;
                AbilityData abilityData = new AbilityData(powerLevel, Either.left(miraculous), powerActive);
                boolean overrideActive = AbilityUtils.performPassiveAbilities(level, entity, abilityData, null, miraculous.value().passiveAbilities());
                if (powerActive && overrideActive) {
                    powerActive = false;
                } else if (powerActive && !mainPowerUsed) {
                    boolean consumeMainPower = AbilityUtils.performActiveAbility(level, entity, abilityData, null, Optional.of(miraculous.value().activeAbility()));
                    if (consumeMainPower) {
                        powerActive = false;
                        mainPowerUsed = true;
                    }
                    remainingTicks = remainingTicks.or(() -> Optional.of(MineraculousServerConfig.get().miraculousTimerDuration.get() * SharedConstants.TICKS_PER_SECOND));
                }

                tickTransformed(remainingTicks, powerActive, mainPowerUsed).save(miraculous, entity, true);
            }
        });
    }

    public void performAbilities(ServerLevel level, Entity entity, Holder<Miraculous> miraculous, @Nullable AbilityContext abilityContext) {
        AbilityData abilityData = new AbilityData(powerLevel, Either.left(miraculous), powerActive);
        boolean overrideActive = AbilityUtils.performPassiveAbilities(level, entity, abilityData, abilityContext, miraculous.value().passiveAbilities());
        if (powerActive && overrideActive) {
            withPowerActive(false).save(miraculous, entity, true);
        } else if (powerActive && !mainPowerUsed) {
            boolean consumeMainPower = AbilityUtils.performActiveAbility(level, entity, abilityData, abilityContext, Optional.of(miraculous.value().activeAbility()));
            if (consumeMainPower) {
                if (abilityContext != null && entity instanceof ServerPlayer player) {
                    MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(player, miraculous.getKey(), abilityContext.advancementContext());
                }
            }
            usedMainPower(consumeMainPower).save(miraculous, entity, true);
        }
    }

    private void finishTransformation(Entity entity, ServerLevel level, Holder<Miraculous> miraculous) {
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
                if (entity instanceof Player player) {
                    tool.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
                }
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

    private MiraculousData tickTransformed(Optional<Integer> remainingTicks, boolean powerActive, boolean mainPowerUsed) {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames, remainingTicks, toolId, powerLevel, powerActive, mainPowerUsed, storedEntities);
    }

    private MiraculousData decrementTransformationFrames() {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames.map(either -> either.mapLeft(frames -> frames - 1)), remainingTicks, toolId, powerLevel, powerActive, mainPowerUsed, storedEntities);
    }

    private MiraculousData decrementDetransformationFrames() {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames.map(either -> either.mapRight(frames -> frames - 1)), remainingTicks, toolId, powerLevel, powerActive, mainPowerUsed, storedEntities);
    }

    private MiraculousData clearTransformationFrames() {
        return new MiraculousData(kwamiData, curiosData, transformed, Optional.empty(), remainingTicks, toolId, powerLevel, powerActive, mainPowerUsed, storedEntities);
    }

    private MiraculousData usedMainPower(boolean consume) {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames, remainingTicks.or(() -> Optional.of(MineraculousServerConfig.get().miraculousTimerDuration.get() * SharedConstants.TICKS_PER_SECOND)), toolId, powerLevel, !consume && powerActive, consume, storedEntities);
    }

    private MiraculousData withToolId(int toolId) {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames, remainingTicks, toolId, powerLevel, powerActive, mainPowerUsed, storedEntities);
    }

    public MiraculousData equip(KwamiData kwamiData, CuriosData curiosData) {
        return new MiraculousData(Optional.of(kwamiData), Optional.of(curiosData), false, Optional.empty(), Optional.empty(), toolId, powerLevel, false, false, storedEntities);
    }

    public MiraculousData unequip() {
        return new MiraculousData(kwamiData, Optional.empty(), false, Optional.empty(), Optional.empty(), toolId, powerLevel, false, false, storedEntities);
    }

    public MiraculousData withPowerActive(boolean powerActive) {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames, remainingTicks, toolId, powerLevel, powerActive, mainPowerUsed, storedEntities);
    }

    public MiraculousData withKwamiData(Optional<KwamiData> kwamiData) {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames, remainingTicks, toolId, powerLevel, powerActive, mainPowerUsed, storedEntities);
    }

    public MiraculousData withPowerLevel(int powerLevel) {
        return new MiraculousData(kwamiData, curiosData, transformed, transformationFrames, remainingTicks, toolId, powerLevel, powerActive, mainPowerUsed, storedEntities);
    }

    public void save(Holder<Miraculous> miraculous, Entity entity, boolean sync) {
        MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        miraculousesData.put(entity, miraculous, this, sync);
    }
}
