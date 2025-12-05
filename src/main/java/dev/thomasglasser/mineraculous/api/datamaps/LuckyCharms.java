package dev.thomasglasser.mineraculous.api.datamaps;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.world.ability.SummonTargetDependentLuckyCharmAbility;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Specifies a pool of items to summon for a related lucky charm target in {@link SummonTargetDependentLuckyCharmAbility}.
 * 
 * @param items The pool of items
 */
public record LuckyCharms(Either<ResourceKey<LootTable>, HolderSet<Item>> items) {
    public static final Codec<LuckyCharms> CODEC = Codec.withAlternative(
            ResourceKey.codec(Registries.LOOT_TABLE).xmap(LuckyCharms::new, lc -> lc.items().left().orElseThrow()),
            Codec.xor(
                    ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").xmap(LuckyCharms::new, lc -> lc.items.left().orElseThrow()).codec(),
                    HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false).fieldOf("items").xmap(LuckyCharms::new, lc -> lc.items.right().orElseThrow()).codec()).xmap(Either::unwrap, Either::left));

    public LuckyCharms(ResourceKey<LootTable> lootTable) {
        this(Either.left(lootTable));
    }

    public LuckyCharms(HolderSet<Item> items) {
        this(Either.right(items));
    }
}
