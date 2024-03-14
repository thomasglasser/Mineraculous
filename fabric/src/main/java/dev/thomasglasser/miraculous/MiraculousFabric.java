package dev.thomasglasser.miraculous;

import dev.thomasglasser.miraculous.world.entity.MiraculousEntityTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class MiraculousFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Miraculous.init();

        registerEntityAttributes();
    }

    private void registerEntityAttributes()
    {
        for (EntityType<? extends LivingEntity> type : MiraculousEntityTypes.getAllAttributes().keySet())
        {
            FabricDefaultAttributeRegistry.register(type, MiraculousEntityTypes.getAllAttributes().get(type));
        }
    }
}