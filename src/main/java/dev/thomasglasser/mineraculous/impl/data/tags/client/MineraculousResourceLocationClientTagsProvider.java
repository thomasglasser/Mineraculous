package dev.thomasglasser.mineraculous.impl.data.tags.client;

import dev.dhyces.trimmed.api.client.ClientKeyResolvers;
import dev.dhyces.trimmed.api.client.tag.ClientTags;
import dev.dhyces.trimmed.api.data.tag.ClientTagDataProvider;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.item.armortrim.MineraculousTrimPatterns;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MineraculousResourceLocationClientTagsProvider extends ClientTagDataProvider<ResourceLocation> {
    public MineraculousResourceLocationClientTagsProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, Mineraculous.MOD_ID, ClientKeyResolvers.TEXTURE, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(ClientTags.TRIM_PATTERN_TEXTURES)
                .add(trimPatternTextures(MineraculousTrimPatterns.LADYBUG))
                .add(trimPatternTextures(MineraculousTrimPatterns.CAT))
                .add(trimPatternTextures(MineraculousTrimPatterns.BUTTERFLY));
    }

    protected ResourceLocation[] trimPatternTextures(ResourceKey<TrimPattern> pattern) {
        ResourceLocation[] textures = new ResourceLocation[2];
        textures[0] = pattern.location().withPrefix("trims/models/armor/");
        textures[1] = pattern.location().withPrefix("trims/models/armor/").withSuffix("_leggings");
        return textures;
    }
}
