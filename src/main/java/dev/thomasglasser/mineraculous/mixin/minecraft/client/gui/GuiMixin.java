package dev.thomasglasser.mineraculous.mixin.minecraft.client.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Unique
    private final ResourceLocation mineraculous$full = Mineraculous.modLoc("hud/heart/cataclysmed_full");
    @Unique
    private final ResourceLocation mineraculous$fullBlinking = Mineraculous.modLoc("hud/heart/cataclysmed_full_blinking");
    @Unique
    private final ResourceLocation mineraculous$half = Mineraculous.modLoc("hud/heart/cataclysmed_half");
    @Unique
    private final ResourceLocation mineraculous$halfBlinking = Mineraculous.modLoc("hud/heart/cataclysmed_half_blinking");
    @Unique
    private final ResourceLocation mineraculous$hardcoreFull = Mineraculous.modLoc("hud/heart/cataclysmed_hardcore_full");
    @Unique
    private final ResourceLocation mineraculous$hardcoreFullBlinking = Mineraculous.modLoc("hud/heart/cataclysmed_hardcore_full_blinking");
    @Unique
    private final ResourceLocation mineraculous$hardcoreHalf = Mineraculous.modLoc("hud/heart/cataclysmed_hardcore_half");
    @Unique
    private final ResourceLocation mineraculous$hardcoreHalfBlinking = Mineraculous.modLoc("hud/heart/cataclysmed_hardcore_half_blinking");

    @ModifyExpressionValue(method = "renderHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui$HeartType;getSprite(ZZZ)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation renderHeart(ResourceLocation original, GuiGraphics pGuiGraphics, Gui.HeartType pHeartType, int pX, int pY, boolean pHardcore, boolean pHalfHeart, boolean pBlinking) {
        // TODO: Add cataclysmed heart textures
        //		if (MineraculousEntityEvents.isCataclysmed(ClientUtils.getMainClientPlayer()))
//		{
//			return mineraculous$getSprite(pHardcore, pHalfHeart, pBlinking);
//		}
        return original;
    }

    @Unique
    private ResourceLocation mineraculous$getSprite(boolean pHardcore, boolean pHalfHeart, boolean pBlinking) {
        if (!pHardcore) {
            if (pHalfHeart) {
                return pBlinking ? this.mineraculous$halfBlinking : this.mineraculous$half;
            } else {
                return pBlinking ? this.mineraculous$fullBlinking : this.mineraculous$full;
            }
        } else if (pHalfHeart) {
            return pBlinking ? this.mineraculous$hardcoreHalfBlinking : this.mineraculous$hardcoreHalf;
        } else {
            return pBlinking ? this.mineraculous$hardcoreFullBlinking : this.mineraculous$hardcoreFull;
        }
    }
}
