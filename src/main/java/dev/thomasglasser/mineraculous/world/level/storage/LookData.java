package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public record LookData(BakedGeoModel model, ResourceLocation texture, List<ResourceLocation> frames) {}
