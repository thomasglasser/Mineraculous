package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.world.entity.ability.AbilityUtils;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record KamikotizationData(Holder<Kamikotization> kamikotization, KamikoData kamikoData, String name, Optional<Either<Integer, Integer>> transformationFrames, int remainingStackCount, boolean powerActive) {

    public static final Codec<KamikotizationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Kamikotization.CODEC.fieldOf("kamikotization").forGetter(KamikotizationData::kamikotization),
            KamikoData.CODEC.fieldOf("kamiko_data").forGetter(KamikotizationData::kamikoData),
            Codec.STRING.optionalFieldOf("name", "").forGetter(KamikotizationData::name),
            Codec.either(Codec.INT, Codec.INT).optionalFieldOf("transformation_frames").forGetter(KamikotizationData::transformationFrames),
            Codec.INT.fieldOf("remaining_stack_count").forGetter(KamikotizationData::remainingStackCount),
            Codec.BOOL.fieldOf("power_active").forGetter(KamikotizationData::powerActive)).apply(instance, KamikotizationData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KamikotizationData> STREAM_CODEC = StreamCodec.composite(
            Kamikotization.STREAM_CODEC, KamikotizationData::kamikotization,
            KamikoData.STREAM_CODEC, KamikotizationData::kamikoData,
            ByteBufCodecs.STRING_UTF8, KamikotizationData::name,
            ByteBufCodecs.optional(ByteBufCodecs.either(ByteBufCodecs.INT, ByteBufCodecs.INT)), KamikotizationData::transformationFrames,
            ByteBufCodecs.INT, KamikotizationData::remainingStackCount,
            ByteBufCodecs.BOOL, KamikotizationData::powerActive,
            KamikotizationData::new);

    private static final int TRANSFORMATION_FRAMES = 10;
    public ItemStack transform(Entity entity, ServerLevel level, ItemStack originalStack) {
        if (entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
            Mineraculous.LOGGER.error("Tried to kamikotize currently powered entity: {}", entity.getName().plainCopy().getString());
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
        if (entity instanceof Player player) {
            kamikotizationStack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
        }

        AbilityReversionItemData.get(level).putKamikotized(entity.getUUID(), originalStack);

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), MineraculousSoundEvents.KAMIKOTIZATION_TRANSFORM, entity.getSoundSource(), 1, 1);
        if (entity instanceof LivingEntity livingEntity) {
            level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((effect, startLevel) -> MineraculousEntityUtils.applyInfiniteHiddenEffect(livingEntity, level.holderOrThrow(effect), startLevel));
        }

        AbilityData abilityData = new AbilityData(0, Either.right(kamikotization), false);
        value.powerSource().right().ifPresent(ability -> ability.value().transform(abilityData, level, entity));
        value.passiveAbilities().forEach(ability -> ability.value().transform(abilityData, level, entity));
        AbilityReversionEntityData.get(level).startTracking(entity.getUUID());

        startTransformation(kamikotizationStack.getCount()).save(entity, true);

        if (entity instanceof Player player) {
            player.refreshDisplayName();
        }

        return kamikotizationStack;
    }

    public void detransform(Entity entity, ServerLevel level, Vec3 kamikoSpawnPos, boolean instant) {
        Kamiko kamiko = kamikoData.summon(level, kamikoSpawnPos);
        if (kamiko == null) {
            Mineraculous.LOGGER.error("Kamiko could not be created for player {}", entity.getName().plainCopy().getString());
        }

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), MineraculousSoundEvents.KAMIKOTIZATION_DETRANSFORM, entity.getSoundSource(), 1, 1);
        if (entity instanceof LivingEntity livingEntity) {
            level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).keySet().forEach(effect -> livingEntity.removeEffect(level.holderOrThrow(effect)));
        }

        Kamikotization value = kamikotization.value();
        AbilityData abilityData = new AbilityData(0, Either.right(kamikotization), powerActive);
        value.powerSource().right().ifPresent(ability -> ability.value().detransform(abilityData, level, entity));
        value.passiveAbilities().forEach(ability -> ability.value().detransform(abilityData, level, entity));
        if (instant) {
            finishDetransformation(entity);
        } else {
            startDetransformation().save(entity, true);
        }

        if (entity instanceof Player player) {
            player.refreshDisplayName();
        }
    }

    public void tick(Entity entity, ServerLevel level) {
        transformationFrames.ifPresentOrElse(either -> either.ifLeft(transformationFrames -> {
            if (transformationFrames > 0) {
                if (entity.tickCount % 2 == 0) {
                    decrementTransformationFrames().save(entity, true);
                }
                level.sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), entity.getX(), entity.getY() + 2 - ((Kamikotization.TRANSFORMATION_FRAMES + 1) - transformationFrames) / 5.0, entity.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
            } else {
                finishTransformation(entity);
                clearTransformationFrames().save(entity, true);
            }
        }).ifRight(detransformationFrames -> {
            if (detransformationFrames > 0) {
                if (entity.tickCount % 2 == 0) {
                    decrementDetransformationFrames().save(entity, true);
                }
                level.sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), entity.getX(), entity.getY() + 2 - ((Kamikotization.TRANSFORMATION_FRAMES + 1) - detransformationFrames) / 5.0, entity.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
            } else {
                finishDetransformation(entity);
            }
        }), () -> {
            if (entity instanceof LivingEntity livingEntity) {
                level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((key, startLevel) -> {
                    Holder<MobEffect> effect = level.holderOrThrow(key);
                    if (!livingEntity.hasEffect(effect)) {
                        MineraculousEntityUtils.applyInfiniteHiddenEffect(livingEntity, effect, startLevel);
                    }
                });
            }

            performAbilities(level, entity, null);
        });
    }

    public void performAbilities(ServerLevel level, Entity entity, @Nullable AbilityContext abilityContext) {
        AbilityData abilityData = new AbilityData(0, Either.right(kamikotization), powerActive);
        boolean overrideActive = AbilityUtils.performPassiveAbilities(level, entity, abilityData, abilityContext, kamikotization.value().passiveAbilities());
        if (powerActive && overrideActive) {
            withPowerActive(false).save(entity, true);
        } else if (powerActive) {
            boolean consumeMainPower = AbilityUtils.performActiveAbility(level, entity, abilityData, abilityContext, kamikotization.value().powerSource().right());
            if (consumeMainPower) {
                if (abilityContext != null && entity instanceof ServerPlayer player) {
                    MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(player, kamikotization.getKey(), abilityContext.advancementContext());
                }
            }
            withPowerActive(!consumeMainPower).save(entity, true);
        }
    }

    private void finishTransformation(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            ArmorData armor = new ArmorData(livingEntity.getItemBySlot(EquipmentSlot.HEAD), livingEntity.getItemBySlot(EquipmentSlot.CHEST), livingEntity.getItemBySlot(EquipmentSlot.LEGS), livingEntity.getItemBySlot(EquipmentSlot.FEET));
            livingEntity.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(armor));
            for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                ItemStack stack = Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.getForSlot(slot).get(), kamikotization);
                stack.enchant(entity.level().holderOrThrow(Enchantments.BINDING_CURSE), 1);
                stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS, Unit.INSTANCE);
                livingEntity.setItemSlot(slot, stack);
            }
        }
    }

    private void finishDetransformation(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.getData(MineraculousAttachmentTypes.STORED_ARMOR).ifPresent(data -> data.equipAndClear(livingEntity));
        }
        remove(entity, true);
    }

    private KamikotizationData startTransformation(int stackCount) {
        return new KamikotizationData(kamikotization, kamikoData, name, Optional.of(Either.left(TRANSFORMATION_FRAMES)), stackCount, false);
    }

    private KamikotizationData startDetransformation() {
        return new KamikotizationData(kamikotization, kamikoData, name, Optional.of(Either.right(TRANSFORMATION_FRAMES)), 0, false);
    }

    private KamikotizationData decrementTransformationFrames() {
        return new KamikotizationData(kamikotization, kamikoData, name, transformationFrames.map(either -> either.mapLeft(frames -> frames - 1)), remainingStackCount, powerActive);
    }

    private KamikotizationData decrementDetransformationFrames() {
        return new KamikotizationData(kamikotization, kamikoData, name, transformationFrames.map(either -> either.mapRight(frames -> frames - 1)), remainingStackCount, powerActive);
    }

    private KamikotizationData clearTransformationFrames() {
        return new KamikotizationData(kamikotization, kamikoData, name, Optional.empty(), remainingStackCount, powerActive);
    }

    public KamikotizationData decrementRemainingStackCount() {
        return new KamikotizationData(kamikotization, kamikoData, name, transformationFrames, remainingStackCount - 1, powerActive);
    }

    public KamikotizationData withPowerActive(boolean powerActive) {
        return new KamikotizationData(kamikotization, kamikoData, name, transformationFrames, remainingStackCount, powerActive);
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.KAMIKOTIZATION, Optional.of(this));
        if (entity.getData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION).isPresent())
            entity.setData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION, Optional.empty());
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.KAMIKOTIZATION, Optional.of(this)), entity.getServer());
    }

    public static void remove(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION, entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).map(KamikotizationData::kamikotization));
        entity.setData(MineraculousAttachmentTypes.KAMIKOTIZATION, Optional.empty());
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.KAMIKOTIZATION, Optional.<Optional<KamikotizationData>>empty()), entity.getServer());
    }
}
