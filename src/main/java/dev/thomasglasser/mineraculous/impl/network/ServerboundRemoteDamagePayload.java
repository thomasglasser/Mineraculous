package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityEffectData;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ServerboundRemoteDamagePayload implements ExtendedPacketPayload {
    public static final ServerboundRemoteDamagePayload INSTANCE = new ServerboundRemoteDamagePayload();
    public static final Type<ServerboundRemoteDamagePayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_remote_damage"));
    public static final StreamCodec<ByteBuf, ServerboundRemoteDamagePayload> CODEC = StreamCodec.unit(INSTANCE);

    private ServerboundRemoteDamagePayload() {}

    @Override
    public void handle(Player player) {
        AbilityEffectData abilityEffectData = player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
        if (abilityEffectData.spectatingId().isPresent() && abilityEffectData.allowRemoteDamage()) {
            ServerLevel level = (ServerLevel) player.level();
            Entity target = level.getEntity(abilityEffectData.spectatingId().get());
            if (target instanceof LivingEntity livingEntity && livingEntity.getHealth() > 4) {
                target.hurt(level.damageSources().playerAttack(player), 20);
                target.hurtMarked = true;
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
