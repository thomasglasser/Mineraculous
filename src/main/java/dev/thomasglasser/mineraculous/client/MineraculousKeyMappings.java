package dev.thomasglasser.mineraculous.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.function.Supplier;
import net.minecraft.client.KeyMapping;

public class MineraculousKeyMappings {
    public static final String MIRACULOUS_CATEGORY = "key.categories.mineraculous";

    public static final Supplier<KeyMapping> TRANSFORM = register("transform", InputConstants.KEY_M, MIRACULOUS_CATEGORY);
    public static final Supplier<KeyMapping> ACTIVATE_POWER = register("activate_power", InputConstants.KEY_O, MIRACULOUS_CATEGORY);
    public static final Supplier<KeyMapping> ACTIVATE_TOOL = register("activate_tool", InputConstants.KEY_U, MIRACULOUS_CATEGORY);
    public static final Supplier<KeyMapping> OPEN_TOOL_WHEEL = register("open_tool_wheel", InputConstants.KEY_H, MIRACULOUS_CATEGORY);
    public static final Supplier<KeyMapping> TAKE_BREAK_ITEM = register("take_break_item", InputConstants.KEY_I, KeyMapping.CATEGORY_GAMEPLAY);
    public static final Supplier<KeyMapping> INCREASE_SWINGING_ROPE_LENGTH = register("increase_swinging_rope_length", InputConstants.KEY_DOWN, MIRACULOUS_CATEGORY);
    public static final Supplier<KeyMapping> SHRINK_SWINGING_ROPE_LENGTH = register("shrink_swinging_rope_length", InputConstants.KEY_UP, MIRACULOUS_CATEGORY);

    public static Supplier<KeyMapping> register(String name, int key, String category) {
        return TommyLibServices.CLIENT.registerKeyMapping(Mineraculous.modLoc(name), key, category);
    }

    public static void init() {}
}
