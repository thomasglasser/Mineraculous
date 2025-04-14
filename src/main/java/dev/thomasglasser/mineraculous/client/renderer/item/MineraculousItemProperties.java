package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.BlockItemStateProperties;

public class MineraculousItemProperties {
    public static void init() {
        ItemPropertyFunction blocking = (stack, level, entity, seed) -> stack.has(MineraculousDataComponents.BLOCKING) ? 1 : 0;
        ItemProperties.register(MineraculousItems.LADYBUG_YOYO.get(), LadybugYoyoItem.BLOCKING_PROPERTY_ID, blocking);
        ItemProperties.register(MineraculousItems.LADYBUG_YOYO.get(), LadybugYoyoItem.EXTENDED_PROPERTY_ID, (stack, level, entity, seed) -> {
            if (stack.has(MineraculousDataComponents.ACTIVE)) {
                if (entity instanceof Player player && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {
                    ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
                    ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(player.level());
                    if (thrownYoyo instanceof ThrownLadybugYoyo yoyo) {
                        return yoyo.inGround() || yoyo.isBound() ? 3 : 2;
                    }
                }
                return 1;
            }
            return 0;
        });
        ItemProperties.register(MineraculousItems.CAT_STAFF.get(), LadybugYoyoItem.BLOCKING_PROPERTY_ID, blocking);
        ItemProperties.register(MineraculousItems.CAT_STAFF.get(), LadybugYoyoItem.EXTENDED_PROPERTY_ID, (stack, level, entity, seed) -> stack.has(MineraculousDataComponents.ACTIVE) ? 1 : 0);
        ItemProperties.register(MineraculousItems.BUTTERFLY_CANE.get(), LadybugYoyoItem.BLOCKING_PROPERTY_ID, blocking);
        ItemProperties.register(MineraculousItems.BUTTERFLY_CANE.get(), ButterflyCaneItem.BLADE_PROPERTY_ID, (stack, level, entity, seed) -> stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY) == ButterflyCaneItem.Ability.BLADE ? 1 : 0);
        MineraculousBlocks.CHEESE_BLOCKS.values().forEach(block -> ItemProperties.register(block.asItem(), CheeseBlock.BITES_PROPERTY_ID, (stack, level, entity, seed) -> {
            BlockItemStateProperties blockItemStateProperties = stack.get(DataComponents.BLOCK_STATE);
            if (blockItemStateProperties == null)
                return 0;
            Integer bites = blockItemStateProperties.get(CheeseBlock.BITES);
            return bites == null ? 0 : bites;
        }));
    }
}
