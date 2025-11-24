package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.gui.screens.packs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.thomasglasser.mineraculous.impl.server.packs.repository.MineraculousPackCompatabilityHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackCompatibility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TransferableSelectionList.PackEntry.class)
public abstract class TransferableSelectionListPackEntryMixin {
    @Shadow
    private static MultiLineLabel cacheDescription(Minecraft minecraft, Component text) {
        return null;
    }

    @Shadow
    @Final
    private PackSelectionModel.Entry pack;
    @Unique
    private MultiLineLabel mineraculous$incompatibleDescriptionDisplayCache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Minecraft minecraft, TransferableSelectionList parent, PackSelectionModel.Entry pack, CallbackInfo ci) {
        mineraculous$incompatibleDescriptionDisplayCache = cacheDescription(minecraft, ((MineraculousPackCompatabilityHolder) pack).mineraculous$getPackCompatibility().getDescription());
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackCompatibility;isCompatible()Z"))
    private boolean checkMiraculousPackCompatibility(PackCompatibility instance, Operation<Boolean> original) {
        return original.call(instance) && ((MineraculousPackCompatabilityHolder) pack).mineraculous$getPackCompatibility().isCompatible();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0), index = 4)
    private int fillOrange(int color) {
        if (pack.getCompatibility().isCompatible()) {
            return 0xFFCC5312;
        }
        return color;
    }

    @WrapOperation(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/packs/TransferableSelectionList$PackEntry;incompatibleDescriptionDisplayCache:Lnet/minecraft/client/gui/components/MultiLineLabel;"))
    private MultiLineLabel wrapWithMiraculousPackCompatibilityDescription(TransferableSelectionList.PackEntry instance, Operation<MultiLineLabel> original) {
        if (pack.getCompatibility().isCompatible()) {
            return mineraculous$incompatibleDescriptionDisplayCache;
        }
        return original.call(instance);
    }

    @WrapOperation(method = "handlePackSelection", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackCompatibility;isCompatible()Z"))
    private boolean checkMineraculousPackCompatibility(PackCompatibility instance, Operation<Boolean> original) {
        return original.call(instance) && ((MineraculousPackCompatabilityHolder) pack).mineraculous$getPackCompatibility().isCompatible();
    }

    @WrapOperation(method = "handlePackSelection", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackCompatibility;getConfirmation()Lnet/minecraft/network/chat/Component;"))
    private Component showMiraculousPackConfirmation(PackCompatibility instance, Operation<Component> original) {
        if (pack.getCompatibility().isCompatible()) {
            return ((MineraculousPackCompatabilityHolder) pack).mineraculous$getPackCompatibility().getConfirmation();
        }
        return original.call(instance);
    }
}
