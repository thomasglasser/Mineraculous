package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.object.BakedAnimations;

public record SuitLookData(Optional<BakedGeoModel> model, ResourceLocation texture, Optional<byte[]> glowmask, List<ResourceLocation> frames, List<byte[]> glowmaskFrames, Optional<BakedAnimations> animations) {}
