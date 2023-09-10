package shcm.shsupercm.forge.citresewn.mixin;

import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AtlasSet.StitchResult.class)
public class AtlasPreparationMixin {
        @ModifyVariable(method = "getSprite", argsOnly = true, at = @At("HEAD"))
        private ResourceLocation citresewn$unwrapTexturePaths(ResourceLocation id) {
            if (id.getPath().endsWith(".png")) {
                id = id.withPath(path -> path.substring(0, path.length() - 4));

                if (id.getPath().startsWith("textures/"))
                    id = id.withPath(path -> path.substring(9));
            }
            return id;
        }
}
