package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ClientboundCheckLuckyCharmWorldRecoveryPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.KamikotizedPowerSourceItem;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionItemData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharm;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharmIdData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record RevertAbilityEffectsAbility(boolean requireInHand, Optional<ParticleOptions> spreadParticle, Optional<Holder<SoundEvent>> startSound) implements Ability {

    public static final MapCodec<RevertAbilityEffectsAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("require_in_hand", false).forGetter(RevertAbilityEffectsAbility::requireInHand),
            ParticleTypes.CODEC.optionalFieldOf("spread_particle").forGetter(RevertAbilityEffectsAbility::spreadParticle),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(RevertAbilityEffectsAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(RevertAbilityEffectsAbility::overrideActive)).apply(instance, RevertAbilityEffectsAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, Context context) {
        if (context == Context.PASSIVE) {
            if (performer instanceof ServerPlayer serverPlayer)
                TommyLibServices.NETWORK.sendToClient(new ClientboundCheckLuckyCharmWorldRecoveryPayload(data, spreadParticle, startSound), serverPlayer);
            return true;
        }
        return false;
    }

    @Override
    public boolean canActivate(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        if (requireInHand) {
            ItemStack mainHandItem = performer.getMainHandItem();
            Either<ResourceKey<Miraculous>, ResourceKey<Kamikotization>> power = data.power();
            LuckyCharm luckyCharm = mainHandItem.get(MineraculousDataComponents.LUCKY_CHARM);
            if (luckyCharm != null) {
                if (power.left().isPresent()) {
                    KwamiData stackKwamiData = mainHandItem.get(MineraculousDataComponents.KWAMI_DATA);
                    return stackKwamiData != null && stackKwamiData.uuid().equals(performer.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(power.left().get()).miraculousItem().get(MineraculousDataComponents.KWAMI_DATA).uuid()) && luckyCharm.id() == LuckyCharmIdData.get(level).getLuckyCharmId(stackKwamiData.uuid());
                } else {
                    return mainHandItem.get(MineraculousDataComponents.KAMIKOTIZATION) == power.right().get() && luckyCharm.id() == LuckyCharmIdData.get(level).getLuckyCharmId(performer.getUUID());
                }
            }
            return false;
        }
        return true;
    }

    public static void beginRecovery(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Optional<ParticleOptions> spreadParticle, Optional<Holder<SoundEvent>> startSound) {
        ItemStack luckyCharm = entity.getMainHandItem();
        Optional<UUID> target = luckyCharm.get(MineraculousDataComponents.LUCKY_CHARM).target();
        if (target.isPresent()) {
            AbilityReversionEntityData abilityReversionEntityData = AbilityReversionEntityData.get(level);
            for (UUID related : abilityReversionEntityData.getTrackedAndRelatedEntities(target.get())) {
                LivingEntity recovering = level.getEntity(related) instanceof LivingEntity livingEntity ? livingEntity : null;
                if (recovering != null) {
                    MiraculousesData miraculousesData = recovering.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                    Set<ResourceKey<Miraculous>> transformed = miraculousesData.keySet();
                    Optional<ResourceKey<Kamikotization>> kamikotizationKey = recovering.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).map(KamikotizationData::kamikotization);
                    if (kamikotizationKey.isEmpty())
                        kamikotizationKey = recovering.getData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION);
                    if (kamikotizationKey.isPresent()) {
                        Kamikotization kamikotization = level.holderOrThrow(kamikotizationKey.get()).value();
                        AbilityData abilityData = new AbilityData(0, Either.right(kamikotizationKey.get()));
                        if (kamikotization.powerSource().left().isPresent()) {
                            if (kamikotization.powerSource().left().get().getItem() instanceof KamikotizedPowerSourceItem item)
                                item.restore(recovering);
                        } else
                            kamikotization.powerSource().right().get().value().revert(abilityData, level, recovering);
                        kamikotization.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, recovering));
                    }
                    for (ResourceKey<Miraculous> miraculousKey : transformed) {
                        Miraculous miraculous = level.holderOrThrow(miraculousKey).value();
                        MiraculousData miraculousData = miraculousesData.get(miraculousKey);
                        AbilityData abilityData = new AbilityData(miraculousData.powerLevel(), Either.left(miraculousKey));
                        miraculous.activeAbility().ifPresent(ability -> ability.value().restore(abilityData, level, recovering));
                        miraculous.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, recovering));
                    }
                }
            }
            abilityReversionEntityData.stopTracking(target.get());
            AbilityReversionItemData.get(level).recoverKamikotized(target.get(), level);
        }
        UUID charmId;
        if (data.power().left().isPresent()) {
            charmId = luckyCharm.get(MineraculousDataComponents.KWAMI_DATA).uuid();
        } else {
            charmId = entity.getUUID();
        }
        LuckyCharmIdData.get(level).incrementLuckyCharmId(charmId);
        startSound.ifPresent(sound -> level.playSound(null, pos, sound.value(), SoundSource.PLAYERS, 1, 1));
        spreadParticle.ifPresent(particleOptions -> level.sendParticles(particleOptions, entity.getX(), entity.getY() + 1, entity.getZ(), 100, level.random.nextInt(-4, 5) / 10.0, 0, level.random.nextInt(-4, 5) / 10.0, 0));
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.RECOVER_ABILITY_DAMAGE.get();
    }
}
