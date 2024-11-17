package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import net.minecraft.resources.ResourceKey;

public record AbilityData(int powerLevel, Either<ResourceKey<Miraculous>, ResourceKey<Kamikotization>> power) {}
