package dev.thomasglasser.mineraculous.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.thomasglasser.mineraculous.Mineraculous;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MineraculousRenderTypes {
    private static final Function<String, ResourceLocation> LUCKY_CHARM_TEXTURE = type -> Mineraculous.modLoc("textures/misc/lucky_charm_" + type + ".png");
    private static final ResourceLocation ENTITY_LUCKY_CHARM_TEXTURE = LUCKY_CHARM_TEXTURE.apply("entity");

    private static final RenderType LUCKY_CHARM = createLuckyCharm(null, Mineraculous.modLoc("textures/misc/lucky_charm.png"), MineraculousRenderStateShards.LUCKY_CHARM_TEXTURING, false);

    private static final RenderType ARMOR_LUCKY_CHARM = createLuckyCharm("armor", LUCKY_CHARM_TEXTURE.apply("armor"), MineraculousRenderStateShards.ARMOR_LUCKY_CHARM_TEXTURING, true);

    private static final RenderType ENTITY_LUCKY_CHARM = createLuckyCharm("entity", ENTITY_LUCKY_CHARM_TEXTURE, MineraculousRenderStateShards.ENTITY_LUCKY_CHARM_TEXTURING, false);

    private static final RenderType SHIELD_LUCKY_CHARM = createLuckyCharm("shield", ENTITY_LUCKY_CHARM_TEXTURE, MineraculousRenderStateShards.SHIELD_LUCKY_CHARM_TEXTURING, false);

    public static RenderType luckyCharm() {
        return LUCKY_CHARM;
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

    private static RenderType createLuckyCharm(@Nullable String name, ResourceLocation texture, RenderStateShard.TexturingStateShard texturingStateShard, boolean offsetZLayering) {
        return RenderType.create(
                Mineraculous.modLoc(name == null ? "lucky_charm" : name + "_lucky_charm").toString(),
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
}
