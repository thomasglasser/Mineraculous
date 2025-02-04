package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousRecoveryDataHolder;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

public record ApplyInfiniteEffectsOrDestroyAbility(HolderSet<MobEffect> effects, Optional<Item> dropItem, Optional<ResourceKey<DamageType>> damageType, Optional<String> blameTag, Optional<Holder<SoundEvent>> startSound, boolean overrideActive) implements Ability {

    public static final MapCodec<ApplyInfiniteEffectsOrDestroyAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("effects").forGetter(ApplyInfiniteEffectsOrDestroyAbility::effects),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("drop_item").forGetter(ApplyInfiniteEffectsOrDestroyAbility::dropItem),
            ResourceKey.codec(Registries.DAMAGE_TYPE).optionalFieldOf("damage_type").forGetter(ApplyInfiniteEffectsOrDestroyAbility::damageType),
            Codec.STRING.optionalFieldOf("blame_tag").forGetter(ApplyInfiniteEffectsOrDestroyAbility::blameTag),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(ApplyInfiniteEffectsOrDestroyAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(ApplyInfiniteEffectsOrDestroyAbility::overrideActive)).apply(instance, ApplyInfiniteEffectsOrDestroyAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Context context) {
        if (context == Context.INTERACT_ENTITY) {
            Entity target = context.entity();
            ((MiraculousRecoveryDataHolder) level.getServer().overworld()).mineraculous$getMiraculousRecoveryEntityData().putRecoverable(entity.getUUID(), target);
            if (target instanceof LivingEntity livingEntity) {
                for (Holder<MobEffect> mobEffect : effects) {
                    MobEffectInstance effect = new MobEffectInstance(mobEffect, -1, (data.powerLevel() / 10));
                    livingEntity.addEffect(effect);
                    if (entity instanceof ServerPlayer serverPlayer)
                        serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(livingEntity.getId(), effect, true));
                }
                if (entity instanceof Player player) {
                    livingEntity.setLastHurtByPlayer(player);
                    blameTag.ifPresent(s -> {
                        CompoundTag tag = TommyLibServices.ENTITY.getPersistentData(livingEntity);
                        tag.putUUID(s, player.getUUID());
                        TommyLibServices.ENTITY.setPersistentData(livingEntity, tag, true);
                    });
                }
            } else if (target instanceof VehicleEntity vehicle && dropItem.isPresent()) {
                vehicle.kill();
                if (level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    ItemStack itemstack = new ItemStack(dropItem.get());
                    UUID id = UUID.randomUUID();
                    itemstack.set(MineraculousDataComponents.RECOVERABLE_ITEM_ID, id);
                    ((MiraculousRecoveryDataHolder) level.getServer().overworld()).mineraculous$getMiraculousRecoveryItemData().putRemovable(entity.getUUID(), id);
                    vehicle.spawnAtLocation(itemstack);
                }
            } else {
                target.hurt(damageType.map(damageTypeResourceKey -> entity.damageSources().source(damageTypeResourceKey, entity)).orElse(entity.damageSources().indirectMagic(entity, entity)), 1024);
            }
            playStartSound(level, pos);
            return true;
        }
        return false;
    }

    @Override
    public void restore(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {
        ((MiraculousRecoveryDataHolder) level.getServer().overworld()).mineraculous$getMiraculousRecoveryEntityData().recover(entity.getUUID(), level, target -> {
            if (target instanceof LivingEntity livingEntity) {
                for (Holder<MobEffect> mobEffect : effects) {
                    livingEntity.removeEffect(mobEffect);
                }
            }
            return target;
        });
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.APPLY_INFINITE_EFFECTS_OR_DESTROY.get();
    }
}
