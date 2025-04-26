package dev.thomasglasser.mineraculous.network;

import static dev.thomasglasser.mineraculous.Mineraculous.MOD_ID;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.animations.MineraculousPlayerAnimationUtil;
import dev.thomasglasser.mineraculous.client.animations.MineraculousPlayerAnimations;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record ClientboundPlayerAnimationPayload(UUID senderID, String animation, MineraculousPlayerAnimationUtil.PlayerAnimationActions action) implements ExtendedPacketPayload {

    public static final Type<ClientboundPlayerAnimationPayload> TYPE = new Type<>(Mineraculous.modLoc("play_player_animation"));
    public static final StreamCodec<ByteBuf, ClientboundPlayerAnimationPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundPlayerAnimationPayload::senderID,
            ByteBufCodecs.STRING_UTF8, ClientboundPlayerAnimationPayload::animation,
            MineraculousPlayerAnimationUtil.PlayerAnimationActions.STREAM_CODEC, ClientboundPlayerAnimationPayload::action,
            ClientboundPlayerAnimationPayload::new);
    @Override
    public void handle(Player player) {
        Player sender = player.level().getPlayerByUUID(senderID);
        if (sender != null) {
            var animationP = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) sender).get(ResourceLocation.fromNamespaceAndPath(MOD_ID, "animation"));
            if (animationP != null) {
                KeyframeAnimationPlayer newAnimation = new KeyframeAnimationPlayer((KeyframeAnimation) PlayerAnimationRegistry.getAnimation(ResourceLocation.fromNamespaceAndPath(MOD_ID, animation)));
                KeyframeAnimationPlayer oldAnimation;
                String oldAnimationName;
                int cTick, lTick;
                if (animationP.getAnimation() != null) {
                    oldAnimation = ((KeyframeAnimationPlayer) animationP.getAnimation());
                    oldAnimationName = oldAnimation.getData().getName();
                    cTick = oldAnimation.getCurrentTick();
                    lTick = oldAnimation.getStopTick();
                } else {
                    oldAnimationName = "null";
                    cTick = 0;
                    lTick = 0;
                }
                boolean same = animation.equals(oldAnimationName);
                switch (action) {
                    case PLAY_ONCE -> {
                        if (!same || cTick == lTick)
                            animationP.setAnimation(newAnimation);
                    }
                    case PLAY_HOLD_ON_FIRST_FRAME -> {
                        newAnimation = new KeyframeAnimationPlayer((KeyframeAnimation) PlayerAnimationRegistry.getAnimation(ResourceLocation.fromNamespaceAndPath(MOD_ID, animation)), 0);
                        if (!same)
                            animationP.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(6, Ease.CONSTANT), newAnimation, false);
                        else animationP.setAnimation(newAnimation);
                    }
                    case PLAY_HOLD_ON_LAST_FRAME -> {
                        if (cTick >= newAnimation.getStopTick() - MineraculousPlayerAnimations.lastFrame(animation))
                            newAnimation = new KeyframeAnimationPlayer((KeyframeAnimation) PlayerAnimationRegistry.getAnimation(ResourceLocation.fromNamespaceAndPath(MOD_ID, animation)), newAnimation.getStopTick() - 5);
                        if (!same || cTick >= newAnimation.getStopTick() - MineraculousPlayerAnimations.lastFrame(animation) + 1)
                            animationP.setAnimation(newAnimation);
                    }
                    case STOP -> {
                        newAnimation.stop();
                        animationP.setAnimation(newAnimation);
                    }
                }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
