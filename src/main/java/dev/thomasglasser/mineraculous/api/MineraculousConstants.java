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

    /// The JSON API version of the mod, used in {@link dev.thomasglasser.mineraculous.api.server.packs.metadata.MineraculousMetadataFormatSection}.
    public static final int API_VERSION = 1;

    /**
     * Creates a {@link ResourceLocation} with the {@link MineraculousConstants#MOD_ID} namespace.
     *
     * @param path The path of the resource location
     * @return The mod resource location
     */
    public static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MineraculousConstants.MOD_ID, path);
    }

    /**
     * Converts a {@link ResourceKey} to a language key.
     *
     * @param key The key to convert
     * @return The language key
     */
    public static String toLanguageKey(ResourceKey<?> key) {
        return key.location().toLanguageKey(key.registry().getPath());
    }

    /// Mod dependencies used in code
    public enum Dependencies {
        /// Used for item tags
        CURIOS("curios"),
        /// Used for the in-game wiki
        MODONOMICON("modonomicon"),
        /// Used for tool phone modes
        TOMMYTECH("tommytech");

        private String modId;

        Dependencies(String modId) {
            this.modId = modId;
        }

        /**
         * Returns the mod ID of the dependency.
         *
         * @return The mod ID of the dependency
         */
        public String getModId() {
            return modId;
        }

        /**
         * Creates a {@link ResourceLocation} with the mod ID as namespace.
         *
         * @param path The path of the resource location
         * @return The mod resource location
         */
        public ResourceLocation modLoc(String path) {
            return ResourceLocation.fromNamespaceAndPath(getModId(), path);
        }

        /**
         * Determines if the dependency is loaded.
         *
         * @return Whether the dependency is loaded
         */
        public boolean isLoaded() {
            return ModList.get().isLoaded(getModId());
        }
    }
}
