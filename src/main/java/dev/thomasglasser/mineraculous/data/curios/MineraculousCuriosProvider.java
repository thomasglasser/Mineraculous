package dev.thomasglasser.mineraculous.data.curios;

import dev.thomasglasser.mineraculous.Mineraculous;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

public class MineraculousCuriosProvider extends CuriosDataProvider {
    public static final String SLOT_BROOCH = "brooch";
    public static final String SLOT_RING = "ring";
    public static final String SLOT_EARRINGS = "earrings";
    public static final String SLOT_BELT = "belt";

    public MineraculousCuriosProvider(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(Mineraculous.MOD_ID, output, fileHelper, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        createEntities("miraculous_holders")
                .addPlayer()
                .addSlots(
                        SLOT_BROOCH,
                        SLOT_RING,
                        SLOT_EARRINGS);

        createEntities("tool_holders")
                .addPlayer()
                .addSlots(
                        SLOT_BELT);

        createSlot(SLOT_BROOCH)
                .icon(Mineraculous.modLoc("slot/empty_brooch_slot"));
        createSlot(SLOT_EARRINGS)
                .icon(Mineraculous.modLoc("slot/empty_earrings_slot"));
    }
}
