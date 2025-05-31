package dev.thomasglasser.mineraculous.mixin.minecraft.server.level;

import dev.thomasglasser.mineraculous.network.ClientboundSyncInventoryPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/level/ServerPlayer$2")
public class ServerPlayer$2Mixin {
    @Shadow
    @Final
    ServerPlayer this$0;

    @Inject(method = "slotChanged", at = @At("TAIL"))
    private void updateInventoryOnSlotChange(AbstractContainerMenu container, int slot, ItemStack itemStack, CallbackInfo ci) {
        for (UUID uuid : this$0.getData(MineraculousAttachmentTypes.INVENTORY_TRACKERS)) {
            if (this$0.level().getPlayerByUUID(uuid) instanceof ServerPlayer tracker) {
                TommyLibServices.NETWORK.sendToClient(new ClientboundSyncInventoryPayload(this$0), tracker);
            }
        }
    }
}
