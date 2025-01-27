package dev.thomasglasser.mineraculous.mixin.minecraft.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.NightVisionAbility;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
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
            CompoundTag data = TommyLibServices.ENTITY.getPersistentData(ClientUtils.getMainClientPlayer());
            if (data.getBoolean(MineraculousEntityEvents.TAG_HAS_NIGHT_VISION)) {
                ClientUtils.getMainClientPlayer().getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed(ClientUtils.getLevel().registryAccess()).forEach(type -> {
                    if (type.activeAbility().isPresent() && type.activeAbility().get().value() instanceof NightVisionAbility nightVisionAbility && nightVisionAbility.shader().isPresent()) {
                        MineraculousClientUtils.setShader(nightVisionAbility.shader().get());
                    } else {
                        type.passiveAbilities().stream().filter(abilityHolder -> abilityHolder.value() instanceof NightVisionAbility nightVisionAbility && nightVisionAbility.shader().isPresent())
                                .findFirst().ifPresent(abilityHolder -> MineraculousClientUtils.setShader(((NightVisionAbility) abilityHolder.value()).shader().get()));
                    }
                });
            } else if (data.getBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK) && getCameraEntity() != ClientUtils.getMainClientPlayer()) {
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
