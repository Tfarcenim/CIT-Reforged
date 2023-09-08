package shcm.shsupercm.fabric.citresewn.ex;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;

/**
 * Thrown when a cit failed to be loaded
 */
public class CITLoadException extends Exception {
    public CITLoadException(PackResources resourcePack, ResourceLocation identifier, String message) {
        super("Couldn't load CIT: " + message + " in " + resourcePack.getName() + " -> " + identifier.toString());
    }
}
