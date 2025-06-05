package dev.thomasglasser.mineraculous.client.renderer.entity.layers;

import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import dev.thomasglasser.tommylib.api.world.entity.player.SpecialPlayerUtils;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SpecialPlayerData(BetaTesterCosmeticOptions choice, boolean displayBeta, boolean displayDev, boolean displayLegacyDev) {

    public static final StreamCodec<FriendlyByteBuf, SpecialPlayerData> STREAM_CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.forEnum(BetaTesterCosmeticOptions.class), SpecialPlayerData::choice,
            ByteBufCodecs.BOOL, SpecialPlayerData::displayBeta,
            ByteBufCodecs.BOOL, SpecialPlayerData::displayDev,
            ByteBufCodecs.BOOL, SpecialPlayerData::displayLegacyDev,
            SpecialPlayerData::new);

    public static final String GIST = "thomasglasser/aa8eb933847685b93d8f99a59f07b62e";
    public SpecialPlayerData verify(UUID uuid) {
        Set<String> types = SpecialPlayerUtils.getSpecialTypes(GIST, uuid);
        return new SpecialPlayerData(choice, displayBeta && types.contains(SpecialPlayerUtils.BETA_TESTER_KEY), displayDev && types.contains(SpecialPlayerUtils.DEV_KEY), displayLegacyDev && types.contains(SpecialPlayerUtils.LEGACY_DEV_KEY));
    }
}
