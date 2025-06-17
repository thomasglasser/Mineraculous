package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import java.util.Optional;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record ArmorData(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {

    public static final Codec<ArmorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("helmet").forGetter(ArmorData::helmet),
            ItemStack.OPTIONAL_CODEC.fieldOf("chestplate").forGetter(ArmorData::chestplate),
            ItemStack.OPTIONAL_CODEC.fieldOf("leggings").forGetter(ArmorData::leggings),
            ItemStack.OPTIONAL_CODEC.fieldOf("boots").forGetter(ArmorData::boots))
            .apply(instance, ArmorData::new));
    public void equipAndClear(LivingEntity livingEntity) {
        livingEntity.setItemSlot(EquipmentSlot.HEAD, helmet);
        livingEntity.setItemSlot(EquipmentSlot.CHEST, chestplate);
        livingEntity.setItemSlot(EquipmentSlot.LEGS, leggings);
        livingEntity.setItemSlot(EquipmentSlot.FEET, boots);
        livingEntity.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.empty());
    }
}
