package dev.thomasglasser.mineraculous.mixin.minecraft.client;

import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.NightVisionAbility;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void handleKeybinds(CallbackInfo ci) {
        if (Minecraft.getInstance().gameRenderer.currentPostEffect() == null) {
            if (TommyLibServices.ENTITY.getPersistentData(ClientUtils.getMainClientPlayer()).getBoolean(MineraculousEntityEvents.TAG_HASNIGHTVISION)) {
                ClientUtils.getMainClientPlayer().getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed(ClientUtils.getLevel().registryAccess()).forEach(type -> {
                    if (type.activeAbility().isPresent() && type.activeAbility().get().value() instanceof NightVisionAbility nightVisionAbility && nightVisionAbility.shader().isPresent()) {
                        MineraculousClientUtils.setShader(nightVisionAbility.shader().get());
                    } else {
                        type.passiveAbilities().stream().filter(abilityHolder -> abilityHolder.value() instanceof NightVisionAbility nightVisionAbility && nightVisionAbility.shader().isPresent())
                                .findFirst().ifPresent(abilityHolder -> MineraculousClientUtils.setShader(((NightVisionAbility) abilityHolder.value()).shader().get()));
                    }
                });
            } else {
                MineraculousClientUtils.setShader(null);
            }
        }
    }
}
