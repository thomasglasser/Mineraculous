package dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record MiraculousLadybugTargetType<T extends MiraculousLadybugTarget<T>>(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {}
