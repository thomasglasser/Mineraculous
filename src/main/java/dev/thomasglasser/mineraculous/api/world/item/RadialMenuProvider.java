package dev.thomasglasser.mineraculous.api.world.item;

import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuScreen;
import dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A helper for {@link Item}s to configure {@link RadialMenuScreen} behavior
 * and enable {@link MineraculousKeyMappings#OPEN_ITEM_RADIAL_MENU}.
 *
 * @param <T> The {@link RadialMenuOption} the provider is for
 */
public interface RadialMenuProvider<T extends RadialMenuOption> {
    /**
     * Determines if the {@link RadialMenuScreen} can be opened.
     * Only called on the client.
     *
     * @param stack  The {@link ItemStack} being used in the screen
     * @param hand   The {@link InteractionHand} holding the provided item
     * @param holder The {@link Player} holding the provided item
     * @return Whether the {@link RadialMenuScreen} can be opened in this key press
     */
    default boolean canOpenMenu(ItemStack stack, InteractionHand hand, Player holder) {
        return true;
    }

    /**
     * Provides the default color for selected options.
     *
     * @param stack  The {@link ItemStack} being used in the screen
     * @param hand   The {@link InteractionHand} holding the provided item
     * @param holder The {@link Player} holding the provided item
     * @return The default color for selected options
     */
    int getColor(ItemStack stack, InteractionHand hand, Player holder);

    /**
     * Provides the list of {@link RadialMenuOption}s available for the provided {@link ItemStack} and {@link Player} holder.
     *
     * @param stack  The {@link ItemStack} being used in the screen
     * @param hand   The {@link InteractionHand} holding the provided item
     * @param holder The {@link Player} holding the provided item
     * @return The list of options available
     */
    List<T> getOptions(ItemStack stack, InteractionHand hand, Player holder);

    /**
     * Provides a filtered list of {@link RadialMenuOption}s available for the provided {@link ItemStack} and {@link Player} holder based on {@link RadialMenuOption#isEnabled}.
     *
     * @param stack  The {@link ItemStack} being used in the screen
     * @param hand   The {@link InteractionHand} holding the provided item
     * @param holder The {@link Player} holding the provided item
     * @return The filtered list of options available
     */
    default List<T> getEnabledOptions(ItemStack stack, InteractionHand hand, Player holder) {
        List<T> filtered = new ReferenceArrayList<>(getOptions(stack, hand, holder));
        filtered.removeIf(option -> !option.isEnabled(stack, holder));
        return filtered;
    }

    /**
     * Handles the {@link MineraculousKeyMappings#OPEN_ITEM_RADIAL_MENU} key press when the menu cannot be opened.
     *
     * @param stack  The {@link ItemStack} being used in the screen
     * @param hand   The {@link InteractionHand} holding the provided item
     * @param holder The {@link Player} holding the provided item
     * @return Whether the secondary key behavior was successful
     */
    default boolean handleSecondaryKeyBehavior(ItemStack stack, InteractionHand hand, Player holder) {
        return false;
    }

    /**
     * Provides the {@link DataComponentType} to use when setting the provided {@link ItemStack}s option.
     *
     * @param stack  The {@link ItemStack} being used in the screen
     * @param hand   The {@link InteractionHand} holding the provided item
     * @param holder The {@link Player} holding the provided item
     * @return The {@link DataComponentType} to use
     */
    Supplier<DataComponentType<T>> getComponentType(ItemStack stack, InteractionHand hand, Player holder);

    /**
     * Sets the option from the provided index on the provided {@link ItemStack}.
     *
     * @param stack  The {@link ItemStack} being used in the screen
     * @param hand   The {@link InteractionHand} holding the provided item
     * @param holder The {@link Player} holding the provided item
     * @param index  The index of the selected option in {@link RadialMenuProvider#getEnabledOptions}
     * @return The selected {@link RadialMenuOption}
     */
    default T setOption(ItemStack stack, InteractionHand hand, Player holder, int index) {
        T value = getEnabledOptions(stack, hand, holder).get(index);
        stack.set(getComponentType(stack, hand, holder).get(), value);
        return value;
    }
}
