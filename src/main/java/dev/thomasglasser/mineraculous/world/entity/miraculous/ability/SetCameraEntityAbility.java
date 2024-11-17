package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.network.ClientboundSetCameraEntityPayload;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

public record SetCameraEntityAbility(EntityPredicate entity, Optional<ResourceLocation> shader, Optional<String> toggleTag, boolean mustBeTamed, boolean overrideActive) implements Ability {

    public static final MapCodec<SetCameraEntityAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntityPredicate.CODEC.fieldOf("entity").forGetter(SetCameraEntityAbility::entity),
            ResourceLocation.CODEC.optionalFieldOf("shader").forGetter(SetCameraEntityAbility::shader),
            Codec.STRING.optionalFieldOf("toggle_tag").forGetter(SetCameraEntityAbility::toggleTag),
            Codec.BOOL.optionalFieldOf("must_be_tamed", true).forGetter(SetCameraEntityAbility::mustBeTamed),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(SetCameraEntityAbility::overrideActive)).apply(instance, SetCameraEntityAbility::new));
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
                TommyLibServices.NETWORK.sendToClient(new ClientboundSetCameraEntityPayload(target.getId(), shader, toggleTag, true, true, data.power().left()), serverPlayer);
                return true;
            }
        }
        return false;
    }

    @Override
    public void detransform(AbilityData data, Level level, BlockPos pos, LivingEntity performer) {
        if (performer instanceof ServerPlayer serverPlayer) {
            Entity target = null;
            for (Entity e : serverPlayer.serverLevel().getEntities().getAll()) {
                if ((!mustBeTamed || (e instanceof TamableAnimal tamable && tamable.isOwnedBy(performer))) && entity.matches(serverPlayer.serverLevel(), e.position(), e)) {
                    target = e;
                    break;
                }
            }
            TommyLibServices.NETWORK.sendToClient(new ClientboundSetCameraEntityPayload(target != null ? target.getId() : -1, Optional.empty(), toggleTag, true, true, data.power().left()), serverPlayer);
        }
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.SET_CAMERA_ENTITY.get();
    }
}
