package dev.thomasglasser.mineraculous.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;

public class MineraculousPoiTypes {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Mineraculous.MOD_ID);

    public static final DeferredHolder<PoiType, PoiType> FROMAGER = registerForVillager("fromager", MineraculousBlocks.CHEESE_POT);

    private static Supplier<Set<BlockState>> getBlockStates(DeferredBlock<?> block) {
        return () -> ImmutableSet.copyOf(block.get().getStateDefinition().getPossibleStates());
    }

    private static DeferredHolder<PoiType, PoiType> registerForVillager(String name, DeferredBlock<?> block) {
        return register(name, getBlockStates(block), 1, 1);
    }

    private static DeferredHolder<PoiType, PoiType> register(String name, Supplier<Set<BlockState>> blockStates, int maxTickets, int validRange) {
        return POI_TYPES.register(name, () -> new PoiType(blockStates.get(), maxTickets, validRange));
    }

    public static void init() {}
}
