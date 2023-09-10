package shcm.shsupercm.fabric.citresewn.mixin.cititem;

import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

@Mixin(BlockModel.class)
public interface JsonUnbakedModelAccessor {
    @Accessor
    Map<String, Either<Material, String>> getTextureMap();

    @Accessor
    void setParentLocation(ResourceLocation parentId);
}
