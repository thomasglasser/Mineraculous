package dev.thomasglasser.mineraculous.api.packs;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.packs.PackInfo;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousPacks {
    /// Replaces instances of "Kamiko" with "Akuma"
    public static final PackInfo AKUMATIZATION = create("akumatization", PackType.CLIENT_RESOURCES, PackInfo.BUILT_IN_OPTIONAL);

    private static PackInfo create(String id, PackType type, PackSource source) {
        return PackInfo.create(MineraculousConstants.MOD_ID, id, type, source);
    }

    @ApiStatus.Internal
    public static List<PackInfo> getPacks() {
        return ReferenceArrayList.of(AKUMATIZATION);
    }
}
