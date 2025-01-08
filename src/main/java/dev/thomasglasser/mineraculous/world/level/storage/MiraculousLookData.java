package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.Optional;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public record MiraculousLookData(Optional<BakedGeoModel> model, ResourceLocation texture, Optional<ItemTransforms> transforms) {}
