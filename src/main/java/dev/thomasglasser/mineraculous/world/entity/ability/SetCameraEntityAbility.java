package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.network.ClientboundSetCameraEntityPayload;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;

public class SetCameraEntityAbility implements Ability {
    public static final MapCodec<SetCameraEntityAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntityPredicate.CODEC.fieldOf("entity").forGetter(SetCameraEntityAbility::entity),
            ResourceLocation.CODEC.optionalFieldOf("shader").forGetter(SetCameraEntityAbility::shader),
            Codec.STRING.optionalFieldOf("toggle_tag").forGetter(SetCameraEntityAbility::toggleTag),
            Codec.BOOL.optionalFieldOf("must_be_tamed", true).forGetter(SetCameraEntityAbility::mustBeTamed),
            Codec.BOOL.optionalFieldOf("override_owner", false).forGetter(SetCameraEntityAbility::overrideOwner),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(SetCameraEntityAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(SetCameraEntityAbility::overrideActive)).apply(instance, SetCameraEntityAbility::new));

    private final EntityPredicate entity;
    private final Optional<ResourceLocation> shader;
    private final Optional<String> toggleTag;
    private final boolean mustBeTamed;
    private final boolean overrideOwner;
    private final Optional<Holder<SoundEvent>> startSound;
    private final boolean overrideActive;

    private Entity target;

    public SetCameraEntityAbility(EntityPredicate entity, Optional<ResourceLocation> shader, Optional<String> toggleTag, boolean mustBeTamed, boolean overrideOwner, Optional<Holder<SoundEvent>> startSound, boolean overrideActive) {
        this.entity = entity;
        this.shader = shader;
        this.toggleTag = toggleTag;
        this.mustBeTamed = mustBeTamed;
        this.overrideOwner = overrideOwner;
        this.startSound = startSound;
        this.overrideActive = overrideActive;
    }

    @Override
    public boolean perform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Context context) {
        if (context == Context.PASSIVE) {
            TommyLibServices.NETWORK.sendToClient(new ClientboundSetCameraEntityPayload(target.getId(), shader, toggleTag, true, overrideOwner), (ServerPlayer) entity);
            target = null;
            playStartSound(level, pos);
            return true;
        }
        return false;
    }

    // TODO: Fix
    @Override
    public void detransform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity performer) {
        if (performer instanceof ServerPlayer serverPlayer) {
//            CompoundTag tag = TommyLibServices.ENTITY.getPersistentData(performer);
//            tag.putBoolean(MineraculousEntityEvents.TAG_CAMERA_CONTROL_INTERRUPTED, true);
//            TommyLibServices.ENTITY.setPersistentData(performer, tag, true);
            Entity target = null;
            for (Entity e : serverPlayer.serverLevel().getEntities().getAll()) {
                if ((!mustBeTamed || (e instanceof TamableAnimal tamable && tamable.isOwnedBy(performer))) && entity.matches(serverPlayer.serverLevel(), e.position(), e)) {
                    target = e;
                    break;
                }
            }
            TommyLibServices.NETWORK.sendToClient(new ClientboundSetCameraEntityPayload(target != null ? target.getId() : -1, Optional.empty(), toggleTag, true, overrideOwner), serverPlayer);
        }
        toggleTag.ifPresent(tag -> {
//            CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(performer);
//            entityData.putBoolean(tag, false);
//            TommyLibServices.ENTITY.setPersistentData(performer, entityData, true);
        });
    }

    @Override
    public boolean canActivate(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            for (Entity e : level.getEntities().getAll()) {
                if ((!mustBeTamed || (e instanceof OwnableEntity tamable && tamable.getOwnerUUID() != null)) && this.entity.matches(serverPlayer.serverLevel(), e.position(), e)) {
                    target = e;
                    break;
                }
            }
            return target != null;
        }
        return false;
    }

    public EntityPredicate entity() {
        return entity;
    }

    public Optional<ResourceLocation> shader() {
        return shader;
    }

    public Optional<String> toggleTag() {
        return toggleTag;
    }

    public boolean mustBeTamed() {
        return mustBeTamed;
    }

    public boolean overrideOwner() {
        return overrideOwner;
    }

    @Override
    public Optional<Holder<SoundEvent>> startSound() {
        return startSound;
    }

    @Override
    public boolean overrideActive() {
        return overrideActive;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.SET_CAMERA_ENTITY.get();
    }
}
