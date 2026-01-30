package dev.thomasglasser.mineraculous.impl.world.entity.vehicle;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.function.Supplier;

public class MineraculousBoatType {
    public static final EnumProxy<Boat.Type> ALMOND = new EnumProxy<>(Boat.Type.class,
            (Supplier<Block>) () -> MineraculousBlocks.ALMOND_WOOD_SET.planks().get(),
            MineraculousConstants.modLoc("almond").toString(),
            (Supplier<Item>) () -> MineraculousBlocks.ALMOND_WOOD_SET.boatItem().get(),
            (Supplier<Item>) () -> MineraculousBlocks.ALMOND_WOOD_SET.chestBoatItem().get(),
            (Supplier<Item>) () -> Items.STICK,
            false);
}
