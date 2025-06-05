package dev.thomasglasser.mineraculous.world.level.storage;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kwami;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
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

public record MiraculousData(boolean transformed, Optional<KwamiData> kwamiData, Optional<CuriosData> curiosData, int toolId, int powerLevel, boolean countdownStarted, boolean powerActive, String name, Optional<Either<Integer, Integer>> transformationFrames, String miraculousLook, String suitLook, List<CompoundTag> storedEntities) {

    public static final String NAME_NOT_SET = "miraculous_data.name.not_set";

    public static final Codec<MiraculousData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("transformed").forGetter(MiraculousData::transformed),
            KwamiData.CODEC.optionalFieldOf("kwami_data").forGetter(MiraculousData::kwamiData),
            CuriosData.CODEC.optionalFieldOf("curios_data").forGetter(MiraculousData::curiosData),
            Codec.INT.fieldOf("tool_id").forGetter(MiraculousData::toolId),
            Codec.INT.fieldOf("power_level").forGetter(MiraculousData::powerLevel),
            Codec.BOOL.fieldOf("countdown_started").forGetter(MiraculousData::countdownStarted),
            Codec.BOOL.fieldOf("power_active").forGetter(MiraculousData::powerActive),
            Codec.STRING.optionalFieldOf("name", "").forGetter(MiraculousData::name),
            Codec.either(Codec.INT, Codec.INT).optionalFieldOf("transformation_frames").forGetter(MiraculousData::transformationFrames),
            Codec.STRING.optionalFieldOf("miraculous_look", "").forGetter(MiraculousData::miraculousLook),
            Codec.STRING.optionalFieldOf("suit_look", "").forGetter(MiraculousData::suitLook),
            CompoundTag.CODEC.listOf().optionalFieldOf("stored_entities", new ObjectArrayList<>()).forGetter(MiraculousData::storedEntities)).apply(instance, MiraculousData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ByteBufCodecs.BOOL, MiraculousData::transformed,
            ByteBufCodecs.optional(KwamiData.STREAM_CODEC), MiraculousData::kwamiData,
            ByteBufCodecs.optional(CuriosData.STREAM_CODEC), MiraculousData::curiosData,
            ByteBufCodecs.INT, MiraculousData::toolId,
            ByteBufCodecs.INT, MiraculousData::powerLevel,
            ByteBufCodecs.BOOL, MiraculousData::countdownStarted,
            ByteBufCodecs.BOOL, MiraculousData::powerActive,
            ByteBufCodecs.STRING_UTF8, MiraculousData::name,
            ByteBufCodecs.optional(ByteBufCodecs.either(ByteBufCodecs.INT, ByteBufCodecs.INT)), MiraculousData::transformationFrames,
            ByteBufCodecs.STRING_UTF8, MiraculousData::miraculousLook,
            ByteBufCodecs.STRING_UTF8, MiraculousData::suitLook,
            ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list()), MiraculousData::storedEntities,
            MiraculousData::new);
    public MiraculousData() {
        this(false, Optional.empty(), Optional.empty(), 0, 0, false, false, "", Optional.empty(), "", "", new ObjectArrayList<>());
    }

    public void transform(Entity entity, ServerLevel level, ResourceKey<Miraculous> miraculous) {
        if (entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
            Mineraculous.LOGGER.error("Tried to transform currently powered entity {}", entity.getName().plainCopy().getString());
            return;
        }
        kwamiData.ifPresentOrElse(kwamiData -> {
            if (entity.level().getEntity(kwamiData.id()) instanceof Kwami kwami) {
                if (kwami.isCharged()) {
                    Miraculous value = level.holderOrThrow(miraculous).value();
                    Optional<Integer> transformationFrames = value.transformationFrames();

                    if (entity instanceof LivingEntity livingEntity) {
                        ArmorData armor = new ArmorData(livingEntity.getItemBySlot(EquipmentSlot.HEAD), livingEntity.getItemBySlot(EquipmentSlot.CHEST), livingEntity.getItemBySlot(EquipmentSlot.LEGS), livingEntity.getItemBySlot(EquipmentSlot.FEET));
                        livingEntity.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(armor));
                        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                            ItemStack stack = Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.getForSlot(slot).get(), miraculous);
                            stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
                            transformationFrames.ifPresent(frames -> stack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, frames));
                            livingEntity.setItemSlot(slot, stack);
                        }

                        if (curiosData.isPresent()) {
                            ItemStack miraculousStack = CuriosUtils.getStackInSlot(livingEntity, curiosData.get());

                            miraculousStack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS, Unit.INSTANCE);
                            miraculousStack.set(MineraculousDataComponents.POWERED, Unit.INSTANCE);

                            CuriosUtils.setStackInSlot(livingEntity, curiosData.get(), miraculousStack);
                        }
                    }

                    level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), value.transformSound(), entity.getSoundSource(), 1, 1);
                    if (name.isEmpty() && entity instanceof Player player) {
                        player.displayClientMessage(Component.translatable(MiraculousData.NAME_NOT_SET, Component.translatable(Miraculous.toLanguageKey(miraculous)), miraculous.location().getPath()), true);
                    }
                    if (entity instanceof LivingEntity livingEntity) {
                        level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((effect, startLevel) -> livingEntity.addEffect(MineraculousEntityEvents.INFINITE_HIDDEN_EFFECT.apply(level.holderOrThrow(effect), startLevel + (powerLevel / 10))));
                        livingEntity.getAttributes().addTransientAttributeModifiers(getMiraculousAttributes(level, powerLevel));
                    }
                    kwami.discard();
                    value.activeAbility().ifPresent(ability -> ability.value().transform(new AbilityData(powerLevel, Either.left(miraculous)), level, entity));
                    value.passiveAbilities().forEach(ability -> ability.value().transform(new AbilityData(powerLevel, Either.left(miraculous)), level, entity));

                    transform(transformationFrames.map(Either::<Integer, Integer>left).orElseGet(() -> Either.right(equipTool(entity, level, value, kwamiData)))).save(miraculous, entity, true);
                } else {
                    kwami.playHurtSound(level.damageSources().starve());
                }
            } else {
                withKwamiData(Optional.empty()).save(miraculous, entity, true);
            }
        }, () -> Mineraculous.LOGGER.error("Tried to transform entity {} with no Kwami Data", entity.getName().plainCopy().getString()));
    }

    public void detransform(Entity entity, ServerLevel level, ResourceKey<Miraculous> miraculous, boolean removed) {
        Kwami kwami = MineraculousEntityEvents.summonKwami(level, miraculous, this, entity);
        if (kwami != null) {
            kwami.setCharged(false);
        } else {
            Mineraculous.LOGGER.error("Kwami could not be created for entity " + entity.getName().plainCopy().getString());
            return;
        }

        Miraculous value = level.holderOrThrow(miraculous).value();
        Optional<Integer> detransformationFrames = value.transformationFrames();

        Optional<KwamiData> kwamiData = this.kwamiData;

        if (entity instanceof LivingEntity livingEntity) {
            if (curiosData.isPresent()) {
                ItemStack miraculousStack = CuriosUtils.getStackInSlot(livingEntity, curiosData.get());
                miraculousStack.remove(DataComponents.ENCHANTMENTS);
                miraculousStack.remove(MineraculousDataComponents.REMAINING_TICKS);
                if (removed) {
                    kwamiData = MineraculousEntityEvents.renounceMiraculous(level, miraculousStack, kwamiData, Optional.of(false));
                } else {
                    miraculousStack.remove(MineraculousDataComponents.POWERED);
                }
                CuriosUtils.setStackInSlot(livingEntity, curiosData.get(), miraculousStack);
            }

            detransformationFrames.ifPresentOrElse(frames -> {
                for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                    livingEntity.getItemBySlot(slot).set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, frames);
                }
            }, () -> livingEntity.getData(MineraculousAttachmentTypes.STORED_ARMOR).ifPresent(armorData -> {
                for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                    livingEntity.setItemSlot(slot, armorData.forSlot(slot));
                }
            }));
        } else if (removed && kwamiData.isPresent()) {
            kwamiData = Optional.of(new KwamiData(kwamiData.get().uuid(), kwamiData.get().id(), false));
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
        value.activeAbility().ifPresent(ability -> ability.value().detransform(new AbilityData(powerLevel, Either.left(miraculous)), level, entity));
        value.passiveAbilities().forEach(ability -> ability.value().detransform(new AbilityData(powerLevel, Either.left(miraculous)), level, entity));

        detransform(kwamiData, removed, detransformationFrames).save(miraculous, entity, true);
    }

    public void tick() {
        // TODO: Check and re-apply effects
    }

    private int equipTool(Entity entity, ServerLevel level, Miraculous miraculous, KwamiData kwamiData) {
        ItemStack tool = miraculous.tool();
        if (!tool.isEmpty()) {
            int id = ToolIdData.get(level).incrementToolId(kwamiData);
            if (entity instanceof Player player) {
                tool.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
            }
            tool.set(MineraculousDataComponents.KWAMI_DATA, kwamiData);
            tool.set(MineraculousDataComponents.TOOL_ID, id);
            miraculous.toolSlot().ifPresentOrElse(slot -> {
                boolean added = entity instanceof LivingEntity livingEntity && CuriosUtils.setStackInFirstValidSlot(livingEntity, slot, tool);
                if (!added) {
                    EntityUtils.addToInventoryOrDrop(entity, tool);
                }
            }, () -> EntityUtils.addToInventoryOrDrop(entity, tool));
            return id;
        }
        return -1;
    }

    private static Multimap<Holder<Attribute>, AttributeModifier> getMiraculousAttributes(ServerLevel level, int powerLevel) {
        Multimap<Holder<Attribute>, AttributeModifier> attributeModifiers = HashMultimap.create();
        Registry<Attribute> attributes = level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
        attributes.getDataMap(MineraculousDataMaps.MIRACULOUS_ATTRIBUTE_MODIFIERS).forEach((attribute, settings) -> attributeModifiers.put(attributes.getHolderOrThrow(attribute), new AttributeModifier(Mineraculous.modLoc("miraculous_buff"), (settings.amount() * (powerLevel / 10.0)), settings.operation())));
        return attributeModifiers;
    }

    private MiraculousData transform(Either<Integer, Integer> either) {
        return either.map(
                transformationFrames -> new MiraculousData(true, kwamiData, curiosData, toolId, powerLevel, countdownStarted, powerActive, name, Optional.of(Either.left(transformationFrames)), miraculousLook, suitLook, storedEntities),
                toolId -> new MiraculousData(true, kwamiData, curiosData, toolId, powerLevel, countdownStarted, powerActive, name, Optional.empty(), miraculousLook, suitLook, storedEntities));
    }

    private MiraculousData detransform(Optional<KwamiData> kwamiData, boolean removed, Optional<Integer> detransformationFrames) {
        return new MiraculousData(false, kwamiData, removed ? Optional.empty() : curiosData, toolId, powerLevel, false, false, name, detransformationFrames.map(Either::right), miraculousLook, suitLook, storedEntities);
    }

    public boolean hasLimitedPower() {
        return MineraculousServerConfig.get().enableLimitedPower.get() && powerLevel < 100;
    }

    public boolean shouldCountDown() {
        return MineraculousServerConfig.get().enableMiraculousTimer.get() && hasLimitedPower() && countdownStarted;
    }

    public MiraculousData withKwamiData(Optional<KwamiData> kwamiData) {
        return new MiraculousData(transformed, kwamiData, curiosData, toolId, powerLevel, countdownStarted, powerActive, name, transformationFrames, miraculousLook, suitLook, storedEntities);
    }

//
//    public MiraculousData equip(ItemStack miraculousItem, CuriosData curiosData) {
//        return new MiraculousData(false, miraculousItem, curiosData, toolId, powerLevel, false, false, name, miraculousLook, suitLook, storedEntities);
//    }
//
//    public MiraculousData unEquip() {
//        return new MiraculousData(transformed, ItemStack.EMPTY, Optional.empty(), toolId, powerLevel, false, false, name, miraculousLook, suitLook, storedEntities);
//    }
//
//    public void transform(boolean transformed, ItemStack miraculousItem, int toolId) {
//        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel, false, false, name, miraculousLook, suitLook, storedEntities);
//    }
//
//    public MiraculousData withCuriosData(CuriosData curiosData) {
//        return new MiraculousData(transformed, Optional.of(curiosData), toolId, powerLevel, countdownStarted, powerActive, name, miraculousLook, suitLook, storedEntities);
//    }
//
//    public MiraculousData withPowerStatus(boolean activated, boolean active) {
//        return new MiraculousData(transformed, curiosData, toolId, powerLevel, activated, active, name, miraculousLook, suitLook, storedEntities);
//    }
//
//    public MiraculousData withUsedPower() {
//        return new MiraculousData(transformed, curiosData, toolId, powerLevel + 1, true, false, name, miraculousLook, suitLook, storedEntities);
//    }
//
//    public MiraculousData withName(String name) {
//        return new MiraculousData(transformed, curiosData, toolId, powerLevel, countdownStarted, powerActive, name, miraculousLook, suitLook, storedEntities);
//    }
//
//    public MiraculousData withMiraculousLook(String miraculousLook) {
//        return new MiraculousData(transformed, curiosData, toolId, powerLevel, countdownStarted, powerActive, name, miraculousLook, suitLook, storedEntities);
//    }
//
//    public MiraculousData withSuitLook(String suitLook) {
//        return new MiraculousData(transformed, curiosData, toolId, powerLevel, countdownStarted, powerActive, name, miraculousLook, suitLook, storedEntities);
//    }
//
//    public MiraculousData withLevel(int level) {
//        return new MiraculousData(transformed, curiosData, toolId, level, countdownStarted, powerActive, name, miraculousLook, suitLook, storedEntities);
//    }

    public void save(ResourceKey<Miraculous> key, Entity entity, boolean sync) {
        MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        miraculousesData.put(entity, key, this, sync);
    }
}
