package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.network.ClientboundSetCameraEntityPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityEffectData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.Nullable;

public record KamikoControlAbility(Optional<ResourceLocation> shader, Optional<ResourceLocation> faceMaskTexture) implements Ability {
    public static final MapCodec<KamikoControlAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("shader").forGetter(KamikoControlAbility::shader),
            ResourceLocation.CODEC.optionalFieldOf("face_mask_texture").forGetter(KamikoControlAbility::faceMaskTexture)).apply(instance, KamikoControlAbility::new));

    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        if (context == null) {
            AbilityEffectData abilityEffectData = performer.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
            if (abilityEffectData.spectationInterrupted()) {
                stopKamikoControl(performer);
                return true;
            } else if (data.powerActive()) {
                if (abilityEffectData.isSpectating()) {
                    stopKamikoControl(performer);
                    return true;
                } else {
                    List<? extends Kamiko> kamikos = level.getEntities(EntityTypeTest.forClass(Kamiko.class), kamiko -> performer.getUUID().equals(kamiko.getOwnerUUID()));
                    if (!kamikos.isEmpty()) {
                        abilityEffectData.withKamikoControl(true, shader, faceMaskTexture).save(performer, true);
                        if (performer instanceof ServerPlayer player) {
                            TommyLibServices.NETWORK.sendToClient(new ClientboundSetCameraEntityPayload(Optional.of(kamikos.getFirst().getId()), shader), player);
                        }
                        return true;
                    }
                }
            } else {
                return abilityEffectData.isSpectating();
            }
        }
        return false;
    }

    private void stopKamikoControl(Entity performer) {
        performer.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withKamikoControl(false, Optional.empty(), Optional.empty()).save(performer, true);
        if (performer instanceof ServerPlayer player) {
            TommyLibServices.NETWORK.sendToClient(new ClientboundSetCameraEntityPayload(Optional.empty()), player);
        }
    }

    @Override
    public void detransform(AbilityData data, ServerLevel level, Entity performer) {
        stopKamikoControl(performer);
    }

    @Override
    public void leaveLevel(AbilityData data, ServerLevel level, Entity performer) {
        stopKamikoControl(performer);
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.KAMIKO_CONTROL.get();
    }
}
