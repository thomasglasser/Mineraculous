package dev.thomasglasser.mineraculous.impl.core.look.context.miraculous;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.look.asset.LookAssetTypeKeys;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HiddenMiraculousLookContext implements LookContext {
    private static final ImmutableSet<ResourceLocation> ASSET_TYPES = ImmutableSet.of(
            LookAssetTypeKeys.TEXTURE,
            LookAssetTypeKeys.GECKOLIB_MODEL,
            LookAssetTypeKeys.GECKOLIB_ANIMATIONS,
            LookAssetTypeKeys.ITEM_TRANSFORMS);

    @Override
    public ImmutableSet<ResourceLocation> assetTypes() {
        return ASSET_TYPES;
    }

    @Override
    public void preparePreview(Player player, Holder<?> selected) {
        if (!(selected.value() instanceof Miraculous)) {
            throw new IllegalArgumentException("Passed non-miraculous selection to HiddenMiraculousLookContext: " + selected);
        }

        ItemStack jewel = Miraculous.createMiraculousStack((Holder<Miraculous>) selected);
        jewel.set(MineraculousDataComponents.OWNER, player.getUUID());
        jewel.set(MineraculousDataComponents.POWER_STATE, MiraculousItem.PowerState.HIDDEN);
        CuriosUtils.setStackInFirstValidSlot(player, jewel);
    }
}
