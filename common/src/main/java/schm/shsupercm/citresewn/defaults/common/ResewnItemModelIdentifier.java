package schm.shsupercm.citresewn.defaults.common;

import net.minecraft.resources.ResourceLocation;

/**
 * Marks models as cit models.
 */
public class ResewnItemModelIdentifier {
    private static final String MARKER = "citresewn_model_path";

    public static ResourceLocation pack(ResourceLocation identifier) {
        return ResourceLocation.fromNamespaceAndPath(MARKER, identifier.getNamespace() + '/' + identifier.getPath());
    }

    public static boolean marked(ResourceLocation identifier) {
        return identifier.getNamespace().equals(MARKER);
    }

    public static ResourceLocation unpack(ResourceLocation identifier) {
        if (!marked(identifier))
            throw new IllegalArgumentException("The given identifier is not a packed resewn model");

        int split = identifier.getPath().indexOf('/');
        return ResourceLocation.fromNamespaceAndPath(identifier.getPath().substring(0, split), identifier.getPath().substring(split + 1));
    }
}
