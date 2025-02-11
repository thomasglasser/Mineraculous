package dev.thomasglasser.mineraculous.network;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.advancements.critereon.KamikotizationUsePowerTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.MiraculousUsePowerTrigger;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record ServerboundSendEmptyLeftClickPayload(int entityId) implements ExtendedPacketPayload {
    public static final Type<ServerboundSendEmptyLeftClickPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_send_empty_left_click"));
    public static final StreamCodec<ByteBuf, ServerboundSendEmptyLeftClickPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSendEmptyLeftClickPayload::entityId,
            ServerboundSendEmptyLeftClickPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        Entity entity = level.getEntity(entityId);
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
                Miraculous miraculous = level.holderOrThrow(key).value();
                MiraculousData data = livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(data.powerLevel(), Either.left(key));
                miraculous.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, livingEntity.blockPosition(), livingEntity) && ability.perform(abilityData, level, livingEntity.blockPosition(), livingEntity, Ability.Context.from()) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.mainPowerActive()) {
                    if (overrideActive.get()) {
                        livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS).put(livingEntity, key, data.withPowerStatus(false, false), true);
                    } else {
                        boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(abilityData, level, livingEntity.blockPosition(), livingEntity, Ability.Context.from());
                        if (usedPower) {
                            livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS).put(livingEntity, key, data.withUsedPower(), true);
                            if (livingEntity instanceof ServerPlayer serverPlayer) {
                                MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(serverPlayer, key, MiraculousUsePowerTrigger.Context.EMPTY);
                            }
                        }
                    }
                }
            });
            livingEntity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                ResourceKey<Kamikotization> key = data.kamikotization();
                Kamikotization kamikotization = level.holderOrThrow(key).value();
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(0, Either.right(key));
                kamikotization.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, livingEntity.blockPosition(), livingEntity) && ability.perform(abilityData, level, livingEntity.blockPosition(), livingEntity, Ability.Context.from()) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.mainPowerActive()) {
                    if (!overrideActive.get()) {
                        boolean usedPower = kamikotization.activeAbility().isPresent() && kamikotization.activeAbility().get().value().perform(abilityData, level, livingEntity.blockPosition(), livingEntity, Ability.Context.from());
                        if (usedPower) {
                            data.withMainPowerActive(false).save(livingEntity, true);
                            if (livingEntity instanceof ServerPlayer serverPlayer) {
                                MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(serverPlayer, key, KamikotizationUsePowerTrigger.Context.EMPTY);
                            }
                        }
                    } else
                        data.withMainPowerActive(false).save(livingEntity, true);
                }
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
