package dev.thomasglasser.mineraculous.mixin.minecraft.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public abstract Entity getCameraEntity();

    @Shadow
    @Final
    public GameRenderer gameRenderer;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void checkPostEffectOnKeyPress(CallbackInfo ci) {
        Player player = this.player;
        if (player != null) {
            player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).shader().ifPresentOrElse(MineraculousClientUtils::setShader, () -> this.gameRenderer.checkEntityPostEffect(getCameraEntity()));
        }
    }

    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean showPlayersGlowingInKamikoView(boolean original, Entity entity) {
        return original || (getCameraEntity() instanceof Kamiko && entity instanceof Player);
    }
}
