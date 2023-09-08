package shcm.shsupercm.fabric.citresewn.mixin.broken_paths;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* if (CITResewnConfig.read().broken_paths) */ @Mixin(PackCompatibility.class)
public abstract class ResourcePackCompatibilityMixin {
    private static final PackCompatibility BROKEN_PATHS = ResourcePackCompatibility("BROKEN_PATHS", -1, "broken_paths");

    @Invoker("<init>")
    public static PackCompatibility ResourcePackCompatibility(String internalName, int internalId, String translationSuffix) {
        throw new AssertionError();
    }

    @Inject(method = "from(ILnet/minecraft/resource/ResourceType;)Lnet/minecraft/resource/ResourcePackCompatibility;", cancellable = true, at = @At("HEAD"))
    private static void redirectBrokenPathsCompatibility(int packVersion, PackType type, CallbackInfoReturnable<PackCompatibility> cir) {
        if (packVersion == Integer.MAX_VALUE - 53)
            cir.setReturnValue(BROKEN_PATHS);
    }
}
