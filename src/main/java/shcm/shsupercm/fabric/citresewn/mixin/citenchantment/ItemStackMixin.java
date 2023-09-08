package shcm.shsupercm.fabric.citresewn.mixin.citenchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;
import shcm.shsupercm.fabric.citresewn.pack.cits.CITEnchantment;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public class ItemStackMixin implements CITEnchantment.Cached {
    private WeakReference<List<CITEnchantment>> citresewn_cachedCITEnchantment = new WeakReference<>(null);
    private long citresewn_cacheTimeCITEnchantment = 0;

    @Override
    public List<CITEnchantment> citresewn_getCachedCITEnchantment(Supplier<List<CITEnchantment>> realtime) {
        if (System.currentTimeMillis() - citresewn_cacheTimeCITEnchantment >= CITResewnConfig.INSTANCE().cache_ms) {
            citresewn_cachedCITEnchantment = new WeakReference<>(realtime.get());
            citresewn_cacheTimeCITEnchantment = System.currentTimeMillis();
        }

        return citresewn_cachedCITEnchantment.get();
    }

    @Inject(method = "hasFoil", cancellable = true, at = @At("HEAD"))
    private void disableDefaultGlint(CallbackInfoReturnable<Boolean> cir) {
        if (CITResewn.INSTANCE.activeCITs != null && ((!CITResewn.INSTANCE.activeCITs.effectiveGlobalProperties.useGlint) || (CITEnchantment.appliedContext != null && CITEnchantment.shouldApply && !CITEnchantment.appliedContext.get(0).useGlint)))
            cir.setReturnValue(false);
    }
}
