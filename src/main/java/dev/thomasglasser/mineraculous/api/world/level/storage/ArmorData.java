package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import java.util.Optional;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Holds a set of humanoid armor.
 *
 * @param head  The item in the head slot
 * @param chest The item in the chest slot
 * @param legs  The item in the legs slot
 * @param feet  The item in the feet slot
 */
public record ArmorData(ItemStack head, ItemStack chest, ItemStack legs, ItemStack feet) {

    public static final Codec<ArmorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("head").forGetter(ArmorData::head),
            ItemStack.OPTIONAL_CODEC.fieldOf("chest").forGetter(ArmorData::chest),
            ItemStack.OPTIONAL_CODEC.fieldOf("legs").forGetter(ArmorData::legs),
            ItemStack.OPTIONAL_CODEC.fieldOf("feet").forGetter(ArmorData::feet))
            .apply(instance, ArmorData::new));
    /**
     * Equips the stored armor on the provided {@link LivingEntity} and clears {@link MineraculousAttachmentTypes#STORED_ARMOR}.
     *
     * @param livingEntity The entity to equip the set on and clear the data for
     */
    public void equipAndClear(LivingEntity livingEntity) {
        livingEntity.setItemSlot(EquipmentSlot.HEAD, head);
        livingEntity.setItemSlot(EquipmentSlot.CHEST, chest);
        livingEntity.setItemSlot(EquipmentSlot.LEGS, legs);
        livingEntity.setItemSlot(EquipmentSlot.FEET, feet);
        livingEntity.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.empty());
    }
}
