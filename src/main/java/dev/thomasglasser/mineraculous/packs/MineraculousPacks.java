package dev.thomasglasser.mineraculous.packs;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.packs.PackInfo;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.List;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.KnownPack;

public class MineraculousPacks {
    public static final PackInfo AKUMATIZATION = new PackInfo(new KnownPack(Mineraculous.MOD_ID, "akumatization", TommyLibServices.PLATFORM.getModVersion(Mineraculous.MOD_ID)), PackType.CLIENT_RESOURCES, PackInfo.BUILT_IN_OPTIONAL);

    public static List<PackInfo> getPacks() {
        return List.of(
                AKUMATIZATION);
    }
}
