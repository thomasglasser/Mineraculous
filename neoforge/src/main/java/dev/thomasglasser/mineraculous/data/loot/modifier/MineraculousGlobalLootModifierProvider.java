package dev.thomasglasser.mineraculous.data.loot.modifier;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.loot.modifier.CataclysmDustConversionLootModifier;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

public class MineraculousGlobalLootModifierProvider extends GlobalLootModifierProvider
{
	public MineraculousGlobalLootModifierProvider(PackOutput output)
	{
		super(output, Mineraculous.MOD_ID);
	}

	@Override
	protected void start()
	{
		CompoundTag cataclysmedTag = new CompoundTag();
		cataclysmedTag.putBoolean(MineraculousEntityEvents.TAG_CATACLYSMED, true);
		CompoundTag neoTag = new CompoundTag();
		neoTag.put("NeoForgeData", cataclysmedTag);
		add("cataclysm_dust_conversion", new CataclysmDustConversionLootModifier(new LootItemCondition[] { LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, new EntityPredicate.Builder().nbt(new NbtPredicate(neoTag)).build()).build() }));
	}
}
