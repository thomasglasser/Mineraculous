package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface Ability {
    Codec<Ability> DIRECT_CODEC = MineraculousBuiltInRegistries.ABILITY_SERIALIZER.byNameCodec()
            .dispatch(Ability::codec, Function.identity());
    Codec<Holder<Ability>> CODEC = RegistryFileCodec.create(MineraculousRegistries.ABILITY, DIRECT_CODEC);

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

    static List<Ability> getAll(Ability ability) {
        List<Ability> abilities = new ReferenceArrayList<>();
        abilities.add(ability);
        if (ability instanceof HasSubAbility hasSubAbility) {
            abilities.addAll(hasSubAbility.getAll());
        }
        return abilities;
    }

    static List<Ability> getMatching(Predicate<Ability> predicate, Ability ability) {
        ArrayList<Ability> abilities = new ArrayList<>();
        if (predicate.test(ability))
            abilities.add(ability);
        if (ability instanceof HasSubAbility hasSubAbility)
            abilities.addAll(hasSubAbility.getMatching(predicate));
        return abilities;
    }

    static @Nullable Ability getFirstMatching(Predicate<Ability> predicate, Ability ability) {
        return getMatching(predicate, ability).stream().findFirst().orElse(null);
    }

    static boolean hasMatching(Predicate<Ability> predicate, Ability ability) {
        return !getMatching(predicate, ability).isEmpty();
    }

    static List<Ability> getAll(Optional<Holder<Ability>> activeAbility, List<Holder<Ability>> passiveAbilities, boolean includeActive) {
        List<Ability> abilities = new ReferenceArrayList<>();
        if (includeActive) {
            activeAbility.ifPresent(ability -> abilities.add(ability.value()));
        }
        for (Holder<Ability> ability : passiveAbilities) {
            abilities.addAll(getAll(ability.value()));
        }
        return abilities;
    }

    static List<Ability> getMatching(Predicate<Ability> predicate, Optional<Holder<Ability>> activeAbility, List<Holder<Ability>> passiveAbilities, boolean includeActive) {
        ArrayList<Ability> abilities = new ArrayList<>();
        if (activeAbility.isPresent() && includeActive) {
            abilities.addAll(getMatching(predicate, activeAbility.get().value()));
        }
        for (Holder<Ability> ability : passiveAbilities) {
            abilities.addAll(getMatching(predicate, ability.value()));
        }
        return abilities;
    }

    static @Nullable Ability getFirstMatching(Predicate<Ability> predicate, Optional<Holder<Ability>> activeAbility, List<Holder<Ability>> passiveAbilities, boolean includeActive) {
        return getMatching(predicate, activeAbility, passiveAbilities, includeActive).stream().findFirst().orElse(null);
    }

    static boolean hasMatching(Predicate<Ability> predicate, Optional<Holder<Ability>> activeAbility, List<Holder<Ability>> passiveAbilities, boolean includeActive) {
        return !getMatching(predicate, activeAbility, passiveAbilities, includeActive).isEmpty();
    }

    static List<Ability> getAll(Miraculous miraculous, boolean includeActive) {
        return getAll(miraculous.activeAbility(), miraculous.passiveAbilities(), includeActive);
    }

    static List<Ability> getMatching(Predicate<Ability> predicate, Miraculous miraculous, boolean includeActive) {
        return getMatching(predicate, miraculous.activeAbility(), miraculous.passiveAbilities(), includeActive);
    }

    static @Nullable Ability getFirstMatching(Predicate<Ability> predicate, Miraculous miraculous, boolean includeActive) {
        return getFirstMatching(predicate, miraculous.activeAbility(), miraculous.passiveAbilities(), includeActive);
    }

    static boolean hasMatching(Predicate<Ability> predicate, Miraculous miraculous, boolean includeActive) {
        return hasMatching(predicate, miraculous.activeAbility(), miraculous.passiveAbilities(), includeActive);
    }

    static List<Ability> getAll(Kamikotization kamikotization, boolean includeActive) {
        return getAll(kamikotization.powerSource().right(), kamikotization.passiveAbilities(), includeActive);
    }

    static List<Ability> getMatching(Predicate<Ability> predicate, Kamikotization kamikotization, boolean includeActive) {
        return getMatching(predicate, kamikotization.powerSource().right(), kamikotization.passiveAbilities(), includeActive);
    }

    static @Nullable Ability getFirstMatching(Predicate<Ability> predicate, Kamikotization kamikotization, boolean includeActive) {
        return getFirstMatching(predicate, kamikotization.powerSource().right(), kamikotization.passiveAbilities(), includeActive);
    }

    static boolean hasMatching(Predicate<Ability> predicate, Kamikotization kamikotization, boolean includeActive) {
        return hasMatching(predicate, kamikotization.powerSource().right(), kamikotization.passiveAbilities(), includeActive);
    }

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
