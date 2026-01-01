package dev.thomasglasser.mineraculous.impl.core.look.context.kamikotization;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.look.asset.LookAssetTypeKeys;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class KamikotizationLookContext implements LookContext {
    private static final ImmutableSet<ResourceLocation> ASSET_TYPES = ImmutableSet.of(
            LookAssetTypeKeys.TEXTURE,
            LookAssetTypeKeys.GECKOLIB_MODEL,
            LookAssetTypeKeys.GECKOLIB_ANIMATIONS);

    @Override
    public ImmutableSet<ResourceLocation> assetTypes() {
        return ASSET_TYPES;
    }

    @Override
    public void preparePreview(Player player, Holder<?> selected) {
        if (!(selected.value() instanceof Kamikotization value)) {
            throw new IllegalArgumentException("Passed non-kamikotization selection to KamikotizationLookContext: " + selected);
        }

        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            ItemStack stack = Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.getForSlot(slot), (Holder<Kamikotization>) selected);
            stack.set(MineraculousDataComponents.OWNER, player.getUUID());
            player.setItemSlot(slot, stack);
        }

        value.powerSource().left().ifPresent(tool -> {
            tool.set(MineraculousDataComponents.OWNER, player.getUUID());
            player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        });
    }
}
