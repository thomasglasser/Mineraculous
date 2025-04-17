package dev.thomasglasser.mineraculous.client.animations;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.network.ClientboundPlayerAnimationPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;

public class MineraculousPlayerAnimationUtil {
    public enum PlayerAnimationActions implements StringRepresentable {
        PLAY,
        STOP;

        public static final Codec<PlayerAnimationActions> CODEC = StringRepresentable.fromEnum(PlayerAnimationActions::values);
        public static final StreamCodec<ByteBuf, PlayerAnimationActions> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(PlayerAnimationActions::of, PlayerAnimationActions::getSerializedName);

        public static PlayerAnimationActions of(String name) {
            return valueOf(name.toUpperCase());
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    public static void sendAnimationToAllClients(Player player, String animationName, PlayerAnimationActions action) {
        if (player instanceof ServerPlayer serverPlayer)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundPlayerAnimationPayload(player.getUUID(), animationName, action), serverPlayer.server);
    }
}
