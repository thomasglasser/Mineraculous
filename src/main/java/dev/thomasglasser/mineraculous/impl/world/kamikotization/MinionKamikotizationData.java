package dev.thomasglasser.mineraculous.impl.world.kamikotization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityData;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityUtils;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.KamikotizationAbilityHandler;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.level.storage.EntityReversionData;
import dev.thomasglasser.mineraculous.api.world.level.storage.ItemReversionData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.KamikotizedMinion;
import dev.thomasglasser.mineraculous.impl.world.item.component.KamikoData;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record MinionKamikotizationData(Holder<Kamikotization> kamikotization, KamikoData kamikoData, String name, Optional<UUID> revertibleId, int toolCount, boolean powerActive, boolean buffsActive) {

    public static final Codec<MinionKamikotizationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Kamikotization.CODEC.fieldOf("kamikotization").forGetter(MinionKamikotizationData::kamikotization),
            KamikoData.CODEC.fieldOf("kamiko_data").forGetter(MinionKamikotizationData::kamikoData),
            Codec.STRING.fieldOf("name").forGetter(MinionKamikotizationData::name),
            UUIDUtil.CODEC.optionalFieldOf("revertible_id").forGetter(MinionKamikotizationData::revertibleId),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("tool_count").forGetter(MinionKamikotizationData::toolCount),
            Codec.BOOL.fieldOf("power_active").forGetter(MinionKamikotizationData::powerActive),
            Codec.BOOL.fieldOf("buffs_active").forGetter(MinionKamikotizationData::buffsActive)).apply(instance, MinionKamikotizationData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MinionKamikotizationData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            Kamikotization.STREAM_CODEC, MinionKamikotizationData::kamikotization,
            KamikoData.STREAM_CODEC, MinionKamikotizationData::kamikoData,
            ByteBufCodecs.STRING_UTF8, MinionKamikotizationData::name,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), MinionKamikotizationData::revertibleId,
            ByteBufCodecs.VAR_INT, MinionKamikotizationData::toolCount,
            ByteBufCodecs.BOOL, MinionKamikotizationData::powerActive,
            ByteBufCodecs.BOOL, MinionKamikotizationData::buffsActive,
            MinionKamikotizationData::new);
    public MinionKamikotizationData(Holder<Kamikotization> kamikotization, KamikoData kamikoData, String name, int toolCount) {
        this(kamikotization, kamikoData, name, Optional.empty(), toolCount, false, false);
    }

    public void transform(KamikotizedMinion minion, ServerLevel level) {
        Kamikotization value = kamikotization.value();

        playKamikotizationEffects(minion, level, MineraculousSoundEvents.KAMIKOTIZATION_TRANSFORM);

        level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((effect, miraculousEffect) -> MineraculousEntityUtils.applyInfiniteHiddenEffect(minion, level.holderOrThrow(effect), miraculousEffect.amplifier() + ((!miraculousEffect.toggleable() || MineraculousServerConfig.get().enableBuffsOnTransformation.get()) ? kamikoData.powerLevel() / 10 : 0)));
        minion.getAttributes().addTransientAttributeModifiers(MiraculousData.getMiraculousAttributes(level, kamikoData.powerLevel()));

        KamikotizationData.equipKamikotizationArmor(minion, kamikotization);
        UUID revertibleId = null;
        if (value.powerSource().left().isPresent()) {
            ItemStack tool = value.powerSource().left().get();
            tool = tool.copyWithCount(Math.min(toolCount, tool.getMaxStackSize()));
            tool.set(MineraculousDataComponents.HIDE_ENCHANTMENTS, Unit.INSTANCE);
            tool.set(MineraculousDataComponents.KAMIKO_DATA, kamikoData);
            tool.set(MineraculousDataComponents.KAMIKOTIZATION, kamikotization);
            tool.set(MineraculousDataComponents.OWNER, minion.getUUID());
            revertibleId = UUID.randomUUID();
            ItemReversionData.get(level).putRemovable(minion.getUUID(), revertibleId);
            tool.set(MineraculousDataComponents.REVERTIBLE_ITEM_ID, revertibleId);
            minion.setItemInHand(InteractionHand.MAIN_HAND, tool);
        }

        AbilityData data = AbilityData.of(this);
        value.powerSource().right().ifPresent(ability -> ability.value().transform(data, level, minion));
        value.passiveAbilities().forEach(ability -> ability.value().transform(data, level, minion));
        EntityReversionData.get(level).startTracking(minion.getUUID());

        transform(revertibleId).save(minion);
    }

    public void detransform(KamikotizedMinion minion, ServerLevel level) {
        playKamikotizationEffects(minion, level, MineraculousSoundEvents.KAMIKOTIZATION_DETRANSFORM);

        Kamikotization value = kamikotization.value();
        AbilityData data = AbilityData.of(this);
        value.powerSource().right().ifPresent(ability -> ability.value().detransform(data, level, minion));
        value.passiveAbilities().forEach(ability -> ability.value().detransform(data, level, minion));
    }

    public void tick(KamikotizedMinion minion, ServerLevel level) {
        level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((key, miraculousEffect) -> {
            Holder<MobEffect> effect = level.holderOrThrow(key);
            if (!minion.hasEffect(effect)) {
                MineraculousEntityUtils.applyInfiniteHiddenEffect(minion, effect, miraculousEffect.amplifier() + ((!miraculousEffect.toggleable() || buffsActive) ? kamikoData.powerLevel() / 10 : 0));
            }
        });

        performAbilities(minion, level, null);
    }

    public void performAbilities(KamikotizedMinion minion, ServerLevel level, @Nullable AbilityContext context) {
        AbilityData data = AbilityData.of(this);
        KamikotizationAbilityHandler handler = new KamikotizationAbilityHandler(kamikotization);
        Ability.State state = AbilityUtils.performPassiveAbilities(level, minion, data, handler, context, kamikotization.value().passiveAbilities());
        if (powerActive) {
            if (state.shouldStop()) {
                withPowerActive(false).save(minion);
            } else {
                state = AbilityUtils.performActiveAbility(level, minion, data, handler, context, kamikotization.value().powerSource().right());
                withPowerActive(!state.shouldStop()).save(minion);
            }
        }
    }

    private void playKamikotizationEffects(KamikotizedMinion minion, ServerLevel level, Holder<SoundEvent> sound) {
        minion.playSound(sound.value());
        for (int i = 0; i < KamikotizationData.TRANSFORMATION_FRAMES; i++) {
            level.sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), minion.getX(), minion.getY() + 2 - ((Kamikotization.TRANSFORMATION_FRAMES + 1) - i) / 5.0, minion.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
        }
    }

    private MinionKamikotizationData transform(@Nullable UUID revertibleId) {
        return new MinionKamikotizationData(kamikotization, kamikoData, name, Optional.ofNullable(revertibleId), toolCount, false, MineraculousServerConfig.get().enableBuffsOnTransformation.get());
    }

    public MinionKamikotizationData withPowerActive(boolean powerActive) {
        return new MinionKamikotizationData(kamikotization, kamikoData, name, revertibleId, toolCount, powerActive, buffsActive);
    }

    public MinionKamikotizationData toggleBuffsActive() {
        return new MinionKamikotizationData(kamikotization, kamikoData, name, revertibleId, toolCount, powerActive, !buffsActive);
    }

    public void save(KamikotizedMinion minion) {
        minion.setKamikotizationData(this);
    }
}
