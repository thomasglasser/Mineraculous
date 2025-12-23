package dev.thomasglasser.mineraculous.impl.data.looks;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.data.look.DefaultLookProvider;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.data.MineraculousDataGenerators;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

public class MineraculousDefaultLookProvider extends DefaultLookProvider {
    public MineraculousDefaultLookProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, MineraculousConstants.MOD_ID, lookupProvider);
    }

    @Override
    protected void registerLooks(HolderLookup.Provider provider) {
        HolderGetter<Miraculous> miraculouses = provider.lookupOrThrow(MineraculousRegistries.MIRACULOUS);

        // Miraculouses
        miraculousNoAnims(miraculouses.getOrThrow(Miraculouses.LADYBUG));
        miraculousNoAnims(miraculouses.getOrThrow(Miraculouses.CAT));
        miraculousNoAnims(miraculouses.getOrThrow(Miraculouses.BUTTERFLY))
                .add(LookContexts.HIDDEN_MIRACULOUS, LookAssetTypes.GECKOLIB_MODEL, modString("geo/item/miraculous/butterfly_hidden.geo.json"))
                .add(LookContexts.HIDDEN_MIRACULOUS, LookAssetTypes.ITEM_TRANSFORMS, modString("models/item/miraculous/butterfly_hidden.json"));

        // Miraculous Tools
        miraculousTool(MineraculousItems.LADYBUG_YOYO.getKey())
                .add(LookContexts.MIRACULOUS_TOOL_BLOCKING, assets()
                        .add(LookAssetTypes.TEXTURE, modString("textures/item/geo/ladybug_yoyo_blocking.png"))
                        .add(LookAssetTypes.GECKOLIB_MODEL, modString("geo/item/ladybug_yoyo_blocking.geo.json"))
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, modString("animations/item/ladybug_yoyo_blocking.animation.json")))
                .add(LookContexts.MIRACULOUS_TOOL_PHONE, LookAssetTypes.TEXTURE, modString("textures/item/geo/ladybug_yoyo_phone.png"))
                .add(LookContexts.MIRACULOUS_TOOL_SPYGLASS, LookAssetTypes.TEXTURE, modString("textures/item/geo/ladybug_yoyo_spyglass.png"));
        miraculousTool(MineraculousItems.CAT_STAFF.getKey())
                .add(LookContexts.MIRACULOUS_TOOL_PHONE, LookAssetTypes.TEXTURE, modString("textures/item/geo/cat_staff_phone.png"));
        miraculousTool(MineraculousItems.BUTTERFLY_CANE.getKey())
                .add(LookContexts.MIRACULOUS_TOOL_PHONE, LookAssetTypes.TEXTURE, modString("textures/item/geo/butterfly_cane_phone.png"))
                .add(LookContexts.MIRACULOUS_TOOL_PHONE, LookAssetTypes.GECKOLIB_MODEL, modString("geo/item/butterfly_cane_phone.geo.json"))
                .add(LookContexts.MIRACULOUS_TOOL_SPYGLASS, LookAssetTypes.TEXTURE, modString("textures/item/geo/butterfly_cane_spyglass.png"));

        // Kamikotizations
        kamikotizationLook(MineraculousDataGenerators.STORMY_KAMIKOTIZATION);
        kamikotizationLookNoAnims(MineraculousDataGenerators.CAT_KAMIKOTIZATION);
        kamikotizationLookNoAnims(MineraculousDataGenerators.LADYBUG_KAMIKOTIZATION);
    }
}
