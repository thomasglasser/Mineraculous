package dev.thomasglasser.mineraculous.impl.client.look;

import com.google.common.collect.ImmutableMap;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssets;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import net.minecraft.resources.ResourceKey;

// TODO: Add metadata system and valid miraculouses & kamikotizations
public record Look(String hash, String name, String author, ImmutableMap<ResourceKey<LookContext>, LookAssets> assets) {}
