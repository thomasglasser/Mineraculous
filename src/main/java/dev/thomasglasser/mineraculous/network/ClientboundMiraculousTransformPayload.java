package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ClientboundMiraculousTransformPayload(ResourceKey<Miraculous> miraculousType, MiraculousData data) implements ExtendedPacketPayload {
    public static final Type<ClientboundMiraculousTransformPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_miraculous_transform"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundMiraculousTransformPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ClientboundMiraculousTransformPayload::miraculousType,
            MiraculousData.STREAM_CODEC, ClientboundMiraculousTransformPayload::data,
            ClientboundMiraculousTransformPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        if (data.transformed()) {
            // TODO: Start player anim
            data.miraculousItem().set(MineraculousDataComponents.TRANSFORMING_ANIM_TICKS.get(), 0 /* TODO: Set it to however long the transform anim is */);
            if (data.name().isEmpty())
                player.displayClientMessage(Component.translatable(MiraculousData.NAME_NOT_SET, Component.translatable(Miraculous.toLanguageKey(miraculousType)), miraculousType.location().getPath()), true);
        } else {
            // TODO: Start player anim
            data.miraculousItem().set(MineraculousDataComponents.DETRANSFORMING_ANIM_TICKS.get(), 0 /* TODO: Set it to however long the detransform anim is */);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
