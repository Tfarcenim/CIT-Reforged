package shcm.shsupercm.fabric.citresewn.mixin.core;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shcm.shsupercm.fabric.citresewn.ActiveCITs;
import shcm.shsupercm.fabric.citresewn.CITHooks;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;
import shcm.shsupercm.fabric.citresewn.pack.CITPack;
import shcm.shsupercm.fabric.citresewn.pack.CITParser;
import shcm.shsupercm.fabric.citresewn.pack.cits.CIT;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import static shcm.shsupercm.fabric.citresewn.CITResewn.info;

@Mixin(value = ModelBakery.class, priority = 999)
public abstract class ModelLoaderMixin {
    @Shadow @Final
    protected ResourceManager resourceManager;

    @Inject(method = "loadTopLevel", at = @At("TAIL"))
    public void initCITs(ModelResourceLocation eventModelId, CallbackInfo ci) {
        CITHooks.initCITS(eventModelId,resourceManager);
    }
}
