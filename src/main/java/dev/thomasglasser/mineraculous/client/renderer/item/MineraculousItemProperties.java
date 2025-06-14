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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.BlockItemStateProperties;

public class MineraculousItemProperties {
    // Vanilla
    public static final ResourceLocation BLOCKING = ResourceLocation.withDefaultNamespace("blocking");

    // Generic
    public static final ResourceLocation ACTIVE = Mineraculous.modLoc("active");

    // Specific
    public static final ResourceLocation ABILITY = Mineraculous.modLoc("ability");
    public static final ResourceLocation BITES = Mineraculous.modLoc("bites");
    public static final ResourceLocation THROWN = Mineraculous.modLoc("thrown");

    public static void init() {
        // Generic
        ItemProperties.registerGeneric(BLOCKING, (stack, level, entity, seed) -> stack.has(MineraculousDataComponents.BLOCKING) ? 1 : 0);
        ItemProperties.registerGeneric(ACTIVE, (stack, level, entity, seed) -> stack.getOrDefault(MineraculousDataComponents.ACTIVE, false) ? 1 : 0);

        // Miraculous Tools
        ItemProperties.register(MineraculousItems.LADYBUG_YOYO.get(), ABILITY, getEnumAbilityPropertyFunction(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get()));
        ItemProperties.register(MineraculousItems.LADYBUG_YOYO.get(), THROWN, (stack, level, entity, seed) -> {
            if (entity instanceof Player player) {
                ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
                ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(player.level());
                if (thrownYoyo instanceof ThrownLadybugYoyo yoyo) {
                    if (yoyo.getInitialHand() == InteractionHand.MAIN_HAND ? player.getMainHandItem() == stack : player.getOffhandItem() == stack) {
                        return yoyo.inGround() /*|| yoyo.isBound()*/ ? 2 : 1;
                    }
                }
            }
            return 0;
        });
        ItemProperties.register(MineraculousItems.CAT_STAFF.get(), ABILITY, getEnumAbilityPropertyFunction(MineraculousDataComponents.CAT_STAFF_ABILITY.get()));
        ItemProperties.register(MineraculousItems.BUTTERFLY_CANE.get(), ABILITY, getEnumAbilityPropertyFunction(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get()));

        // Block Items
        ItemPropertyFunction bitesFunction = (stack, level, entity, seed) -> {
            BlockItemStateProperties blockItemStateProperties = stack.get(DataComponents.BLOCK_STATE);
            if (blockItemStateProperties == null)
                return 0;
            Integer bites = blockItemStateProperties.get(CheeseBlock.BITES);
            return bites == null ? 0 : bites;
        };
        MineraculousBlocks.CHEESE.values().forEach(block -> ItemProperties.register(block.asItem(), BITES, bitesFunction));
        MineraculousBlocks.CAMEMBERT.values().forEach(block -> ItemProperties.register(block.asItem(), BITES, bitesFunction));
        MineraculousBlocks.WAXED_CHEESE.values().forEach(block -> ItemProperties.register(block.asItem(), BITES, bitesFunction));
        MineraculousBlocks.WAXED_CAMEMBERT.values().forEach(block -> ItemProperties.register(block.asItem(), BITES, bitesFunction));
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
