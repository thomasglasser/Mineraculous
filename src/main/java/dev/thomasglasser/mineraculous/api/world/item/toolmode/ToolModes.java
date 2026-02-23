package dev.thomasglasser.mineraculous.api.world.item.toolmode;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;

public class ToolModes {
    // Common
    public static final ToolMode BLOCK = get("block");
    public static final ToolMode PHONE = get("phone");
    public static final ToolMode SPYGLASS = get("spyglass");

    // Specific
    public static final ToolMode BLADE = get("blade");
    public static final ToolMode KAMIKO_STORE = get("kamiko_store");
    public static final ToolMode LASSO = get("lasso");
    public static final ToolMode PERCH = get("perch");
    public static final ToolMode PURIFY = get("purify");
    public static final ToolMode THROW = get("throw");
    public static final ToolMode TRAVEL = get("travel");

    private static ToolMode get(String name) {
        return ToolMode.get(MineraculousConstants.modLoc(name));
    }
}
