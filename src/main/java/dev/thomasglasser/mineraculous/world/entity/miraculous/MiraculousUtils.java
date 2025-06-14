package dev.thomasglasser.mineraculous.world.entity.miraculous;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.entity.Kwami;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

public class MiraculousUtils {
    public static Optional<KwamiData> renounce(ItemStack stack, ServerLevel level, Optional<KwamiData> kwamiData) {
        stack.set(MineraculousDataComponents.POWERED, Unit.INSTANCE);
        stack.remove(MineraculousDataComponents.REMAINING_TICKS);
        if (kwamiData.isPresent() && level.getEntity(kwamiData.get().uuid()) instanceof Kwami kwami) {
            KwamiData newData = new KwamiData(kwami.getUUID(), kwami.getId(), kwamiData.get().charged());
            stack.set(MineraculousDataComponents.KWAMI_DATA, newData);
            kwami.discard();
            return Optional.of(newData);
        }
        return Optional.empty();
    }
}
