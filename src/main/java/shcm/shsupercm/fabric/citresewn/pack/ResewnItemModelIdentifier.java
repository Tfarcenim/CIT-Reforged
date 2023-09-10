package shcm.shsupercm.fabric.citresewn.pack;

import net.minecraft.resources.ResourceLocation;

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
