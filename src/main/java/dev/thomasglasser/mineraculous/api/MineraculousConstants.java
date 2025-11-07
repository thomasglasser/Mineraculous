package dev.thomasglasser.mineraculous.api;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MineraculousConstants {
    public static final String MOD_ID = "mineraculous";
    public static final String MOD_NAME = "Mineraculous";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final ResourceLocation EMPTY_TEXTURE = modLoc("textures/misc/empty.png");

    public static final int API_VERSION = 1;

    public static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MineraculousConstants.MOD_ID, path);
    }

    public static String toLanguageKey(ResourceKey<?> key) {
        return key.location().toLanguageKey(key.registry().getPath());
    }

    public enum Dependencies {
        CURIOS("curios"),
        MODONOMICON("modonomicon"),
        TOMMYTECH("tommytech");

        private String id;

        Dependencies(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public ResourceLocation modLoc(String s) {
            return ResourceLocation.fromNamespaceAndPath(getId(), s);
        }

        public boolean isLoaded() {
            return ModList.get().isLoaded(getId());
        }
    }
}
