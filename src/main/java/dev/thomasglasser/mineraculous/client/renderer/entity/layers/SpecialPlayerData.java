package dev.thomasglasser.mineraculous.client.renderer.entity.layers;

import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import dev.thomasglasser.tommylib.api.world.entity.player.SpecialPlayerUtils;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SpecialPlayerData(SnapshotTesterCosmeticOptions choice, boolean displaySnapshot, boolean displayDev, boolean displayLegacyDev) {

    public static final StreamCodec<FriendlyByteBuf, SpecialPlayerData> STREAM_CODEC = StreamCodec.composite(
            NetworkUtils.enumCodec(SnapshotTesterCosmeticOptions.class), SpecialPlayerData::choice,
            ByteBufCodecs.BOOL, SpecialPlayerData::displaySnapshot,
            ByteBufCodecs.BOOL, SpecialPlayerData::displayDev,
            ByteBufCodecs.BOOL, SpecialPlayerData::displayLegacyDev,
            SpecialPlayerData::new);

    public static final String GIST = "thomasglasser/aa8eb933847685b93d8f99a59f07b62e";
    public SpecialPlayerData verify(UUID uuid) {
        Set<String> types = SpecialPlayerUtils.getSpecialTypes(GIST, uuid);
        return new SpecialPlayerData(choice, displaySnapshot && types.contains(SpecialPlayerUtils.SNAPSHOT_TESTER_KEY), displayDev && types.contains(SpecialPlayerUtils.DEV_KEY), displayLegacyDev && types.contains(SpecialPlayerUtils.LEGACY_DEV_KEY));
    }
}
