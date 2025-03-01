package shcm.shsupercm.fabric.citresewn.mixin.types.item;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import schm.shsupercm.citresewn.cit.CITCache;
import shcm.shsupercm.fabric.citresewn.defaults.cit.types.TypeItem;

@Mixin(ItemStack.class)
public class ItemStackMixin implements TypeItem.CITCacheItem {
    @Unique
    private final CITCache.Single<TypeItem> citresewn$cacheTypeItem = new CITCache.Single<>(TypeItem.CONTAINER::getRealTimeCIT);

    @Override
    public CITCache.Single<TypeItem> citresewn$getCacheTypeItem() {
        return this.citresewn$cacheTypeItem;
    }
}
