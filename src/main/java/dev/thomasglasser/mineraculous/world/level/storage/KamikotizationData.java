package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.advancements.critereon.UseKamikotizationPowerTrigger;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Optional;

public record KamikotizationData(ResourceKey<Kamikotization> kamikotization, int stackCount, Either<Integer, CuriosData> slotInfo, KamikoData kamikoData, boolean powerActive, Optional<Either<Integer, Integer>> transformationFrames, String name) {

    public static final Codec<KamikotizationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).fieldOf("kamikotization").forGetter(KamikotizationData::kamikotization),
            Codec.INT.fieldOf("stack_count").forGetter(KamikotizationData::stackCount),
            Codec.either(Codec.INT, CuriosData.CODEC).fieldOf("slot_info").forGetter(KamikotizationData::slotInfo),
            KamikoData.CODEC.fieldOf("kamiko_data").forGetter(KamikotizationData::kamikoData),
            Codec.BOOL.fieldOf("power_active").forGetter(KamikotizationData::powerActive),
            Codec.either(Codec.INT, Codec.INT).optionalFieldOf("transformation_frames").forGetter(KamikotizationData::transformationFrames),
            Codec.STRING.optionalFieldOf("name", "").forGetter(KamikotizationData::name)).apply(instance, KamikotizationData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KamikotizationData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION), KamikotizationData::kamikotization,
            ByteBufCodecs.INT, KamikotizationData::stackCount,
            ByteBufCodecs.either(ByteBufCodecs.INT, CuriosData.STREAM_CODEC), KamikotizationData::slotInfo,
            KamikoData.STREAM_CODEC, KamikotizationData::kamikoData,
            ByteBufCodecs.BOOL, KamikotizationData::powerActive,
            ByteBufCodecs.optional(ByteBufCodecs.either(ByteBufCodecs.INT, ByteBufCodecs.INT)), KamikotizationData::transformationFrames,
            ByteBufCodecs.STRING_UTF8, KamikotizationData::name,
            KamikotizationData::new);

    public void tick(Entity entity, ServerLevel level) {
        transformationFrames.ifPresentOrElse(either -> either.ifLeft(transformationFrames -> {
            if (transformationFrames > 0) {
                if (entity.tickCount % 2 == 0) {
                    withTransformationFrames(transformationFrames - 1).save(entity, true);
                }
                level.sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), entity.getX(), entity.getY() + 2 - ((Kamikotization.TRANSFORMATION_FRAMES + 1) - transformationFrames) / 5.0, entity.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
            } else {
                if (entity instanceof LivingEntity livingEntity) {
                    ArmorData armor = new ArmorData(livingEntity.getItemBySlot(EquipmentSlot.HEAD), livingEntity.getItemBySlot(EquipmentSlot.CHEST), livingEntity.getItemBySlot(EquipmentSlot.LEGS), livingEntity.getItemBySlot(EquipmentSlot.FEET));
                    livingEntity.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(armor));
                    for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                        ItemStack stack = Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.getForSlot(slot).get(), kamikotization);
                        stack.enchant(level.holderOrThrow(Enchantments.BINDING_CURSE), 1);
                        stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS, Unit.INSTANCE);
                        livingEntity.setItemSlot(slot, stack);
                    }
                }
                if (entity instanceof Player player) {
                    player.refreshDisplayName();
                }
                clearTransformationFrames().save(entity, true);
            }
        }).ifRight(detransformationFrames -> {
            if (detransformationFrames > 0) {
                if (entity.tickCount % 2 == 0) {
                    withDetransformationFrames(detransformationFrames - 1).save(entity, true);
                }
                level.sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), entity.getX(), entity.getY() + 2 - ((Kamikotization.TRANSFORMATION_FRAMES + 1) - detransformationFrames) / 5.0, entity.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
            } else {
                remove(entity, true);
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.getData(MineraculousAttachmentTypes.STORED_ARMOR).ifPresent(data -> {
                        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                            livingEntity.setItemSlot(slot, data.forSlot(slot));
                        }
                    });
                }
                if (entity instanceof Player player) {
                    player.refreshDisplayName();
                }
                clearTransformationFrames().save(entity, true);
            }
        }), () -> {
            Kamikotization kamikotizationValue = entity.level().holderOrThrow(kamikotization).value();
            boolean overrideActive = false;
            AbilityData abilityData = new AbilityData(0, Either.right(kamikotization));
            for (Holder<Ability> abilityHolder : kamikotizationValue.passiveAbilities()) {
                Ability ability = abilityHolder.value();
                if (ability.perform(abilityData, level, entity, null) && ability.overrideActive(null))
                    overrideActive = true;
            }
            if (powerActive) {
                if (overrideActive) {
                    withPowerActive(false).save(entity, true);
                } else {
                    boolean usedPower = kamikotizationValue.powerSource().right().map(ability -> ability.value().perform(abilityData, level, entity, null)).orElse(true);
                    if (usedPower) {
                        withPowerActive(false).save(entity, true);
                        if (entity instanceof ServerPlayer serverPlayer) {
                            MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(serverPlayer, kamikotization, UseKamikotizationPowerTrigger.Context.PASSIVE);
                        }
                    }
                }
            }
        });
    }

    public KamikotizationData withStackCount(int stackCount) {
        return new KamikotizationData(kamikotization, stackCount, slotInfo, kamikoData, powerActive, transformationFrames, name);
    }

    public KamikotizationData decrementStackCount() {
        return new KamikotizationData(kamikotization, stackCount - 1, slotInfo, kamikoData, powerActive, transformationFrames, name);
    }

    public KamikotizationData withPowerActive(boolean powerActive) {
        return new KamikotizationData(kamikotization, stackCount, slotInfo, kamikoData, powerActive, transformationFrames, name);
    }

    public KamikotizationData withTransformationFrames(int transformationFrames) {
        return new KamikotizationData(kamikotization, stackCount, slotInfo, kamikoData, powerActive, Optional.of(Either.left(transformationFrames)), name);
    }

    public KamikotizationData withDetransformationFrames(int detransformationFrames) {
        return new KamikotizationData(kamikotization, stackCount, slotInfo, kamikoData, powerActive, Optional.of(Either.right(detransformationFrames)), name);
    }

    public KamikotizationData clearTransformationFrames() {
        return new KamikotizationData(kamikotization, stackCount, slotInfo, kamikoData, powerActive, Optional.empty(), name);
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
