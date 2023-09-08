package shcm.shsupercm.fabric.citresewn.mixin.core;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

@Mixin(CompoundTag.class)
public interface NbtCompoundAccessor {
    @Accessor
    Map<String, Tag> getTags();
}
