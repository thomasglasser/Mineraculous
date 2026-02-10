package dev.thomasglasser.mineraculous.impl.world.item.armor;

import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmorMaterials;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Unbreakable;

public class MiraculousArmorItem extends AbstractMiraculousArmorItem {
    public MiraculousArmorItem(Type type, Properties pProperties) {
        super(MineraculousArmorMaterials.MIRACULOUS, type, pProperties
                .component(DataComponents.UNBREAKABLE, new Unbreakable(false)));
    }
}
