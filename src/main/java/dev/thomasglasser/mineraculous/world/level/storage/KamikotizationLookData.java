package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public record KamikotizationLookData(Optional<BakedGeoModel> model, ResourceLocation texture, Optional<byte[]> glowmask) {}
