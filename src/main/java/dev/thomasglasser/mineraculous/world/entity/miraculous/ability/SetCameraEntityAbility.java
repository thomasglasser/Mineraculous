package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.network.ClientboundSetCameraEntityPayload;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

public record SetCameraEntityAbility(EntityPredicate entity, boolean mustBeTamed) implements Ability {
    public static final MapCodec<SetCameraEntityAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntityPredicate.CODEC.fieldOf("entity").forGetter(SetCameraEntityAbility::entity),
            Codec.BOOL.optionalFieldOf("must_be_tamed", true).forGetter(SetCameraEntityAbility::mustBeTamed)).apply(instance, SetCameraEntityAbility::new));

    @Override
    public boolean perform(AbilityData data, Level level, BlockPos pos, LivingEntity performer, Context context) {
        if (context == Context.PASSIVE && performer instanceof ServerPlayer serverPlayer) {
            Entity target = null;
            for (Entity e : serverPlayer.serverLevel().getEntities().getAll()) {
                if ((!mustBeTamed || (e instanceof TamableAnimal tamable && tamable.isOwnedBy(performer))) && entity.matches(serverPlayer.serverLevel(), e.position(), e)) {
                    target = e;
                    break;
                }
            }
            if (target != null) {
                TommyLibServices.NETWORK.sendToClient(new ClientboundSetCameraEntityPayload(target.getId()), serverPlayer);
                return true;
            }
        }
        return false;
    }

    @Override
    public void detransform(AbilityData data, Level level, BlockPos pos, LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            TommyLibServices.NETWORK.sendToClient(new ClientboundSetCameraEntityPayload(-1), serverPlayer);
        }
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.SET_CAMERA_ENTITY.get();
    }
}
