package shcm.shsupercm.fabric.citresewn.pack;

import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.lang3.StringUtils;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.ex.CITParseException;
import shcm.shsupercm.fabric.citresewn.pack.cits.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

/**
 * Parses cits from resourcepacks
 */
public final class CITParser { private CITParser() {}
    /**
     * CIT type registry.
     */
    public static final Map<String, CITConstructor> REGISTRY = new HashMap<>();
    static {
        REGISTRY.put("item", CITItem::new);
        REGISTRY.put("armor", CITArmor::new);
        REGISTRY.put("elytra", CITElytra::new);
        REGISTRY.put("enchantment", CITEnchantment::new);
    }

    /**
     * Parses cit entries from an ordered collection of resourcepacks.
     * @param packs packs to parse
     * @return a collection of parsed CITs
     */
    public static List<CITPack> parseCITs(Collection<PackResources> packs) {
        List<CITPack> list = new ArrayList<>();
        for (PackResources pack : packs) {
            Collection<CITPack> parse = parse(pack);
            for (CITPack citPack : parse) {
                list.add(citPack);
            }
        }
        return list;
    }

    /**
     * Parses a resourcepack into a possible collection of citpacks that are contained within.
     * @param resourcePack pack to parse
     * @return a collection of CITPacks or an empty collection if resourcepack contains none
     */
    public static Collection<CITPack> parse(PackResources resourcePack) {
        final CITPack citPack = new CITPack(resourcePack);

        Collection<ResourceLocation> packProperties = new ArrayList<>();
        for (String namespace : resourcePack.getNamespaces(PackType.CLIENT_RESOURCES))
            if (ResourceLocation.isValidResourceLocation(namespace))
                for (String citRoot : new String[] { "citresewn", "optifine", "mcpatcher" }) {


                        List<ResourceLocation> locations = new ArrayList<>();
                        resourcePack.listResources(PackType.CLIENT_RESOURCES, namespace, citRoot + "/cit", (resourceLocation, inputStreamIoSupplier) -> locations.add(resourceLocation));
                        packProperties.addAll(locations);
                        //     packProperties.addAll(resourcePack.getResources(PackType.CLIENT_RESOURCES, namespace, citRoot + "/cit", s -> s.getPath().endsWith(".properties")));
                        ResourceLocation global = new ResourceLocation(namespace, citRoot + "/cit.properties");
                        if (resourcePack.getResource(PackType.CLIENT_RESOURCES, global) != null)
                            packProperties.add(global);
                    }

        boolean readGlobalProperties = false;
        for (Iterator<ResourceLocation> iterator = packProperties.iterator(); iterator.hasNext(); ) {
            ResourceLocation propertiesIdentifier = iterator.next();
            try {
                if (StringUtils.countMatches(propertiesIdentifier.getPath(), '/') <= 2 && propertiesIdentifier.getPath().endsWith("cit.properties")) {
                    iterator.remove();
                    if (!readGlobalProperties)
                        try (InputStream is = resourcePack.getResource(PackType.CLIENT_RESOURCES, propertiesIdentifier).get()) {
                            Properties citProperties = new Properties();
                            citProperties.load(is);
                            citPack.loadGlobalProperties(citProperties);
                            readGlobalProperties = true;
                        }
                }
            } catch (Exception e) {
                CITResewn.logErrorLoading("Skipped global properties: " + e.getMessage() + " in " + resourcePack.packId() + " -> " + propertiesIdentifier);
            }
        }

        packProperties.stream()
                .flatMap(citIdentifier -> {
                    try (InputStream is = resourcePack.getResource(PackType.CLIENT_RESOURCES, citIdentifier).get()) {
                        Properties citProperties = new Properties();
                        citProperties.load(new InputStreamReader(is, StandardCharsets.UTF_8));

                        CITConstructor type = REGISTRY.get(citProperties.getProperty("type", "item"));
                        if (type == null)
                            throw new CITParseException(citPack.resourcePack, citIdentifier, "Unknown cit type \"" + citProperties.getProperty("type") + "\"");

                        return Stream.of(type.cit(citPack, citIdentifier, citProperties));
                    } catch (Exception e) {
                        CITResewn.logErrorLoading(e.getMessage());
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toCollection(() -> citPack.cits));

        if (citPack.cits.isEmpty())
            return Collections.emptySet();
        else {
            CITResewn.info("Found " + citPack.cits.size() + " CIT" + (citPack.cits.size() == 1 ? "" : "s") + " in " + resourcePack.packId());
            return Collections.singleton(citPack);
        }
    }

    public interface CITConstructor {
        CIT cit(CITPack pack, ResourceLocation identifier, Properties properties) throws CITParseException;
    }
}
