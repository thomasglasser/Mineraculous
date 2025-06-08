package dev.thomasglasser.mineraculous.mixin.minecraft.world.entity.player;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.network.ServerboundHurtEntityPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow
    public abstract boolean isLocalPlayer();

    @Unique
    private final Player mineraculous$instance = (Player) (Object) this;

    private PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Component formatMiraculousName(Component original) {
        return MineraculousEntityUtils.formatDisplayName(mineraculous$instance, original);
    }

    @ModifyReturnValue(method = "decorateDisplayNameComponent", at = @At(value = "RETURN"))
    private MutableComponent formatMiraculousDisplayName(MutableComponent original) {
        return MineraculousEntityUtils.formatDisplayName(mineraculous$instance, original).copy();
    }

    // TODO: Check, and find a way to prevent block breaking
    // Hurt camera when transformed
    @Override
    public void swing(InteractionHand hand, boolean updateSelf) {
        if (getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
            Entity camera = isLocalPlayer() ? MineraculousClientUtils.getCameraEntity() : mineraculous$instance instanceof ServerPlayer serverPlayer ? serverPlayer.getCamera() : null;
            if (camera instanceof Player target && mineraculous$instance != target && target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() && target.getHealth() > 4) {
                if (isLocalPlayer())
                    TommyLibServices.NETWORK.sendToServer(new ServerboundHurtEntityPayload(target.getId(), DamageTypes.PLAYER_ATTACK, 15));
            }
            if (camera != null && camera != mineraculous$instance)
                return;
        }
        super.swing(hand, updateSelf);
    }
}
