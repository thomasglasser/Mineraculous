package dev.thomasglasser.mineraculous.api.world.entity;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.network.ClientboundRefreshDisplayNamePayload;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

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
                // TODO: Fix name
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

    public static void refreshAndSyncDisplayName(ServerPlayer player) {
        player.refreshDisplayName();
        TommyLibServices.NETWORK.sendToTrackingClientsAndSelf(new ClientboundRefreshDisplayNamePayload(player.getUUID()), player);
    }

    /**
     * Performs item breaking on an {@link ItemEntity} and spawns a new item for the remaining stack.
     *
     * @param itemEntity  The item entity to break
     * @param serverLevel The level to perform the breaking in
     */
    public static void tryBreakItemEntity(ItemEntity itemEntity, ServerLevel serverLevel) {
        Vec3 position = itemEntity.position();
        MineraculousItemUtils.BreakResult result = MineraculousItemUtils.tryBreakItem(itemEntity.getItem(), serverLevel, position, null);
        ItemStack stack = result.original();
        ItemStack rest = result.remainder();
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

    /**
     * Collects every item an entity has in a {@link Set}, including curios.
     *
     * @param entity The entity to check for items
     * @return The {@link Set} of any items the entity has
     */
    public static Set<ItemStack> getInventoryAndCurios(Entity entity) {
        Set<ItemStack> inventory = new ReferenceOpenHashSet<>();
        inventory.addAll(EntityUtils.getInventory(entity));
        if (entity instanceof LivingEntity livingEntity) {
            inventory.addAll(CuriosUtils.getAllItems(livingEntity).values());
        }
        return inventory;
    }

    /**
     * Summons a {@link Kwami} with the provided charge, miraculous ID, and miraculous for the provided owner.
     *
     * @param owner         The owner of the kwami
     * @param charged       Whether the kwami is charged
     * @param miraculousId  The related miraculous item {@link UUID}
     * @param miraculous    The miraculous of the kwami
     * @param playAnimation Whether to play the kwami summon animation
     * @return The summoned kwami
     */
    public static Kwami summonKwami(Entity owner, boolean charged, UUID miraculousId, Holder<Miraculous> miraculous, boolean playAnimation, @Nullable UUID uuidOverride) {
        Level level = owner.level();
        Kwami kwami = MineraculousEntityTypes.KWAMI.get().create(level);
        if (kwami != null) {
            kwami.setCharged(charged);
            kwami.setMiraculous(miraculous);
            kwami.setMiraculousId(miraculousId);
            if (owner instanceof Player player) {
                kwami.tame(player);
            } else {
                kwami.setOwnerUUID(owner.getUUID());
                kwami.setTame(true, true);
            }
            kwami.setYRot(owner.getYRot() + 180);
            kwami.setYHeadRot(owner.getYRot() + 180);
            if (playAnimation) {
                kwami.moveTo(owner.position());
                kwami.setSummonTicks(SharedConstants.TICKS_PER_SECOND * MineraculousServerConfig.get().kwamiSummonTime.getAsInt());
                kwami.playSound(MineraculousSoundEvents.KWAMI_SUMMON.get());
            } else {
                Vec3 ownerPos = owner.position();
                Vec3 lookDirection = owner.getLookAngle();
                Vec3 kwamiPos = ownerPos.add(lookDirection.scale(1.5));
                kwami.moveTo(kwamiPos.x, owner.getEyeY(), kwamiPos.z);
            }
            if (uuidOverride != null)
                kwami.setUUID(uuidOverride);
            level.addFreshEntity(kwami);
            return kwami;
        }
        return null;
    }

    /**
     * Renounces a kwami from a provided {@link ItemStack} and discards it if in the same level.
     *
     * @param kwamiId The {@link UUID} of the kwami to renounce
     * @param stack   The {@link ItemStack} to renounce
     * @param level   The level to renounce the kwami in
     */
    public static void renounceKwami(@Nullable UUID kwamiId, ItemStack stack, ServerLevel level) {
        stack.set(MineraculousDataComponents.POWERED, Unit.INSTANCE);
        stack.remove(MineraculousDataComponents.REMAINING_TICKS);
        stack.remove(MineraculousDataComponents.KWAMI_ID);
        if (kwamiId != null && level.getEntity(kwamiId) instanceof Kwami kwami) {
            kwami.discard();
        }
    }

    /**
     * Adds an entity caused by a lucky charm to the provided {@link AbilityReversionEntityData} for tracking.
     *
     * @param player The {@link Player} spawning the entity
     * @param entity The {@link Entity} to track
     * @param level  The {@link ServerLevel} of the entity
     * @param stack  The {@link ItemStack} used to summon the entity
     */
    public static void checkAndTrackLuckyCharmEntity(@Nullable Player player, @Nullable Entity entity, ServerLevel level, ItemStack stack) {
        if (player != null && entity != null && stack.has(MineraculousDataComponents.LUCKY_CHARM)) {
            AbilityReversionEntityData.get(level).putRemovable(player.getUUID(), entity);
        }
    }

    /**
     * Finds an entity in whatever level is currently loaded with the provided {@link UUID}.
     *
     * @param level The level to pull the level list from
     * @param uuid  The {@link UUID} of the entity to find
     * @return The found entity, or null if not found
     */
    public static @Nullable Entity findEntity(ServerLevel level, UUID uuid) {
        for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity != null)
                return entity;
        }
        return null;
    }
}
