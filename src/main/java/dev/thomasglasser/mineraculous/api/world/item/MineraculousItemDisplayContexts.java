package dev.thomasglasser.mineraculous.api.world.item;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import org.jetbrains.annotations.Nullable;

/// @see dev.thomasglasser.mineraculous.api.client.renderer.item.curio.ContextDependentCurioRenderer
public class MineraculousItemDisplayContexts {
    private static final String NONE = "NONE";
    private static final String HEAD = "HEAD";
    private static final String THIRD_PERSON_LEFT_HAND = "THIRD_PERSON_LEFT_HAND";
    private static final String THIRD_PERSON_RIGHT_HAND = "THIRD_PERSON_RIGHT_HAND";
    private static final String MINERACULOUS_CURIOS_LEFT_EARRING = "MINERACULOUS_CURIOS_LEFT_EARRING";

    /// Default and fallback, aligns with {@link net.minecraft.client.model.HumanoidModel#body}
    public static final EnumProxy<ItemDisplayContext> CURIOS_BODY = register("curios_body", NONE);
    /// Aligns with {@link net.minecraft.client.model.HumanoidModel#head}
    public static final EnumProxy<ItemDisplayContext> CURIOS_HEAD = register("curios_head", HEAD);
    /// Special-cased to align with and render once on each side of {@link net.minecraft.client.model.HumanoidModel#head}
    public static final EnumProxy<ItemDisplayContext> CURIOS_LEFT_EARRING = register("curios_left_earring", HEAD);
    public static final EnumProxy<ItemDisplayContext> CURIOS_RIGHT_EARRING = register("curios_right_earring", MINERACULOUS_CURIOS_LEFT_EARRING);
    /// Aligns with {@link net.minecraft.client.model.HumanoidModel#leftArm}
    public static final EnumProxy<ItemDisplayContext> CURIOS_LEFT_ARM = register("curios_left_arm", THIRD_PERSON_LEFT_HAND);
    /// Aligns with {@link net.minecraft.client.model.HumanoidModel#rightArm}
    public static final EnumProxy<ItemDisplayContext> CURIOS_RIGHT_ARM = register("curios_right_arm", THIRD_PERSON_RIGHT_HAND);
    /// Aligns with {@link net.minecraft.client.model.HumanoidModel#leftLeg}
    public static final EnumProxy<ItemDisplayContext> CURIOS_LEFT_LEG = register("curios_left_leg", null);
    /// Aligns with {@link net.minecraft.client.model.HumanoidModel#rightLeg}
    public static final EnumProxy<ItemDisplayContext> CURIOS_RIGHT_LEG = register("curios_right_leg", null);

    private static EnumProxy<ItemDisplayContext> register(String name, @Nullable String fallback) {
        return new EnumProxy<>(ItemDisplayContext.class, -1, MineraculousConstants.modLoc(name).toString(), fallback);
    }
}
