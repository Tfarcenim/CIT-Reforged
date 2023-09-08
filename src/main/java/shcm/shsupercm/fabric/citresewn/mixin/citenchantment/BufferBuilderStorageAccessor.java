package shcm.shsupercm.fabric.citresewn.mixin.citenchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.SortedMap;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;

@Mixin(RenderBuffers.class)
public interface BufferBuilderStorageAccessor {
    @Accessor("fixedBuffers")
    SortedMap<RenderType, BufferBuilder> entityBuilders();
}
