package dev.thomasglasser.mineraculous.data.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.tags.MineraculousDamageTypeTags;
import dev.thomasglasser.mineraculous.world.damagesource.MineraculousDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MineraculousDamageTypeTagsProvider extends TagsProvider<DamageType> {
    public MineraculousDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, Mineraculous.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS)
                .add(MineraculousDamageTypes.CATACLYSM);

        tag(DamageTypeTags.BYPASSES_ARMOR)
                .add(MineraculousDamageTypes.CATACLYSM);

        tag(DamageTypeTags.NO_KNOCKBACK)
                .add(MineraculousDamageTypes.CATACLYSM);

        tag(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS)
                .add(MineraculousDamageTypes.CATACLYSM);

        tag(DamageTypeTags.BYPASSES_WOLF_ARMOR)
                .add(MineraculousDamageTypes.CATACLYSM);

        tag(DamageTypeTags.IS_PLAYER_ATTACK)
                .add(MineraculousDamageTypes.CATACLYSM);

        tag(DamageTypeTags.PANIC_CAUSES)
                .add(MineraculousDamageTypes.CATACLYSM);

        tag(Tags.DamageTypes.IS_MAGIC)
                .add(MineraculousDamageTypes.CATACLYSM);

        tag(MineraculousDamageTypeTags.IS_CATACLYSM)
                .add(MineraculousDamageTypes.CATACLYSM);

        tag(MineraculousDamageTypeTags.RESISTED_BY_MIRACULOUS)
                .addTag(DamageTypeTags.PANIC_CAUSES);
    }
}
