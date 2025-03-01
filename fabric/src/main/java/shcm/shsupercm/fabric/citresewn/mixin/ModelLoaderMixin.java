package shcm.shsupercm.fabric.citresewn.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shcm.shsupercm.fabric.citresewn.cit.ActiveCITs;

import java.util.Map;

/**
 * Initializes the (re)loading of active cits in the resource manager.
 * @see ActiveCITs
 */
@Mixin(ModelBakery.class)
public class ModelLoaderMixin {
    /**
     * @see ActiveCITs#load(ResourceManager, ProfilerFiller)
     */
    @Inject(method = "<init>", at =
    @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V"))
    private void citresewn$loadCITs(BlockColors blockColors, ProfilerFiller profiler, Map jsonUnbakedModels, Map blockStates, CallbackInfo ci) {
        profiler.push("citresewn:reloading_cits");
        ActiveCITs.load(Minecraft.getInstance().getResourceManager(), profiler);
        profiler.pop();
    }
}
