package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public interface Ability {
    Codec<Ability> DIRECT_CODEC = MineraculousBuiltInRegistries.ABILITY_SERIALIZER.byNameCodec()
            .dispatch(Ability::codec, Function.identity());
    Codec<Holder<Ability>> CODEC = RegistryFixedCodec.create(MineraculousRegistries.ABILITY);

    boolean perform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Context context);

    default void transform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {}

    default void detransform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {}

    default boolean canActivate(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {
        return true;
    }

    default void restore(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {}

    Optional<Holder<SoundEvent>> startSound();

    boolean overrideActive();

    default void playStartSound(ServerLevel level, BlockPos pos) {
        if (startSound().isPresent())
            level.playSound(null, pos, startSound().get().value(), SoundSource.PLAYERS, 1, 1);
    }

    MapCodec<? extends Ability> codec();

    enum Context {
        INTERACT_BLOCK,
        INTERACT_ENTITY,
        INTERACT_ITEM,
        INTERACT_AIR,
        PASSIVE;

        private BlockState state;
        private BlockPos pos;
        private Entity entity;
        private ItemStack stack;

        public BlockState state() {
            return state;
        }

        public BlockPos pos() {
            return pos;
        }

        public Entity entity() {
            return entity;
        }

        public ItemStack stack() {
            return stack;
        }

        public void state(BlockState state) {
            this.state = state;
        }

        public void pos(BlockPos pos) {
            this.pos = pos;
        }

        public void entity(Entity entity) {
            this.entity = entity;
        }

        public void stack(ItemStack stack) {
            this.stack = stack;
        }

        public static Context from(BlockState state, BlockPos pos) {
            if (state.is(Blocks.AIR)) {
                return INTERACT_AIR;
            } else {
                Context context = INTERACT_BLOCK;
                context.state(state);
                context.pos(pos);
                return context;
            }
        }

        public static Context from(Entity entity) {
            Context context = INTERACT_ENTITY;
            context.entity(entity);
            return context;
        }

        public static Context from(ItemStack stack) {
            Context context = INTERACT_ITEM;
            context.stack(stack);
            return context;
        }

        public static Context from(ItemStack stack, LivingEntity entity) {
            Context context = INTERACT_ITEM;
            context.stack(stack);
            context.entity(entity);
            return context;
        }

        public static Context from() {
            return INTERACT_AIR;
        }
    }
}
