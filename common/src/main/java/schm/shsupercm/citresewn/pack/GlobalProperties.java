package schm.shsupercm.citresewn.pack;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import schm.shsupercm.citresewn.pack.format.PropertyGroup;
import schm.shsupercm.citresewn.pack.format.PropertyKey;
import schm.shsupercm.citresewn.pack.format.PropertyValue;
import schm.shsupercm.citresewn.api.CITGlobalProperties;
import schm.shsupercm.citresewn.platform.Services;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Property group representation of the global cit.properties file.
 * @see CITGlobalProperties
 * @see PackParser#loadGlobalProperties(ResourceManager, GlobalProperties)
 */
public class GlobalProperties extends PropertyGroup {
    public GlobalProperties() {
        super("global_properties", ResourceLocation.fromNamespaceAndPath("citresewn", "global_properties"));
    }

    @Override
    public String getExtension() {
        return ".properties";
    }

    @Override
    public PropertyGroup load(String packName, ResourceLocation identifier, InputStream is) throws IOException, ResourceLocationException {
        PropertyGroup group = PropertyGroup.tryParseGroup(packName, identifier, is);
        if (group != null)
            for (Map.Entry<PropertyKey, Set<PropertyValue>> entry : group.properties.entrySet())
                this.properties.computeIfAbsent(entry.getKey(), key -> new LinkedHashSet<>()).addAll(entry.getValue());

        return this;
    }

    /**
     * Calls all {@link CITGlobalProperties} handler entrypoints for every global property they're associated with.<br>
     * Global properties are matched to their entrypoints by mod id and it's the handler responsibility to filter the properties.
     *
     * @see CITGlobalProperties
     */
    public void callHandlers() {
        Services.PLATFORM.callHandlers(properties);
    }
}
