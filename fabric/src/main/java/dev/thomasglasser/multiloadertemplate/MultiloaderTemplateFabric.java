package dev.thomasglasser.multiloadertemplate;

import net.fabricmc.api.ModInitializer;

public class MultiloaderTemplateFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        MultiloaderTemplate.init();
    }
}