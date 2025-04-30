package dev.thomasglasser.mineraculous.mixin.minecraft.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.ability.NightVisionAbility;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
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
        if (Minecraft.getInstance().gameRenderer.postEffect == null) {
            // TODO: Fix
            CompoundTag data = /*TommyLibServices.ENTITY.getPersistentData(Minecraft.getInstance().player)*/new CompoundTag();
            if (data.getBoolean(MineraculousEntityEvents.TAG_HAS_NIGHT_VISION)) {
                MiraculousDataSet miraculousDataSet = Minecraft.getInstance().player.getData(MineraculousAttachmentTypes.MIRACULOUS);
                miraculousDataSet.getTransformedHolders(ClientUtils.getLevel().registryAccess()).forEach(miraculous -> {
                    NightVisionAbility nightVisionAbility = Ability.getFirstMatching(ability -> ability instanceof NightVisionAbility n && n.shader().isPresent(), miraculous.value(), miraculousDataSet.get(miraculous.getKey()).mainPowerActive()) instanceof NightVisionAbility n ? n : null;
                    if (nightVisionAbility != null) {
                        MineraculousClientUtils.setShader(nightVisionAbility.shader().get());
                    }
                });
                if (Minecraft.getInstance().player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
                    KamikotizationData kamikotizationData = Minecraft.getInstance().player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).get();
                    NightVisionAbility nightVisionAbility = Ability.getFirstMatching(ability -> ability instanceof NightVisionAbility n && n.shader().isPresent(), ClientUtils.getLevel().holderOrThrow(kamikotizationData.kamikotization()).value(), kamikotizationData.mainPowerActive()) instanceof NightVisionAbility n ? n : null;
                    if (nightVisionAbility != null) {
                        MineraculousClientUtils.setShader(nightVisionAbility.shader().get());
                    }
                }
            } else if (data.getBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK) && getCameraEntity() != Minecraft.getInstance().player) {
                MineraculousClientUtils.setShader(Kamiko.SPECTATOR_SHADER);
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
