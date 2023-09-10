package shcm.shsupercm.forge.citresewn.mixin.citarmor;

import org.spongepowered.asm.mixin.Mixin;
import shcm.shsupercm.forge.citresewn.config.CITResewnConfig;
import shcm.shsupercm.forge.citresewn.pack.cits.CITArmor;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public class ItemStackMixin implements CITArmor.Cached {
    private WeakReference<CITArmor> citresewn_cachedCITArmor = new WeakReference<>(null);
    private long citresewn_cacheTimeCITArmor = 0;

    @Override
    public CITArmor citresewn_getCachedCITArmor(Supplier<CITArmor> realtime) {
        if (System.currentTimeMillis() - citresewn_cacheTimeCITArmor >= CITResewnConfig.INSTANCE().cache_ms) {
            citresewn_cachedCITArmor = new WeakReference<>(realtime.get());
            citresewn_cacheTimeCITArmor = System.currentTimeMillis();
        }

        return citresewn_cachedCITArmor.get();
    }
}
