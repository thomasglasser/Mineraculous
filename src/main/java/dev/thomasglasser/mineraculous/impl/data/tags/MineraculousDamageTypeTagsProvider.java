package dev.thomasglasser.mineraculous.impl.data.tags;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.tags.MineraculousDamageTypeTags;
import dev.thomasglasser.mineraculous.api.world.damagesource.MineraculousDamageTypes;
import dev.thomasglasser.tommylib.api.data.tags.ExtendedTagsProvider;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MineraculousDamageTypeTagsProvider extends ExtendedTagsProvider<DamageType> {
    public MineraculousDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, MineraculousConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(MineraculousDamageTypeTags.HURTS_KAMIKOS)
                .addTag(MineraculousDamageTypeTags.IS_CATACLYSM)
                .addTag(DamageTypeTags.BYPASSES_INVULNERABILITY);

        tag(MineraculousDamageTypeTags.IS_CATACLYSM)
                .add(MineraculousDamageTypes.CATACLYSM);

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
    }
}
