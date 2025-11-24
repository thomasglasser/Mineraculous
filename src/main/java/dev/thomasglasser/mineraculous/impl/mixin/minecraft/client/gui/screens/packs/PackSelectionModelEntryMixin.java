package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.gui.screens.packs;

import dev.thomasglasser.mineraculous.impl.server.packs.repository.MineraculousPackCompatabilityHolder;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PackSelectionModel.Entry.class)
public interface PackSelectionModelEntryMixin extends MineraculousPackCompatabilityHolder {}
