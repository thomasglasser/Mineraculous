package dev.thomasglasser.mineraculous.api.packs;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.packs.PackInfo;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;

public class MineraculousPacks {
    public static final PackInfo AKUMATIZATION = create("akumatization", PackType.CLIENT_RESOURCES, PackInfo.BUILT_IN_OPTIONAL);

    private static PackInfo create(String id, PackType type, PackSource source) {
        return PackInfo.create(Mineraculous.MOD_ID, id, type, source);
    }

    public static List<PackInfo> getPacks() {
        return ReferenceArrayList.of(AKUMATIZATION);
    }
}
