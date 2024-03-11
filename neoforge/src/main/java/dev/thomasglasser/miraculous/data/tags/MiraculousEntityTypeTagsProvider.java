package dev.thomasglasser.miraculous.data.tags;

import dev.thomasglasser.miraculous.Miraculous;
import dev.thomasglasser.miraculous.tags.MiraculousEntityTypeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MiraculousEntityTypeTagsProvider extends EntityTypeTagsProvider
{
	public MiraculousEntityTypeTagsProvider(PackOutput p_256095_, CompletableFuture<HolderLookup.Provider> p_256572_, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(p_256095_, p_256572_, Miraculous.MOD_ID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider pProvider)
	{
		tag(MiraculousEntityTypeTags.KWAMIS);
	}
}
