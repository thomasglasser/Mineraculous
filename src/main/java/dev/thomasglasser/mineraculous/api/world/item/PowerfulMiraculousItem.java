package dev.thomasglasser.mineraculous.api.world.item;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

public class PowerfulMiraculousItem extends MiraculousItem {
    public PowerfulMiraculousItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level instanceof ServerLevel serverLevel) {
            UUID owner = stack.get(MineraculousDataComponents.OWNER);
            if (owner == null || !owner.equals(entity.getUUID())) {
                stack.set(MineraculousDataComponents.OWNER, entity.getUUID());
                UUID kwamiId = stack.get(MineraculousDataComponents.KWAMI_ID);
                if (kwamiId != null) {
                    if (serverLevel.getEntity(kwamiId) instanceof Kwami kwami) {
                        if (entity instanceof Player player) {
                            kwami.tame(player);
                        } else {
                            kwami.setTame(true, true);
                            kwami.setOwnerUUID(entity.getUUID());
                        }
                    } else if (!stack.has(MineraculousDataComponents.POWERED)) {
                        stack.remove(MineraculousDataComponents.KWAMI_ID);
                    }
                }
            }
            if (!stack.has(MineraculousDataComponents.POWERED) && !stack.has(MineraculousDataComponents.KWAMI_ID)) {
                stack.set(MineraculousDataComponents.POWERED, Unit.INSTANCE);
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (!entity.level().isClientSide() && miraculous != null && !(stack.is(prevStack.getItem()) && miraculous == prevStack.get(MineraculousDataComponents.MIRACULOUS))) {
            MiraculousData data = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous);
            if (!data.transformed()) {
                UUID miraculousId = stack.get(MineraculousDataComponents.MIRACULOUS_ID);
                if (miraculousId == null) {
                    miraculousId = UUID.randomUUID();
                    stack.set(MineraculousDataComponents.MIRACULOUS_ID, miraculousId);
                }
                if (stack.has(MineraculousDataComponents.POWERED)) {
                    stack.remove(MineraculousDataComponents.POWERED);
                    Kwami kwami = MineraculousEntityUtils.summonKwami(entity, stack.getOrDefault(MineraculousDataComponents.CHARGED, true), miraculousId, miraculous, true, null);
                    if (kwami != null) {
                        stack.set(MineraculousDataComponents.KWAMI_ID, kwami.getUUID());
                    } else {
                        MineraculousConstants.LOGGER.error("Kwami could not be created for entity {}", entity.getName().plainCopy().getString());
                    }
                }
                data.equip(new CuriosData(slotContext)).save(miraculous, entity, true);
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (miraculous != null && entity.level() instanceof ServerLevel level && !(stack.is(newStack.getItem()) && miraculous == newStack.get(MineraculousDataComponents.MIRACULOUS))) {
            MiraculousData data = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous);
            if (data.transformed()) {
                data.detransform(entity, level, miraculous, stack, true);
            } else {
                data.unequip().save(miraculous, entity, true);
            }
        }
    }
}
