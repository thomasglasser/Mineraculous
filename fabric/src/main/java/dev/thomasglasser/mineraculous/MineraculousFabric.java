package dev.thomasglasser.mineraculous;

import dev.thomasglasser.mineraculous.network.MineraculousPayloads;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.tommylib.api.network.FabricNetworkUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class MineraculousFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Mineraculous.init();

        registerEntityAttributes();

        MineraculousPayloads.PAYLOADS.forEach(FabricNetworkUtils::register);

        registerTrinkets();

        registerEvents();
    }

    private void registerEntityAttributes()
    {
        for (EntityType<? extends LivingEntity> type : MineraculousEntityTypes.getAllAttributes().keySet())
        {
            FabricDefaultAttributeRegistry.register(type, MineraculousEntityTypes.getAllAttributes().get(type));
        }
    }

    private void registerTrinkets()
    {
        // TODO: Update Trinkets
//        TrinketsApi.registerTrinket(MineraculousItems.CAT_MIRACULOUS.get(), new Trinket() {
//            private final MiraculousItemCurio curio = new CatMiraculousItemCurio();
//
//            @Override
//            public void tick(ItemStack stack, SlotReference slot, LivingEntity entity)
//            {
//                curio.tick(stack, new CuriosData(slot.index(), slot.inventory().getSlotType().getGroup(), slot.inventory().getSlotType().getName()), entity);
//            }
//
//            @Override
//            public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity)
//            {
//                curio.onEquip(stack, new CuriosData(slot.index(), slot.inventory().getSlotType().getGroup(), slot.inventory().getSlotType().getName()), entity);
//            }
//
//            @Override
//            public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity)
//            {
//                curio.onUnequip(stack, slot.inventory().getItem(slot.index()), new CuriosData(slot.index(), slot.inventory().getSlotType().getGroup(), slot.inventory().getSlotType().getName()), entity);
//            }
//        });
    }

    private void registerEvents()
    {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> MineraculousEntityEvents.onEntityInteract(player, entity, hand));
        UseBlockCallback.EVENT.register((player, world, hand, blockHitResult) -> MineraculousEntityEvents.onBlockInteract(player, blockHitResult, hand));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> MineraculousEntityEvents.onAttackEntity(player, entity));
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> MineraculousEntityEvents.onBlockLeftClick(player, pos, hand));
    }
}