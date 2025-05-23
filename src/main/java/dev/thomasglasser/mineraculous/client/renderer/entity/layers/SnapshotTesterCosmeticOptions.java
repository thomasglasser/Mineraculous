package dev.thomasglasser.mineraculous.client.renderer.entity.layers;

import net.minecraft.world.entity.EquipmentSlot;

public enum SnapshotTesterCosmeticOptions {
    DERBY_HAT(EquipmentSlot.HEAD);

    final EquipmentSlot slot;

    SnapshotTesterCosmeticOptions(EquipmentSlot slot) {
        this.slot = slot;
    }

    public EquipmentSlot slot() {
        return this.slot;
    }
}
