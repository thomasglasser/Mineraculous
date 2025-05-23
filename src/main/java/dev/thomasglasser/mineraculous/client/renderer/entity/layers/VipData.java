package dev.thomasglasser.mineraculous.client.renderer.entity.layers;

import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import dev.thomasglasser.tommylib.api.world.entity.player.SpecialPlayerUtils;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record VipData(SnapshotTesterCosmeticOptions choice, boolean displaySnapshot, boolean displayDev, boolean displayLegacyDev) {

    public static final StreamCodec<FriendlyByteBuf, VipData> STREAM_CODEC = StreamCodec.composite(
            NetworkUtils.enumCodec(SnapshotTesterCosmeticOptions.class), VipData::choice,
            ByteBufCodecs.BOOL, VipData::displaySnapshot,
            ByteBufCodecs.BOOL, VipData::displayDev,
            ByteBufCodecs.BOOL, VipData::displayLegacyDev,
            VipData::new);

    public static final String GIST = "thomasglasser/aa8eb933847685b93d8f99a59f07b62e";
    public VipData verify(UUID uuid) {
        Set<String> types = SpecialPlayerUtils.getSpecialTypes(GIST, uuid);
        return new VipData(choice, displaySnapshot && types.contains(SpecialPlayerUtils.SNAPSHOT_TESTER_KEY), displayDev && types.contains(SpecialPlayerUtils.DEV_KEY), displayLegacyDev && types.contains(SpecialPlayerUtils.LEGACY_DEV_KEY));
    }
}
