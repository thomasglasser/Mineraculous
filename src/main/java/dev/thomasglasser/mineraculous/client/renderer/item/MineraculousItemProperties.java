package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.BlockItemStateProperties;

public class MineraculousItemProperties {
    public static void init() {
        ItemProperties.register(MineraculousItems.LADYBUG_YOYO.get(), LadybugYoyoItem.EXTENDED_PROPERTY_ID, (stack, level, entity, seed) -> {
            if (stack.has(MineraculousDataComponents.ACTIVE)) {
                if (entity instanceof Player player && (player.getMainHandItem() == stack || player.getOffhandItem() == stack) && player.getData(MineraculousAttachmentTypes.LADYBUG_YOYO).isPresent()) {
                    Entity thrown = player.level().getEntity(player.getData(MineraculousAttachmentTypes.LADYBUG_YOYO).get());
                    if (thrown instanceof ThrownLadybugYoyo yoyo)
                        return yoyo.inGround() || yoyo.isBound() ? 3 : 2;
                }
                return 1;
            }
            return 0;
        });
        ItemProperties.register(MineraculousItems.CAT_STAFF.get(), CatStaffItem.EXTENDED_PROPERTY_ID, (stack, level, entity, seed) -> {
            if (stack.has(MineraculousDataComponents.ACTIVE)) {
                if (entity != null && stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY) == CatStaffItem.Ability.PERCH && entity.getData(MineraculousAttachmentTypes.PERCH_CAT_STAFF).tick() > 10) {
                    return 2;
                }
                return 1;
            }
            return 0;
        });
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
