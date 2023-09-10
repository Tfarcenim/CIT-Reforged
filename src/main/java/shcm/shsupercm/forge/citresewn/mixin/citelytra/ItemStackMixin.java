package shcm.shsupercm.forge.citresewn.mixin.citelytra;

import org.spongepowered.asm.mixin.Mixin;
import shcm.shsupercm.forge.citresewn.config.CITResewnConfig;
import shcm.shsupercm.forge.citresewn.pack.cits.CITElytra;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public class ItemStackMixin implements CITElytra.Cached {
    private WeakReference<CITElytra> citresewn_cachedCITElytra = new WeakReference<>(null);
    private long citresewn_cacheTimeCITElytra = 0;

    @Override
    public CITElytra citresewn_getCachedCITElytra(Supplier<CITElytra> realtime) {
        if (System.currentTimeMillis() - citresewn_cacheTimeCITElytra >= CITResewnConfig.INSTANCE().cache_ms) {
            citresewn_cachedCITElytra = new WeakReference<>(realtime.get());
            citresewn_cacheTimeCITElytra = System.currentTimeMillis();
        }

        return citresewn_cachedCITElytra.get();
    }
}
