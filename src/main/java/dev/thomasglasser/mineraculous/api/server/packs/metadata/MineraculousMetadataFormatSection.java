package dev.thomasglasser.mineraculous.api.server.packs.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import java.util.Optional;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.InclusiveRange;

public record MineraculousMetadataFormatSection(int apiFormat, Optional<InclusiveRange<Integer>> supportedApiFormats) {
    public static final Codec<MineraculousMetadataFormatSection> CODEC = RecordCodecBuilder.create(
            p_337567_ -> p_337567_.group(
                    Codec.INT.fieldOf("api_format").forGetter(MineraculousMetadataFormatSection::apiFormat),
                    InclusiveRange.codec(Codec.INT).optionalFieldOf("supported_api_formats").forGetter(MineraculousMetadataFormatSection::supportedApiFormats))
                    .apply(p_337567_, MineraculousMetadataFormatSection::new));
    public static final MetadataSectionType<MineraculousMetadataFormatSection> TYPE = MetadataSectionType.fromCodec("mineraculous", CODEC);

    public static MineraculousMetadataFormatSection createBuiltIn() {
        return new MineraculousMetadataFormatSection(MineraculousConstants.API_VERSION, Optional.empty());
    }

    public static InclusiveRange<Integer> getDeclaredApiVersions(String id, MineraculousMetadataFormatSection metadata) {
        int i = metadata.apiFormat();
        if (metadata.supportedApiFormats().isEmpty()) {
            return new InclusiveRange<>(i);
        } else {
            InclusiveRange<Integer> inclusiverange = metadata.supportedApiFormats().get();
            if (!inclusiverange.isValueInRange(i)) {
                MineraculousConstants.LOGGER.warn("Pack {} declared support for Mineraculous API versions {} but declared API format is {}, defaulting to {}", id, inclusiverange, i, i);
                return new InclusiveRange<>(i);
            } else {
                return inclusiverange;
            }
        }
    }
}
