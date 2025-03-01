package schm.shsupercm.citresewn.mixin;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FileToIdConverter.class)
public class ResourceFinderMixin {

    @Shadow @Final private String extension;

    @Inject(method = "idToFile", cancellable = true, at =
    @At("HEAD"))
    private void citresewn$forceAbsoluteTextureIdentifier(ResourceLocation id, CallbackInfoReturnable<ResourceLocation> cir) {
        if (id.getPath().endsWith(".png") && this.extension.equals(".png"))
            cir.setReturnValue(id);
    }
}
