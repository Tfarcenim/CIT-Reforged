package shcm.shsupercm.fabric.citresewn.mixin.broken_paths;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.fabric.citresewn.CITResewn;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/* if (CITResewnConfig.read().broken_paths) */ @Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerImplMixin {
    @Shadow @Final private PackType type;

    @Inject(method = "reload", at = @At("RETURN"))
    public void onReload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<PackResources> packs, CallbackInfoReturnable<ReloadInstance> cir) {
        if (CITResewn.INSTANCE.processingBrokenPaths = this.type == PackType.CLIENT_RESOURCES) {
            CITResewn.LOG.error("[citresewn] Caution! Broken paths is enabled!");
            cir.getReturnValue().done().thenRun(() -> CITResewn.INSTANCE.processingBrokenPaths = false);
        }
    }
}
