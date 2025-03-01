package schm.shsupercm.citresewn.defaults;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import schm.shsupercm.citresewn.defaults.cit.types.TypeArmor;
import schm.shsupercm.citresewn.defaults.cit.types.TypeElytra;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Holder for utility compatibility methods for use in other mods.
 */
public abstract class CITResewnDefaultsCompatAPI {
    /**
     * Entrypoint for client initialization that's only called with CIT Resewn: Defaults present.
     */
    public static final String ENTRYPOINT = "citresewn:defaults_compat";

    //@Entrypoint(Entrypoint.CLIENT)
   // public static void initAll() {
  //      for (EntrypointContainer<CITResewnDefaultsCompatAPI> compat : FabricLoader.getInstance().getEntrypointContainers(CITResewnDefaultsCompatAPI.ENTRYPOINT, CITResewnDefaultsCompatAPI.class))
   //         compat.getEntrypoint().onInitializeClient();
  //  }

    /**
     * Registers a slot redirect for type=armor
     * @param redirect returns the currently visible armor item in the given equipment slot or null to not redirect.
     */
    protected final void typeArmorRedirectSlotGetter(BiFunction<LivingEntity, EquipmentSlot, ItemStack> redirect) {
        TypeArmor.CONTAINER.getItemInSlotCompatRedirects.add(redirect);
    }

    /**
     * Registers a slot redirect for type=elytra
     * @param redirect returns the currently visible elytra item or null to not redirect.
     */
    protected final void typeElytraRedirectSlotGetter(Function<LivingEntity, ItemStack> redirect) {
        TypeElytra.CONTAINER.getItemInSlotCompatRedirects.add(redirect);
    }
}
