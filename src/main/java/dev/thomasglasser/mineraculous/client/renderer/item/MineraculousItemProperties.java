package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.BlockItemStateProperties;

public class MineraculousItemProperties {
    // Generic
    public static final ResourceLocation BLOCKING = Mineraculous.modLoc("blocking");
    public static final ResourceLocation OPEN = Mineraculous.modLoc("open");

    // Specific
    public static final ResourceLocation ABILITY = Mineraculous.modLoc("ability");
    public static final ResourceLocation BITES = Mineraculous.modLoc("bites");
    public static final ResourceLocation EXTENDED = Mineraculous.modLoc("extended");

    public static void init() {
        // Generic
        ItemProperties.registerGeneric(BLOCKING, (stack, level, entity, seed) -> stack.has(MineraculousDataComponents.BLOCKING) ? 1 : 0);
        ItemProperties.registerGeneric(OPEN, (stack, level, entity, seed) -> {
            Boolean open = stack.get(MineraculousDataComponents.OPEN);
            if (open == null)
                return 0;
            return open ? 1 : 0;
        });

        // Miraculous Tools
        ItemProperties.register(MineraculousItems.LADYBUG_YOYO.get(), ABILITY, getEnumAbilityPropertyFunction(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get()));
        ItemProperties.register(MineraculousItems.LADYBUG_YOYO.get(), EXTENDED, (stack, level, entity, seed) -> {
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
        ItemProperties.register(MineraculousItems.CAT_STAFF.get(), ABILITY, getEnumAbilityPropertyFunction(MineraculousDataComponents.CAT_STAFF_ABILITY.get()));
        ItemProperties.register(MineraculousItems.CAT_STAFF.get(), EXTENDED, (stack, level, entity, seed) -> stack.has(MineraculousDataComponents.ACTIVE) ? 1 : 0);
        ItemProperties.register(MineraculousItems.BUTTERFLY_CANE.get(), ABILITY, getEnumAbilityPropertyFunction(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get()));

        // Block Items
        MineraculousBlocks.CHEESE_BLOCKS.values().forEach(block -> ItemProperties.register(block.asItem(), BITES, (stack, level, entity, seed) -> {
            BlockItemStateProperties blockItemStateProperties = stack.get(DataComponents.BLOCK_STATE);
            if (blockItemStateProperties == null)
                return 0;
            Integer bites = blockItemStateProperties.get(CheeseBlock.BITES);
            return bites == null ? 0 : bites;
        }));
    }

    public static <T extends Enum<T>> ItemPropertyFunction getEnumAbilityPropertyFunction(DataComponentType<T> component) {
        return (stack, level, entity, seed) -> {
            T value = stack.get(component);
            if (value == null)
                return 0;
            return value.ordinal() + 1;
        };
    }

    public static int getPropertyForAbility(Enum<?> value) {
        return value.ordinal() + 1;
    }
}
