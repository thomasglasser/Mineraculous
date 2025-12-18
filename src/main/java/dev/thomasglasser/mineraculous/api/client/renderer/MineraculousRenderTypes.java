package dev.thomasglasser.mineraculous.api.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import java.util.function.BiFunction;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MineraculousRenderTypes {
    private static final ResourceLocation ENTITY_LUCKY_CHARM_TEXTURE = createLuckyCharmTexture("entity");
    private static final ResourceLocation ENTITY_KAMIKOTIZING_TEXTURE = createKamikotizingTexture("entity");
    private static final ResourceLocation LADYBUG_BODY_TEXTURE = MineraculousConstants.modLoc("textures/particle/ladybug.png");
    private static final ResourceLocation LADYBUG_OUTLINE_TEXTURE = MineraculousConstants.modLoc("textures/particle/ladybug_glow.png");

    private static final RenderType ITEM_LUCKY_CHARM = createLuckyCharm("item", MineraculousRenderStateShards.ITEM_LUCKY_CHARM_TEXTURING, false);
    private static final RenderType ARMOR_LUCKY_CHARM = createLuckyCharm("armor", MineraculousRenderStateShards.ARMOR_LUCKY_CHARM_TEXTURING, true);
    private static final RenderType ENTITY_LUCKY_CHARM = createLuckyCharm("entity", ENTITY_LUCKY_CHARM_TEXTURE, MineraculousRenderStateShards.ENTITY_LUCKY_CHARM_TEXTURING, false);
    private static final RenderType SHIELD_LUCKY_CHARM = createLuckyCharm("shield", ENTITY_LUCKY_CHARM_TEXTURE, MineraculousRenderStateShards.SHIELD_LUCKY_CHARM_TEXTURING, false);

    private static final RenderType ITEM_KAMIKOTIZING = createKamikotizing("item", MineraculousRenderStateShards.ITEM_KAMIKOTIZING_TEXTURING, false);
    private static final RenderType ARMOR_KAMIKOTIZING = createKamikotizing("armor", MineraculousRenderStateShards.ARMOR_KAMIKOTIZING_TEXTURING, true);
    private static final RenderType ENTITY_KAMIKOTIZING = createKamikotizing("entity", ENTITY_KAMIKOTIZING_TEXTURE, MineraculousRenderStateShards.ENTITY_KAMIKOTIZING_TEXTURING, false);
    private static final RenderType SHIELD_KAMIKOTIZING = createKamikotizing("shield", ENTITY_KAMIKOTIZING_TEXTURE, MineraculousRenderStateShards.SHIELD_KAMIKOTIZING_TEXTURING, false);

    private static final RenderType MIRACULOUS_LADYBUG_BODY = createMiraculousLadybugBody();
    private static final RenderType MIRACULOUS_LADYBUG_OUTLINE = createMiraculousLadybugOutline();

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

    public static RenderType itemKamikotizing() {
        return ITEM_KAMIKOTIZING;
    }

    public static RenderType armorKamikotizing() {
        return ARMOR_KAMIKOTIZING;
    }

    public static RenderType entityKamikotizing() {
        return ENTITY_KAMIKOTIZING;
    }

    public static RenderType shieldKamikotizing() {
        return SHIELD_KAMIKOTIZING;
    }

    public static RenderType miraculousLadybugBody() {
        return MIRACULOUS_LADYBUG_BODY;
    }

    public static RenderType miraculousLadybugOutline() {
        return MIRACULOUS_LADYBUG_OUTLINE;
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
     * Returns the default kamikotizing texture location based on the provided name.
     *
     * @param name The name to create the texture location for
     * @return The kamikotizing texture location
     */
    public static ResourceLocation createKamikotizingTexture(ResourceLocation name) {
        return name.withPath(path -> "textures/misc/kamikotizing_" + path + ".png");
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
    public static RenderType createItemShader(ResourceLocation name, ResourceLocation texture, RenderStateShard.TexturingStateShard texturingStateShard, boolean offsetZLayering) {
        return RenderType.create(
                name.toString(),
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
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
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
        return createItemShader(name.withSuffix("_lucky_charm"), createLuckyCharmTexture(name), texturingStateShard, offsetZLayering);
    }

    /**
     * Creates a kamikotizing {@link RenderType} with the given name and a default texture derived from the name.
     *
     * @param name                The name of the render type
     * @param texturingStateShard The texturing state shard of the render type
     * @param offsetZLayering     Whether to enable offset z layering
     * @return The kamikotizing {@link RenderType}
     */
    public static RenderType createKamikotizing(ResourceLocation name, RenderStateShard.TexturingStateShard texturingStateShard, boolean offsetZLayering) {
        return createItemShader(name.withSuffix("_kamikotizing"), createKamikotizingTexture(name), texturingStateShard, offsetZLayering);
    }

    private static ResourceLocation createLuckyCharmTexture(String name) {
        return createLuckyCharmTexture(MineraculousConstants.modLoc(name));
    }

    private static ResourceLocation createKamikotizingTexture(String name) {
        return createKamikotizingTexture(MineraculousConstants.modLoc(name));
    }

    private static RenderType createLuckyCharm(String name, RenderStateShard.TexturingStateShard texturingStateShard, boolean offsetZLayering) {
        return createLuckyCharm(MineraculousConstants.modLoc(name), texturingStateShard, offsetZLayering);
    }

    private static RenderType createLuckyCharm(String name, ResourceLocation texture, RenderStateShard.TexturingStateShard texturingStateShard, boolean offsetZLayering) {
        return createItemShader(MineraculousConstants.modLoc(name + "_lucky_charm"), texture, texturingStateShard, offsetZLayering);
    }

    private static RenderType createKamikotizing(String name, RenderStateShard.TexturingStateShard texturingStateShard, boolean offsetZLayering) {
        return createKamikotizing(MineraculousConstants.modLoc(name), texturingStateShard, offsetZLayering);
    }

    private static RenderType createKamikotizing(String name, ResourceLocation texture, RenderStateShard.TexturingStateShard texturingStateShard, boolean offsetZLayering) {
        return createItemShader(MineraculousConstants.modLoc(name + "_kamikotizing"), texture, texturingStateShard, offsetZLayering);
    }

    private static RenderType createMiraculousLadybugBody() {
        return RenderType.create(
                "miraculous_ladybug_main",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                true,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(MineraculousRenderStateShards.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_NO_LIGHTMAP_SHADER)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY) // keeps alpha blending
                        .setTextureState(new RenderStateShard.TextureStateShard(LADYBUG_BODY_TEXTURE, false, false))
                        .setCullState(RenderStateShard.CULL)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .createCompositeState(false));
    }

    private static RenderType createMiraculousLadybugOutline() {
        return RenderType.create(
                "miraculous_ladybug_outline",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(MineraculousRenderStateShards.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_NO_LIGHTMAP_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(LADYBUG_OUTLINE_TEXTURE, false, true))
                        .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .createCompositeState(false));
    }

    public static RenderType kwamiGlowColor(ResourceLocation location) {
        return KWAMI_GLOW.apply(location, RenderStateShard.NO_CULL);
    }

    public static final RenderStateShard.OutputStateShard KWAMI_TARGET = new RenderStateShard.OutputStateShard(
            "kwami_target",
            () -> MineraculousClientUtils.kwamiTarget.bindWrite(false),
            () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false));

    static final BiFunction<ResourceLocation, RenderStateShard.CullStateShard, RenderType> KWAMI_GLOW = Util.memoize(
            (p_349872_, p_349873_) -> RenderType.create(
                    "outline",
                    DefaultVertexFormat.POSITION_TEX_COLOR,
                    VertexFormat.Mode.QUADS,
                    1536,
                    RenderType.CompositeState.builder()
                            .setShaderState(RenderStateShard.RENDERTYPE_OUTLINE_SHADER)
                            .setTextureState(new RenderStateShard.TextureStateShard(p_349872_, false, false))
                            .setCullState(p_349873_)
                            .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                            .setOutputState(KWAMI_TARGET)
                            .createCompositeState(RenderType.OutlineProperty.IS_OUTLINE)));
}
