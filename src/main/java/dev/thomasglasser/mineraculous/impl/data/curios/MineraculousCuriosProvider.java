package dev.thomasglasser.mineraculous.impl.data.curios;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosDataProvider;

public class MineraculousCuriosProvider extends CuriosDataProvider {
    public static final String SLOT_BROOCH = "brooch";
    public static final String SLOT_RING = "ring";
    public static final String SLOT_EARRINGS = "earrings";
    public static final String SLOT_BELT = "belt";
    public static final String SLOT_BACK = "back";
    public static final String SLOT_POCKET = "pocket";

    public MineraculousCuriosProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, @Nullable ExistingFileHelper fileHelper) {
        super(Mineraculous.MOD_ID, output, fileHelper, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        createEntities("miraculous_holders")
                .addPlayer()
                .addSlots(
                        SLOT_BROOCH,
                        SLOT_RING,
                        SLOT_POCKET,
                        SLOT_EARRINGS);

        createEntities("miraculous_tool_holders")
                .addPlayer()
                .addSlots(
                        SLOT_BELT,
                        SLOT_BACK);

        createSlot(SLOT_BROOCH)
                .icon(slotIcon(SLOT_BROOCH));
        createSlot(SLOT_EARRINGS)
                .icon(slotIcon(SLOT_EARRINGS));
        createSlot(SLOT_POCKET)
                .icon(slotIcon(SLOT_BROOCH));
        createSlot(SLOT_BACK)
                .icon(slotIcon(SLOT_BROOCH));
    }

    private ResourceLocation slotIcon(String name) {
        return Mineraculous.modLoc("slot/empty_" + name + "_slot");
    }
}
