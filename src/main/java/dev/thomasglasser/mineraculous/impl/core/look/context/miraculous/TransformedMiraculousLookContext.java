package dev.thomasglasser.mineraculous.impl.core.look.context.miraculous;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class TransformedMiraculousLookContext implements LookContext {
    @Override
    public void preparePreview(Player player, Holder<?> selected) {
        if (!(selected.value() instanceof Miraculous value)) {
            throw new IllegalArgumentException("Passed non-miraculous selection to TransformedMiraculousLookContext: " + selected);
        }
        Holder<Miraculous> miraculous = (Holder<Miraculous>) selected;
        ItemStack jewel = Miraculous.createMiraculousStack(miraculous);
        jewel.set(MineraculousDataComponents.OWNER, player.getUUID());
        jewel.set(MineraculousDataComponents.POWER_STATE, MiraculousItem.PowerState.POWERED);
        CuriosUtils.setStackInFirstValidSlot(player, jewel);

        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            ItemStack stack = Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.getForSlot(slot), miraculous);
            stack.set(MineraculousDataComponents.OWNER, player.getUUID());
            player.setItemSlot(slot, stack);
        }

        ItemStack tool = value.tool();
        tool.set(MineraculousDataComponents.OWNER, player.getUUID());
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
    }
}
