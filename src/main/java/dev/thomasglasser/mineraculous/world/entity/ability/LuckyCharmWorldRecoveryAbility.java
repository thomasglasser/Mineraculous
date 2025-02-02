package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ClientboundCheckLuckyCharmWorldRecoveryPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharm;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharmIdDataHolder;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
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

public record LuckyCharmWorldRecoveryAbility(boolean requireInHand, Optional<ParticleOptions> spreadParticle, Optional<Holder<SoundEvent>> startSound, boolean overrideActive) implements Ability {

    public static final MapCodec<LuckyCharmWorldRecoveryAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("require_in_hand", false).forGetter(LuckyCharmWorldRecoveryAbility::requireInHand),
            ParticleTypes.CODEC.optionalFieldOf("spread_particle").forGetter(LuckyCharmWorldRecoveryAbility::spreadParticle),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(LuckyCharmWorldRecoveryAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(LuckyCharmWorldRecoveryAbility::overrideActive)).apply(instance, LuckyCharmWorldRecoveryAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Context context) {
        if (context == Context.PASSIVE) {
            if (entity instanceof ServerPlayer serverPlayer)
                TommyLibServices.NETWORK.sendToClient(new ClientboundCheckLuckyCharmWorldRecoveryPayload(data, spreadParticle, startSound), serverPlayer);
            return true;
        }
        return false;
    }

    @Override
    public boolean canActivate(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {
        if (requireInHand) {
            ItemStack mainHandItem = entity.getMainHandItem();
            Either<ResourceKey<Miraculous>, ResourceKey<Kamikotization>> power = data.power();
            LuckyCharm luckyCharm = mainHandItem.get(MineraculousDataComponents.LUCKY_CHARM);
            if (luckyCharm != null) {
                if (power.left().isPresent()) {
                    KwamiData stackKwamiData = mainHandItem.get(MineraculousDataComponents.KWAMI_DATA);
                    return stackKwamiData != null && stackKwamiData.uuid() == entity.getData(MineraculousAttachmentTypes.MIRACULOUS).get(power.left().get()).miraculousItem().get(MineraculousDataComponents.KWAMI_DATA).uuid() && luckyCharm.id() == ((LuckyCharmIdDataHolder) level.getServer().overworld()).mineraculous$getLuckyCharmIdData().getLuckyCharmId(stackKwamiData.uuid());
                } else {
                    return mainHandItem.get(MineraculousDataComponents.KAMIKOTIZATION) == power.right().get() && luckyCharm.id() == ((LuckyCharmIdDataHolder) level.getServer().overworld()).mineraculous$getLuckyCharmIdData().getLuckyCharmId(entity.getUUID());
                }
            }
            return false;
        }
        return true;
    }

    public static void beginRecovery(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Optional<ParticleOptions> spreadParticle, Optional<Holder<SoundEvent>> startSound) {
        ItemStack luckyCharm = entity.getMainHandItem();
        Optional<Entity> target = luckyCharm.get(MineraculousDataComponents.LUCKY_CHARM).target().map(level::getEntity);
        if (target.isPresent()) {
            // TODO: Heal everything from target
            System.out.println("You're healed!");
        }
        UUID charmId;
        if (data.power().left().isPresent()) {
            charmId = luckyCharm.get(MineraculousDataComponents.KWAMI_DATA).uuid();
        } else {
            charmId = entity.getUUID();
        }
        ((LuckyCharmIdDataHolder) level.getServer().overworld()).mineraculous$getLuckyCharmIdData().incrementLuckyCharmId(charmId);
        startSound.ifPresent(sound -> level.playSound(null, pos, sound.value(), SoundSource.PLAYERS, 1, 1));
        spreadParticle.ifPresent(particleOptions -> level.sendParticles(particleOptions, entity.getX(), entity.getY() + 1, entity.getZ(), 100, level.random.nextInt(-4, 5) / 10.0, 0, level.random.nextInt(-4, 5) / 10.0, 0));
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.LUCKY_CHARM_WORLD_RECOVERY.get();
    }
}
