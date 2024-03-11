package dev.thomasglasser.miraculous;

import net.fabricmc.api.ModInitializer;

public class MiraculousFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Miraculous.init();
    }
}