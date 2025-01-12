package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public record SuitLookData(Optional<BakedGeoModel> model, ResourceLocation texture, List<ResourceLocation> frames) {}
