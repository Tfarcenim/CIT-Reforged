package shcm.shsupercm.forge.citresewn.mixin.cititem;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockModel.class)
public interface JsonUnbakedModelAccessor {
    @Accessor
    void setParentLocation(ResourceLocation parentId);
}
