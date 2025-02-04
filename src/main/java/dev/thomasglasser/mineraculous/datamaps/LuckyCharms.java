package dev.thomasglasser.mineraculous.datamaps;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.EitherCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;

public record LuckyCharms(Either<ResourceKey<LootTable>, HolderSet<Item>> items) {
    public static final Codec<LuckyCharms> CODEC = new EitherCodec<>(ResourceKey.codec(Registries.LOOT_TABLE), HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false)).xmap(LuckyCharms::new, LuckyCharms::items);

    public LuckyCharms(ResourceKey<LootTable> lootTable) {
        this(Either.left(lootTable));
    }

    public LuckyCharms(HolderSet<Item> items) {
        this(Either.right(items));
    }
}
