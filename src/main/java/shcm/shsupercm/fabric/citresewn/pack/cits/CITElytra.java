package shcm.shsupercm.fabric.citresewn.pack.cits;

import shcm.shsupercm.fabric.citresewn.ex.CITParseException;
import shcm.shsupercm.fabric.citresewn.pack.CITPack;

import java.util.Properties;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public class CITElytra extends CIT {
    public final ResourceLocation textureIdentifier;

    public CITElytra(CITPack pack, ResourceLocation identifier, Properties properties) throws CITParseException {
        super(pack, identifier, properties);
        try {
            textureIdentifier = resolvePath(identifier, properties.getProperty("texture"), ".png", id -> pack.resourcePack.hasResource(PackType.CLIENT_RESOURCES, id));
            if (textureIdentifier == null)
                throw new Exception("Cannot resolve texture");
        } catch (Exception e) {
            throw new CITParseException(pack.resourcePack, identifier, (e.getClass() == Exception.class ? "" : e.getClass().getSimpleName() + ": ") + e.getMessage());
        }
    }

    public interface Cached {
        CITElytra citresewn_getCachedCITElytra(Supplier<CITElytra> realtime);
    }
}
