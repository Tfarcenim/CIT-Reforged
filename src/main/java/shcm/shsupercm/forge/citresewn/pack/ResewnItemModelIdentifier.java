package shcm.shsupercm.forge.citresewn.pack;

import net.minecraft.resources.ResourceLocation;
import shcm.shsupercm.forge.citresewn.mixin.cititem.ModelLoaderMixin;

/**
 * Marks models as cit item models.
 * @see ModelLoaderMixin
 */
public class ResewnItemModelIdentifier extends ResourceLocation {
    public ResewnItemModelIdentifier(String id) {
        super(id);
    }

    public ResewnItemModelIdentifier(ResourceLocation identifier) {
        super(identifier.getNamespace(), identifier.getPath());
    }
}
