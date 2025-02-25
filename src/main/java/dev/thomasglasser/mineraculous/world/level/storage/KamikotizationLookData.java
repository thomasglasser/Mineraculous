package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.object.BakedAnimations;

public record KamikotizationLookData(Optional<BakedGeoModel> model, ResourceLocation texture, Optional<byte[]> glowmask, Optional<BakedAnimations> animations) {}
