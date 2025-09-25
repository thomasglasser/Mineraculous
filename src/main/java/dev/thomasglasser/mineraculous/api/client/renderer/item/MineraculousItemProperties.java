package dev.thomasglasser.mineraculous.api.client.renderer.item;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.level.block.PieceBlock;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import java.util.UUID;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.BlockItemStateProperties;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousItemProperties {
    // Vanilla
    /**
     * Whether the stack is being used for blocking
     * based on {@link LivingEntity#isBlocking()} and {@link LivingEntity#getUseItem()}.
     */
    public static final ResourceLocation BLOCKING = ResourceLocation.withDefaultNamespace("blocking");

    // Generic
    /// Whether {@link MineraculousDataComponents#ACTIVE} is present and {@code true}.
    public static final ResourceLocation ACTIVE = MineraculousConstants.modLoc("active");
    /// The value of a {@link PieceBlock} {@link BlockItem} corresponding to {@link PieceBlock#getMissingPiecesProperty()}.
    public static final ResourceLocation MISSING_PIECES = MineraculousConstants.modLoc(PieceBlock.MISSING_PIECES);

    // Specific
    /// {@link Enum#ordinal()} {@code + 1} of a Miraculous tool ability.
    public static final ResourceLocation ABILITY = MineraculousConstants.modLoc("ability");
    /// Thrown state of a {@link LadybugYoyoItem}.
    public static final ResourceLocation THROWN = MineraculousConstants.modLoc("thrown");
    /// Whether a {@link ButterflyCaneItem} owner is currently storing a kamiko
    public static final ResourceLocation STORING = MineraculousConstants.modLoc("storing");

    /**
     * Creates an {@link ItemPropertyFunction} for an {@link Enum} {@link DataComponentType} based on ordinal.
     * 
     * @param component The {@link DataComponentType} to use
     * @return The created {@link ItemPropertyFunction}
     * @param <T> The {@link Enum} to use
     */
    public static <T extends Enum<T>> ItemPropertyFunction getEnumPropertyFunction(DataComponentType<T> component) {
        return (stack, level, entity, seed) -> {
            T value = stack.get(component);
            if (value == null)
                return 0;
            return getPropertyForAbility(value);
        };
    }

    /**
     * Returns the property value for the passed {@link Enum} based on ordinal.
     * 
     * @param value The {@link Enum} value
     * @return The property value
     */
    public static int getPropertyForAbility(Enum<?> value) {
        return value.ordinal() + 1;
    }

    @ApiStatus.Internal
    public static void init() {
        // Generic
        ItemProperties.registerGeneric(BLOCKING, (stack, level, entity, seed) -> stack.has(MineraculousDataComponents.BLOCKING) ? 1 : 0);
        ItemProperties.registerGeneric(ACTIVE, (stack, level, entity, seed) -> Active.isActive(stack) ? 1 : 0);
        ItemProperties.registerGeneric(MISSING_PIECES, (stack, level, entity, seed) -> {
            BlockItemStateProperties blockItemStateProperties = stack.get(DataComponents.BLOCK_STATE);
            if (blockItemStateProperties != null && stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof PieceBlock pieceBlock) {
                Integer missing = blockItemStateProperties.get(pieceBlock.getMissingPiecesProperty());
                return missing == null ? 0 : missing;
            }
            return 0;
        });

        // Miraculous Tools
        ItemProperties.register(MineraculousItems.LADYBUG_YOYO.get(), ABILITY, getEnumPropertyFunction(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get()));
        ItemProperties.register(MineraculousItems.LADYBUG_YOYO.get(), THROWN, (stack, level, entity, seed) -> {
            if (entity instanceof Player player) {
                ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
                if (data.id().isPresent()) {
                    ThrownLadybugYoyo yoyo = data.getThrownYoyo(player.level());
                    return (yoyo != null && yoyo.inGround()) ? 2 : 1;
                } else if (player.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).isPresent()) {
                    return 2;
                }
            }
            return 0;
        });
        ItemProperties.register(MineraculousItems.CAT_STAFF.get(), ABILITY, getEnumPropertyFunction(MineraculousDataComponents.CAT_STAFF_ABILITY.get()));
        ItemProperties.register(MineraculousItems.BUTTERFLY_CANE.get(), ABILITY, getEnumPropertyFunction(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get()));
        ItemProperties.register(MineraculousItems.BUTTERFLY_CANE.get(), STORING, (stack, level, entity, seed) -> {
            UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
            if (ownerId != null && level != null) {
                Entity owner = level.getEntities().get(ownerId);
                if (owner != null) {
                    if (owner.getData(MineraculousAttachmentTypes.MIRACULOUSES).hasStoredEntities(MiraculousTags.CAN_USE_BUTTERFLY_CANE)) {
                        return 1;
                    }
                }
            }
            return 0;
        });
    }
}
