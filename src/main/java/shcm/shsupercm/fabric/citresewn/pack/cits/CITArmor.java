package shcm.shsupercm.fabric.citresewn.pack.cits;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import org.openjdk.nashorn.internal.objects.NativeUint8Array;
import shcm.shsupercm.fabric.citresewn.ex.CITParseException;
import shcm.shsupercm.fabric.citresewn.pack.CITPack;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

public class CITArmor extends CIT {
    public final Map<String, ResourceLocation> textures = new HashMap<>();

    public CITArmor(CITPack pack, ResourceLocation identifier, Properties properties) throws CITParseException {
        super(pack, identifier, properties);
        try {
            if (this.items.size() == 0)
                throw new Exception("CIT must target at least one item type");
            for (Item item : this.items)
                if (!(item instanceof ArmorItem))
                    throw new Exception("Armor CIT must target armor items only(" + BuiltInRegistries.ITEM.getKey(item) + " is not armor)");

            for (Object o : properties.keySet())
                if (o instanceof String property && property.startsWith("texture.")) {
                    ResourceLocation textureIdentifier = resolvePath(identifier, properties.getProperty(property), ".png", Minecraft.getInstance().getResourceManager());
                    if (textureIdentifier == null)
                        throw new Exception("Cannot resolve path for " + property);

                    this.textures.put(property.substring(8), textureIdentifier);
                }
        } catch (Exception e) {
            throw new CITParseException(pack.resourcePack, identifier, (e.getClass() == Exception.class ? "" : e.getClass().getSimpleName() + ": ") + e.getMessage());
        }
    }

    public interface Cached {
        CITArmor citresewn_getCachedCITArmor(Supplier<CITArmor> realtime);
    }
}
