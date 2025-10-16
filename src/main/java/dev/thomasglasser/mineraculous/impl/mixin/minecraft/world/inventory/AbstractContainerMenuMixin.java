package dev.thomasglasser.mineraculous.impl.mixin.minecraft.world.inventory;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Shadow
    public abstract Slot getSlot(int slotId);

    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void preventKamikotizingClick(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (slotId > 0 && slotId < slots.size() && getSlot(slotId).getItem().has(MineraculousDataComponents.KAMIKOTIZING))
            ci.cancel();
    }
}
