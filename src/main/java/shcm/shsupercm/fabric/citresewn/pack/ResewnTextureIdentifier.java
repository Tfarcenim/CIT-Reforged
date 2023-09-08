package shcm.shsupercm.fabric.citresewn.pack;

import net.minecraft.resources.ResourceLocation;
import shcm.shsupercm.fabric.citresewn.mixin.core.SpriteAtlasTextureMixin;

/**
 * Marks path identifiers as forced literal texture paths.
 * @see SpriteAtlasTextureMixin
 */
public class ResewnTextureIdentifier extends ResourceLocation {
    public ResewnTextureIdentifier(ResourceLocation identifier) {
        super(identifier.getNamespace(), identifier.getPath());
    }
}
