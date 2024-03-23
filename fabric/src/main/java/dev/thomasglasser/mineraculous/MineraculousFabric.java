package dev.thomasglasser.mineraculous;

import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class MineraculousFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Mineraculous.init();

        registerEntityAttributes();
    }

    private void registerEntityAttributes()
    {
        for (EntityType<? extends LivingEntity> type : MineraculousEntityTypes.getAllAttributes().keySet())
        {
            FabricDefaultAttributeRegistry.register(type, MineraculousEntityTypes.getAllAttributes().get(type));
        }
    }
}