package shcm.shsupercm.fabric.citresewn.mixin.core;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shcm.shsupercm.CITHooks;

@Mixin(value = ModelBakery.class, priority = 999)
public abstract class ModelLoaderMixin {

    @Inject(method = "loadTopLevel", at = @At("TAIL"))
    public void initCITs(ModelResourceLocation eventModelId, CallbackInfo ci) {
        CITHooks.initCITS(eventModelId);
    }
}
