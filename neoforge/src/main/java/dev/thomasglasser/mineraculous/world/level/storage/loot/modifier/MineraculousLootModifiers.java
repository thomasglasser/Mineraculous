package dev.thomasglasser.mineraculous.world.level.storage.loot.modifier;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.Mineraculous;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MineraculousLootModifiers
{
	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Mineraculous.MOD_ID);

	public static final DeferredHolder<Codec<? extends IGlobalLootModifier>, Codec<CataclysmDustConversionLootModifier>> CATACLYSM_DUST_CONVERSION = LOOT_MODIFIERS.register("cataclysm_dust_conversion", () -> CataclysmDustConversionLootModifier.CODEC);
}
