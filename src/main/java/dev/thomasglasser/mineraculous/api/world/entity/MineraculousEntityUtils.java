package dev.thomasglasser.mineraculous.api.world.entity;

import com.mojang.datafixers.util.Pair;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.level.storage.MiraculousesData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class MineraculousEntityUtils {
    /**
     * Applies the provided {@link MobEffect} to the provided {@link LivingEntity} at the provided amplifier invisibly for an infinite duration.
     * 
     * @param entity    The entity to apply the effect to
     * @param effect    The effect to apply to the entity
     * @param amplifier The amplifier to use for the effect
     */
    public static void applyInfiniteHiddenEffect(LivingEntity entity, Holder<MobEffect> effect, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect, -1, amplifier, false, false));
    }

    /**
     * Formats the provided {@link Entity}'s display name,
     * taking {@link MiraculousData} and {@link KamikotizationData} into account.
     *
     * @param entity   The entity to check for formatting
     * @param original The original name of the entity
     * @return The formatted name of the entity, or the original if no formatting was needed
     */
    public static Component formatDisplayName(Entity entity, Component original) {
        if (original != null) {
            Style style = original.getStyle();
            MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            List<Holder<Miraculous>> transformed = miraculousesData.getTransformed();
            if (!transformed.isEmpty()) {
                Holder<Miraculous> miraculous = transformed.getFirst();
                MiraculousData data = miraculousesData.get(miraculous);
                Style newStyle = style.withColor(miraculous.value().color());
                if (/*!data.name().isEmpty()*/false)
                    return Component.literal(/*data.name()*/"").setStyle(newStyle);
                return Entity.removeAction(original.copy().setStyle(newStyle.withObfuscated(true).withHoverEvent(null)));
            } else if (entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
                KamikotizationData data = entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).get();
                Style newStyle = style.withColor(data.kamikoData().nameColor());
                return Entity.removeAction(Component.literal(data.name()).setStyle(newStyle.withHoverEvent(null)));
            }
        }
        return original;
    }

    /**
     * Performs item breaking on an {@link ItemEntity} and spawns a new item for the remaining stack.
     *
     * @param itemEntity  The item entity to break
     * @param serverLevel The level to perform the breaking in
     */
    public static void tryBreakItemEntity(ItemEntity itemEntity, ServerLevel serverLevel) {
        Vec3 position = itemEntity.position();
        Pair<ItemStack, ItemStack> result = MineraculousItemUtils.tryBreakItem(itemEntity.getItem(), serverLevel, position, null);
        ItemStack stack = result.getFirst();
        ItemStack rest = result.getSecond();
        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }
        if (!rest.isEmpty()) {
            ItemEntity restEntity = new ItemEntity(serverLevel, position.x(), position.y(), position.z(), rest);
            serverLevel.addFreshEntity(restEntity);
        }
    }
}
