package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Holds a set of humanoid armor,
 * corresponding to an {@link EquipmentSlot} value.
 *
 * @param head  The item in the {@link EquipmentSlot#HEAD} slot
 * @param chest The item in the {@link EquipmentSlot#CHEST} slot
 * @param legs  The item in the {@link EquipmentSlot#LEGS} slot
 * @param feet  The item in the {@link EquipmentSlot#FEET} slot
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
     * @param entity The entity to equip the set on and clear the data for
     */
    public void equipAndClear(LivingEntity entity) {
        entity.setItemSlot(EquipmentSlot.HEAD, head);
        entity.setItemSlot(EquipmentSlot.CHEST, chest);
        entity.setItemSlot(EquipmentSlot.LEGS, legs);
        entity.setItemSlot(EquipmentSlot.FEET, feet);
        entity.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.empty());
    }

    public ItemStack removeFrom(EquipmentSlot slot, Entity entity) {
        ItemStack stack = switch (slot) {
            case HEAD -> head.copyAndClear();
            case CHEST -> chest.copyAndClear();
            case LEGS -> legs.copyAndClear();
            case FEET -> feet.copyAndClear();
            default -> throw new IllegalArgumentException("Invalid slot " + slot);
        };
        entity.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(this));
        return stack;
    }

    public static void restoreOrClear(LivingEntity entity) {
        entity.getData(MineraculousAttachmentTypes.STORED_ARMOR).ifPresentOrElse(data -> data.equipAndClear(entity), () -> {
            entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            entity.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            entity.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
            entity.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        });
    }
}
