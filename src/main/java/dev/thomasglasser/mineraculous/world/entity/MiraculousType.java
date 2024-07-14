package dev.thomasglasser.mineraculous.world.entity;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.List;

public enum MiraculousType implements StringRepresentable {
    CAT;

    public static final Codec<MiraculousType> CODEC = StringRepresentable.fromEnum(MiraculousType::values);

    private final List<String> includedLooks;

    MiraculousType()
    {
        this.includedLooks = List.of();
    }

    MiraculousType(List<String> includedLooks)
    {
        this.includedLooks = includedLooks;
    }

    public String getTranslationKey() {
        return "miraculous." + getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }

    @Override
    public String toString() {
        return getSerializedName();
    }

    public List<String> getIncludedLooks()
    {
        return includedLooks;
    }
}
