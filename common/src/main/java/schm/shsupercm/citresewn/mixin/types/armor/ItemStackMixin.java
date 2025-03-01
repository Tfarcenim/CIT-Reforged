package schm.shsupercm.citresewn.mixin.types.armor;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import schm.shsupercm.citresewn.cit.CITCache;
import schm.shsupercm.citresewn.defaults.cit.types.TypeArmor;

@Mixin(ItemStack.class)
public class ItemStackMixin implements TypeArmor.CITCacheArmor {
    @Unique
    private final CITCache.Single<TypeArmor> citresewn$cacheTypeArmor = new CITCache.Single<>(TypeArmor.CONTAINER::getRealTimeCIT);

    @Override
    public CITCache.Single<TypeArmor> citresewn$getCacheTypeArmor() {
        return this.citresewn$cacheTypeArmor;
    }
}
