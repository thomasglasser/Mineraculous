package dev.thomasglasser.mineraculous;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketsApi;
import dev.thomasglasser.mineraculous.network.MineraculousPackets;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.curio.CatMiraculousItemCurio;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.MiraculousItemCurio;
import dev.thomasglasser.tommylib.api.network.FabricPacketUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class MineraculousFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Mineraculous.init();

        registerEntityAttributes();

        MineraculousPackets.PACKETS.forEach(FabricPacketUtils::register);

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
        TrinketsApi.registerTrinket(MineraculousItems.CAT_MIRACULOUS.get(), new Trinket() {
            private final MiraculousItemCurio curio = new CatMiraculousItemCurio();

            @Override
            public void tick(ItemStack stack, SlotReference slot, LivingEntity entity)
            {
                curio.tick(stack, new CuriosData(slot.index(), slot.inventory().getSlotType().getGroup(), slot.inventory().getSlotType().getName()), entity);
            }

            @Override
            public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity)
            {
                curio.onEquip(stack, new CuriosData(slot.index(), slot.inventory().getSlotType().getGroup(), slot.inventory().getSlotType().getName()), entity);
            }
        });
    }

    private void registerEvents()
    {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> MineraculousEntityEvents.onEntityInteract(player, entity, hand));
        UseBlockCallback.EVENT.register((player, world, hand, blockHitResult) -> MineraculousEntityEvents.onBlockInteract(player, blockHitResult, hand));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> MineraculousEntityEvents.onAttackEntity(player, entity));
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> MineraculousEntityEvents.onBlockLeftClick(player, pos, hand));
    }
}