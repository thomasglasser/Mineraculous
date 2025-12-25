package dev.thomasglasser.mineraculous.impl.data.looks;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.data.look.LookProvider;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.data.MineraculousDataGenerators;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

public class MineraculousLookProvider extends LookProvider {
    public MineraculousLookProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, MineraculousConstants.MOD_ID, MineraculousConstants.MOD_NAME, lookupProvider);
    }

    @Override
    protected void registerLooks(HolderLookup.Provider provider) {
        HolderGetter<Miraculous> miraculouses = provider.lookupOrThrow(MineraculousRegistries.MIRACULOUS);
        HolderGetter<Kamikotization> kamikotizations = provider.lookupOrThrow(MineraculousRegistries.KAMIKOTIZATION);

        // Miraculouses
        miraculousNoAnims(miraculouses.getOrThrow(Miraculouses.LADYBUG), "Ladybug");
        miraculousNoAnims(miraculouses.getOrThrow(Miraculouses.CAT), "Cat Noir");
        miraculousNoAnims(miraculouses.getOrThrow(Miraculouses.BUTTERFLY), "Hawk Moth")
                .asset(LookContexts.HIDDEN_MIRACULOUS, LookAssetTypes.GECKOLIB_MODEL, modString("geo/item/miraculous/butterfly_hidden.geo.json"))
                .asset(LookContexts.HIDDEN_MIRACULOUS, LookAssetTypes.ITEM_TRANSFORMS, modString("models/item/miraculous/butterfly_hidden.json"));

        // Miraculous Tools
        miraculousTool(MineraculousItems.LADYBUG_YOYO.getKey(), "Ladybug")
                .assets(LookContexts.MIRACULOUS_TOOL_BLOCKING, assets()
                        .add(LookAssetTypes.TEXTURE, modString("textures/item/geo/ladybug_yoyo_blocking.png"))
                        .add(LookAssetTypes.GECKOLIB_MODEL, modString("geo/item/ladybug_yoyo_blocking.geo.json"))
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, modString("animations/item/ladybug_yoyo_blocking.animation.json")))
                .asset(LookContexts.MIRACULOUS_TOOL_PHONE, LookAssetTypes.TEXTURE, modString("textures/item/geo/ladybug_yoyo_phone.png"))
                .asset(LookContexts.MIRACULOUS_TOOL_SPYGLASS, LookAssetTypes.TEXTURE, modString("textures/item/geo/ladybug_yoyo_spyglass.png"));
        miraculousTool(MineraculousItems.CAT_STAFF.getKey(), "Cat Noir")
                .asset(LookContexts.MIRACULOUS_TOOL_PHONE, LookAssetTypes.TEXTURE, modString("textures/item/geo/cat_staff_phone.png"));
        miraculousTool(MineraculousItems.BUTTERFLY_CANE.getKey(), "Hawk Moth")
                .asset(LookContexts.MIRACULOUS_TOOL_PHONE, LookAssetTypes.TEXTURE, modString("textures/item/geo/butterfly_cane_phone.png"))
                .asset(LookContexts.MIRACULOUS_TOOL_PHONE, LookAssetTypes.GECKOLIB_MODEL, modString("geo/item/butterfly_cane_phone.geo.json"))
                .asset(LookContexts.MIRACULOUS_TOOL_SPYGLASS, LookAssetTypes.TEXTURE, modString("textures/item/geo/butterfly_cane_spyglass.png"));

        // Kamikotizations
        kamikotizationLook(kamikotizations.getOrThrow(MineraculousDataGenerators.STORMY_KAMIKOTIZATION), "Stormy");
        kamikotizationLookNoAnims(kamikotizations.getOrThrow(MineraculousDataGenerators.CAT_KAMIKOTIZATION), "Kitty");
        kamikotizationLookNoAnims(kamikotizations.getOrThrow(MineraculousDataGenerators.LADYBUG_KAMIKOTIZATION), "Bugaboo");
    }
}
