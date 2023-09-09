package shcm.shsupercm.fabric.citresewn.ex;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;

/**
 * Thrown when a cit failed to be parsed
 */
public class CITParseException extends Exception {
    public CITParseException(PackResources resourcePack, ResourceLocation identifier, String message) {
        super("Skipped CIT: " + message + " in " + resourcePack.packId() + " -> " + identifier.toString());
    }
}
