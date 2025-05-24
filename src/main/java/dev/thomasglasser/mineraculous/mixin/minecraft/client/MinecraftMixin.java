package dev.thomasglasser.mineraculous.mixin.minecraft.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.ability.NightVisionAbility;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityEffectData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import net.minecraft.client.Minecraft;
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

    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void handleKeybinds(CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (Minecraft.getInstance().gameRenderer.postEffect == null && player != null) {
            AbilityEffectData abilityEffectData = player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
            if (abilityEffectData.hasNightVision()) {
                MiraculousDataSet miraculousDataSet = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
                miraculousDataSet.getTransformedHolders(player.level().registryAccess()).forEach(miraculous -> {
                    NightVisionAbility nightVisionAbility = Ability.getFirstMatching(ability -> ability instanceof NightVisionAbility n && n.shader().isPresent(), miraculous.value(), miraculousDataSet.get(miraculous.getKey()).mainPowerActive()) instanceof NightVisionAbility n ? n : null;
                    if (nightVisionAbility != null) {
                        nightVisionAbility.shader().ifPresent(MineraculousClientUtils::setShader);
                    }
                });
                if (player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
                    KamikotizationData kamikotizationData = player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).get();
                    NightVisionAbility nightVisionAbility = Ability.getFirstMatching(ability -> ability instanceof NightVisionAbility n && n.shader().isPresent(), player.level().holderOrThrow(kamikotizationData.kamikotization()).value(), kamikotizationData.mainPowerActive()) instanceof NightVisionAbility n ? n : null;
                    if (nightVisionAbility != null) {
                        nightVisionAbility.shader().ifPresent(MineraculousClientUtils::setShader);
                    }
                }
            } else {
                gameRenderer.checkEntityPostEffect(getCameraEntity());
            }
        }
    }

    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean shouldEntityAppearGlowing(boolean original, Entity entity) {
        if (getCameraEntity() instanceof Kamiko && entity instanceof Player) {
            return true;
        }
        return original;
    }
}
