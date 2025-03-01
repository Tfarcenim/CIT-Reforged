package shcm.shsupercm.fabric.citresewn.mixin.types.elytra;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import schm.shsupercm.citresewn.cit.CITCache;
import shcm.shsupercm.fabric.citresewn.defaults.cit.types.TypeElytra;

@Mixin(ItemStack.class)
public class ItemStackMixin implements TypeElytra.CITCacheElytra {
    @Unique
    private final CITCache.Single<TypeElytra> citresewn$cacheTypeElytra = new CITCache.Single<>(TypeElytra.CONTAINER::getRealTimeCIT);

    @Override
    public CITCache.Single<TypeElytra> citresewn$getCacheTypeElytra() {
        return this.citresewn$cacheTypeElytra;
    }
}
