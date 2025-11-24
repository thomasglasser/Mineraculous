package dev.thomasglasser.mineraculous.impl.mixin.minecraft.server.packs.repository;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.server.packs.metadata.MineraculousMetadataFormatSection;
import dev.thomasglasser.mineraculous.impl.server.packs.repository.MineraculousPackCompatabilityHolder;
import dev.thomasglasser.mineraculous.impl.server.packs.repository.MineraculousPackCompatibility;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Pack.class)
public abstract class PackMixin {
    @WrapOperation(method = "readPackMetadata", at = @At(value = "NEW", target = "Lnet/minecraft/server/packs/repository/Pack$Metadata;"))
    private static Pack.Metadata checkForApiCompatibility(Component description, PackCompatibility compatibility, FeatureFlagSet requestedFeatures, List<String> overlays, boolean isHidden, Operation<Pack.Metadata> original, @Local PackResources packResources, @Local(argsOnly = true) PackLocationInfo info) throws IOException {
        Pack.Metadata metadata = original.call(description, compatibility, requestedFeatures, overlays, isHidden);
        MineraculousMetadataFormatSection mineraculousMetadata = packResources.getMetadataSection(MineraculousMetadataFormatSection.TYPE);
        if (mineraculousMetadata != null) {
            InclusiveRange<Integer> declaredApiVersions = MineraculousMetadataFormatSection.getDeclaredApiVersions(info.id(), mineraculousMetadata);
            MineraculousPackCompatibility mineraculousCompatibility = MineraculousPackCompatibility.forVersion(declaredApiVersions, MineraculousConstants.API_VERSION);
            ((MineraculousPackCompatabilityHolder) (Object) metadata).mineraculous$setPackCompatibility(mineraculousCompatibility);
        }
        return metadata;
    }
}
