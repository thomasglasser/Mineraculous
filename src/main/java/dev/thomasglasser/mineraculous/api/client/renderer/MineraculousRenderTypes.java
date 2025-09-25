package dev.thomasglasser.mineraculous.api.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MineraculousRenderTypes {
    private static final ResourceLocation ENTITY_LUCKY_CHARM_TEXTURE = createLuckyCharmTexture("entity");

    private static final RenderType ITEM_LUCKY_CHARM = createLuckyCharm("item", MineraculousRenderStateShards.ITEM_LUCKY_CHARM_TEXTURING, false);
    private static final RenderType ARMOR_LUCKY_CHARM = createLuckyCharm("armor", MineraculousRenderStateShards.ARMOR_LUCKY_CHARM_TEXTURING, true);
    private static final RenderType ENTITY_LUCKY_CHARM = createLuckyCharm("entity", ENTITY_LUCKY_CHARM_TEXTURE, MineraculousRenderStateShards.ENTITY_LUCKY_CHARM_TEXTURING, false);
    private static final RenderType SHIELD_LUCKY_CHARM = createLuckyCharm("shield", ENTITY_LUCKY_CHARM_TEXTURE, MineraculousRenderStateShards.SHIELD_LUCKY_CHARM_TEXTURING, false);

    public static RenderType itemLuckyCharm() {
        return ITEM_LUCKY_CHARM;
    }

    public static RenderType armorLuckyCharm() {
        return ARMOR_LUCKY_CHARM;
    }

    public static RenderType entityLuckyCharm() {
        return ENTITY_LUCKY_CHARM;
    }

    public static RenderType shieldLuckyCharm() {
        return SHIELD_LUCKY_CHARM;
    }

    /**
     * Returns the default lucky charm texture location based on the provided name.
     * 
     * @param name The name to create the texture location for
     * @return The lucky charm texture location
     */
    public static ResourceLocation createLuckyCharmTexture(ResourceLocation name) {
        return name.withPath(path -> "textures/misc/lucky_charm_" + path + ".png");
    }

    /**
     * Returns a lucky charm {@link RenderType} with the provided name, texture, and texturing shard.
     * 
     * @param name                The name of the render type
     * @param texture             The texture location of the render type
     * @param texturingStateShard The texturing state shard of the render type
     * @param offsetZLayering     Whether to enable offset z layering
     * @return The lucky charm {@link RenderType}
     */
    public static RenderType createLuckyCharm(ResourceLocation name, ResourceLocation texture, RenderStateShard.TexturingStateShard texturingStateShard, boolean offsetZLayering) {
        return RenderType.create(
                name.withSuffix("_lucky_charm").toString(),
                DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR,
                VertexFormat.Mode.QUADS,
                1536,
                RenderType.CompositeState.builder()
                        .setShaderState(MineraculousRenderStateShards.RENDERTYPE_GLINT_TRANSLUCENT_LIGHTMAP_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, true))
                        .setTexturingState(texturingStateShard)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                        .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                        .setLayeringState(offsetZLayering ? RenderStateShard.VIEW_OFFSET_Z_LAYERING : RenderStateShard.NO_LAYERING)
                        .setCullState(RenderStateShard.NO_CULL)
                        .createCompositeState(false));
    }

    /**
     * Creates a lucky charm {@link RenderType} with the given name and a default texture derived from the name.
     *
     * @param name                The name of the render type
     * @param texturingStateShard The texturing state shard of the render type
     * @param offsetZLayering     Whether to enable offset z layering
     * @return The lucky charm {@link RenderType}
     */
    public static RenderType createLuckyCharm(ResourceLocation name, RenderStateShard.TexturingStateShard texturingStateShard, boolean offsetZLayering) {
        return createLuckyCharm(name, createLuckyCharmTexture(name), texturingStateShard, offsetZLayering);
    }

    private static ResourceLocation createLuckyCharmTexture(String name) {
        return createLuckyCharmTexture(MineraculousConstants.modLoc(name));
    }

    private static RenderType createLuckyCharm(String name, ResourceLocation texture, RenderStateShard.TexturingStateShard texturingStateShard, boolean offsetZLayering) {
        return createLuckyCharm(MineraculousConstants.modLoc(name), texture, texturingStateShard, offsetZLayering);
    }

    private static RenderType createLuckyCharm(String name, RenderStateShard.TexturingStateShard texturingStateShard, boolean offsetZLayering) {
        return createLuckyCharm(MineraculousConstants.modLoc(name), texturingStateShard, offsetZLayering);
    }
}
