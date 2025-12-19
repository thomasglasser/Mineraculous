package dev.thomasglasser.mineraculous.impl.client.look;

import com.google.common.collect.ImmutableMap;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssets;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import java.util.Set;
import net.minecraft.resources.ResourceKey;

// TODO: Replace miraculous key set w metadata system
public record Look(String hash, String name, String author, Set<ResourceKey<Miraculous>> validMiraculouses, ImmutableMap<ResourceKey<LookContext>, LookAssets> assets) {}
