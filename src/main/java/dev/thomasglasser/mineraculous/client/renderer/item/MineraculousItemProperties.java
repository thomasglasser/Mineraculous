package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.player.Player;

public class MineraculousItemProperties {
    public static void init() {
        ItemProperties.register(MineraculousItems.LADYBUG_YOYO.get(), LadybugYoyoItem.EXTENDED_PROPERTY_ID, (stack, level, entity, seed) -> stack.has(MineraculousDataComponents.ACTIVE) ? 1 : 0);
        ItemProperties.register(MineraculousItems.LADYBUG_YOYO.get(), LadybugYoyoItem.THROWN_PROPERTY_ID, (stack, level, entity, seed) -> entity instanceof Player player && player.getMainHandItem() == stack && player.getData(MineraculousAttachmentTypes.LADYBUG_YOYO).isPresent() ? 1 : 0);
        ItemProperties.register(MineraculousItems.CAT_STAFF.get(), CatStaffItem.EXTENDED_PROPERTY_ID, (stack, level, entity, seed) -> stack.has(MineraculousDataComponents.ACTIVE) ? 1 : 0);
        ItemProperties.register(MineraculousItems.BUTTERFLY_CANE.get(), ButterflyCaneItem.BLADE_PROPERTY_ID, (stack, level, entity, seed) -> stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY) == ButterflyCaneItem.Ability.BLADE ? 1 : 0);
    }
}
