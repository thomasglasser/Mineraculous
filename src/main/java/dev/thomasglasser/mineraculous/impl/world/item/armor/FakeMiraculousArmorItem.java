package dev.thomasglasser.mineraculous.impl.world.item.armor;

import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmorMaterials;

public class FakeMiraculousArmorItem extends AbstractMiraculousArmorItem {
    public FakeMiraculousArmorItem(Type type, Properties pProperties) {
        super(MineraculousArmorMaterials.COSTUME, type, pProperties);
    }
}
