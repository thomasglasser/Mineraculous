package dev.thomasglasser.mineraculous.api.world.item;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import org.jetbrains.annotations.Nullable;

/// @see ContextDependentCurioRenderer
public class MineraculousItemDisplayContexts {
    private static final String NONE = "none";
    private static final String HEAD = "head";
    private static final String THIRD_PERSON_LEFT_HAND = "thirdpersion_lefthand";
    private static final String THIRD_PERSON_RIGHT_HAND = "thirdpersion_righthand";

    /// Default and fallback, aligns with {@link HumanoidModel#body}
    public static final EnumProxy<ItemDisplayContext> CURIOS_BODY = register("curios_body", "none");
    /// Aligns with {@link HumanoidModel#head}
    public static final EnumProxy<ItemDisplayContext> CURIOS_HEAD = register("curios_head", "head");
    /// Special-cased to align with and render once on each side of {@link HumanoidModel#head}
    public static final EnumProxy<ItemDisplayContext> CURIOS_EARRINGS = register("curios_earrings", "head");
    /// Aligns with {@link HumanoidModel#leftArm}
    public static final EnumProxy<ItemDisplayContext> CURIOS_LEFT_ARM = register("curios_left_arm", THIRD_PERSON_LEFT_HAND);
    /// Aligns with {@link HumanoidModel#rightArm}
    public static final EnumProxy<ItemDisplayContext> CURIOS_RIGHT_ARM = register("curios_right_arm", THIRD_PERSON_RIGHT_HAND);
    /// Aligns with {@link HumanoidModel#leftLeg}
    public static final EnumProxy<ItemDisplayContext> CURIOS_LEFT_LEG = register("curios_left_leg", null);
    /// Aligns with {@link HumanoidModel#rightLeg}
    public static final EnumProxy<ItemDisplayContext> CURIOS_RIGHT_LEG = register("curios_right_leg", null);

    private static EnumProxy<ItemDisplayContext> register(String name, @Nullable String fallback) {
        return new EnumProxy<>(ItemDisplayContext.class, -1, Mineraculous.modLoc(name).toString(), fallback);
    }
}
