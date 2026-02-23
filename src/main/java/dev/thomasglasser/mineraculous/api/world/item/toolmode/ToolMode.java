/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package dev.thomasglasser.mineraculous.api.world.item.toolmode;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class ToolMode {
    public static final Codec<ToolMode> CODEC = ResourceLocation.CODEC.xmap(ToolMode::get, ToolMode::id);
    public static final StreamCodec<ByteBuf, ToolMode> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(ToolMode::get, ToolMode::id);

    private static final Map<ResourceLocation, ToolMode> modes = new ConcurrentHashMap<>();

    /**
     * Gets or creates a new ToolMode for the given id.
     */
    public static ToolMode get(ResourceLocation name) {
        return modes.computeIfAbsent(name, ToolMode::new);
    }

    /**
     * Returns all registered modes.
     * This collection can be kept around, and will update itself in response to changes to the map.
     * See {@link ConcurrentHashMap#values()} for details.
     */
    public static Collection<ToolMode> getModes() {
        return Collections.unmodifiableCollection(modes.values());
    }

    private final ResourceLocation id;

    /**
     * Use {@link #get(ResourceLocation)} to get or create a ItemAbility
     */
    private ToolMode(ResourceLocation id) {
        this.id = id;
    }

    /**
     * Returns the id of this item ability
     */
    public ResourceLocation id() {
        return id;
    }

    @Override
    public String toString() {
        return "ToolMode[" + id + "]";
    }
}
