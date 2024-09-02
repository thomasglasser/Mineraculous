package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public record ApplyInfiniteEffectsOrDestroyAbility(HolderSet<MobEffect> effects, Optional<Item> dropItem, Optional<ResourceKey<DamageType>> damageType) implements Ability {

    public static final MapCodec<ApplyInfiniteEffectsOrDestroyAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("effects").forGetter(ApplyInfiniteEffectsOrDestroyAbility::effects),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("drop_item").forGetter(ApplyInfiniteEffectsOrDestroyAbility::dropItem),
            ResourceKey.codec(Registries.DAMAGE_TYPE).optionalFieldOf("damage_type").forGetter(ApplyInfiniteEffectsOrDestroyAbility::damageType)).apply(instance, ApplyInfiniteEffectsOrDestroyAbility::new));
    @Override
    public boolean perform(ResourceKey<Miraculous> type, MiraculousData data, Level level, BlockPos pos, LivingEntity performer, Context context) {
        if (context == Context.INTERACT_ENTITY) {
            Entity target = context.entity();
            if (target instanceof LivingEntity livingEntity) {
                for (Holder<MobEffect> mobEffect : effects) {
                    MobEffectInstance effect = new MobEffectInstance(mobEffect, -1, data.powerLevel());
                    livingEntity.addEffect(effect);
                    if (performer instanceof ServerPlayer serverPlayer)
                        serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(livingEntity.getId(), effect, true));
                }
                if (performer instanceof Player player)
                    livingEntity.setLastHurtByPlayer(player);
            } else if (target instanceof VehicleEntity vehicle && dropItem.isPresent()) {
                vehicle.destroy(dropItem.get());
            } else {
                target.hurt(damageType.map(damageTypeResourceKey -> performer.damageSources().source(damageTypeResourceKey, performer)).orElse(performer.damageSources().indirectMagic(performer, performer)), 1024);
            }
            return true;
        }
        return false;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.APPLY_INFINITE_EFFECTS_OR_DESTROY.get();
    }
}
