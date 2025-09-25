package dev.thomasglasser.mineraculous.impl.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.InclusiveRange;

public enum MineraculousPackCompatibility {
    TOO_OLD("old"),
    TOO_NEW("new"),
    COMPATIBLE("compatible");

    private final Component description;
    private final Component confirmation;

    MineraculousPackCompatibility(String type) {
        this.description = Component.translatable("mineraculous_pack.incompatible." + type).withStyle(ChatFormatting.GRAY);
        this.confirmation = Component.translatable("mineraculous_pack.incompatible.confirm." + type);
    }

    public boolean isCompatible() {
        return this == COMPATIBLE;
    }

    public static MineraculousPackCompatibility forVersion(InclusiveRange<Integer> range, int version) {
        if (range.maxInclusive() < version) {
            return TOO_OLD;
        } else {
            return version < range.minInclusive() ? TOO_NEW : COMPATIBLE;
        }
    }

    public Component getDescription() {
        return this.description;
    }

    public Component getConfirmation() {
        return this.confirmation;
    }
}
