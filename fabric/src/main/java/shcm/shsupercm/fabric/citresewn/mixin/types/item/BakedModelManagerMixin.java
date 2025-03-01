package shcm.shsupercm.fabric.citresewn.mixin.types.item;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.fabric.citresewn.defaults.cit.types.TypeItem;

@Mixin(ModelManager.class)
public class BakedModelManagerMixin implements TypeItem.BakedModelManagerMixinAccess {
    private BakedModel citresewn$forcedMojankModel = null;

    @Inject(method = "getModel", cancellable = true, at =
    @At("HEAD"))
    private void citresewn$getCITMojankModel(ModelResourceLocation id, CallbackInfoReturnable<BakedModel> cir) {
        if (citresewn$forcedMojankModel != null) {
            cir.setReturnValue(citresewn$forcedMojankModel);
            citresewn$forcedMojankModel = null;
        }
    }

    @Override
    public void citresewn$forceMojankModel(BakedModel model) {
        this.citresewn$forcedMojankModel = model;
    }
}
