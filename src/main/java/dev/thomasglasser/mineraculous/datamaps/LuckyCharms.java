package dev.thomasglasser.mineraculous.datamaps;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.EitherCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;

public record LuckyCharms(Either<HolderSet<Item>, ResourceKey<LootTable>> items) {
    public static final Codec<LuckyCharms> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            new EitherCodec<>(HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false), ResourceKey.codec(Registries.LOOT_TABLE)).fieldOf("items").forGetter(LuckyCharms::items)).apply(instance, LuckyCharms::new));

    public LuckyCharms(HolderSet<Item> items) {
        this(Either.left(items));
    }

    public LuckyCharms(ResourceKey<LootTable> lootTable) {
        this(Either.right(lootTable));
    }
}
