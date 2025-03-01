package shcm.shsupercm.fabric.citresewn.platform;

import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import schm.shsupercm.citresewn.CITResewn;
import schm.shsupercm.citresewn.api.CITConditionContainer;
import schm.shsupercm.citresewn.api.CITDisposable;
import schm.shsupercm.citresewn.api.CITGlobalProperties;
import schm.shsupercm.citresewn.api.CITTypeContainer;
import schm.shsupercm.citresewn.cit.CITRegistry;
import schm.shsupercm.citresewn.pack.format.PropertyKey;
import schm.shsupercm.citresewn.pack.format.PropertyValue;
import schm.shsupercm.citresewn.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfigScreenFactoryFabric;

import java.io.File;
import java.util.Map;
import java.util.Set;

import static schm.shsupercm.citresewn.cit.CITRegistry.TYPES;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public void dispose() {
        for (CITDisposable disposable : FabricLoader.getInstance().getEntrypoints(CITDisposable.ENTRYPOINT, CITDisposable.class))
            disposable.dispose();
    }

    @Override
    public File getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().toFile();
    }

    @Override
    public void registerCITTypes() {
        for (EntrypointContainer<CITConditionContainer> entrypointContainer : FabricLoader.getInstance().getEntrypointContainers(CITConditionContainer.ENTRYPOINT, CITConditionContainer.class)) {
            String namespace = entrypointContainer.getProvider().getMetadata().getId();
            if (namespace.equals("citresewn-defaults"))
                namespace = "citresewn";

            for (String alias : entrypointContainer.getEntrypoint().aliases) {
                final PropertyKey key = new PropertyKey(namespace, alias);
                CITConditionContainer<?> container = entrypointContainer.getEntrypoint();

                CITRegistry.CONDITIONS.put(key, container);
                CITRegistry.getConditionToId().putIfAbsent(container.createCondition.get().getClass(), key);
            }
        }
        CITResewn.info("Registering CIT Types");
        for (var entrypointContainer : FabricLoader.getInstance().getEntrypointContainers(CITTypeContainer.ENTRYPOINT, CITTypeContainer.class)) {
            String namespace = entrypointContainer.getProvider().getMetadata().getId();
            if (namespace.equals("citresewn-defaults"))
                namespace = "citresewn";

            final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, entrypointContainer.getEntrypoint().id);
            CITTypeContainer<?> container = entrypointContainer.getEntrypoint();

            TYPES.put(id, container);
            CITRegistry.getTypeToId().putIfAbsent(container.createType.get().getClass(), id);
        }
    }

    @Override
    public void callHandlers(Map<PropertyKey, Set<PropertyValue>> properties) {
        for (EntrypointContainer<CITGlobalProperties> container : FabricLoader.getInstance().getEntrypointContainers(CITGlobalProperties.ENTRYPOINT, CITGlobalProperties.class)) {
            String containerNamespace = container.getProvider().getMetadata().getId();
            if (containerNamespace.equals("citresewn-defaults"))
                containerNamespace = "citresewn";

            for (Map.Entry<PropertyKey, Set<PropertyValue>> entry : properties.entrySet())
                if (entry.getKey().namespace().equals(containerNamespace)) {
                    PropertyValue lastValue = null;
                    for (PropertyValue value : entry.getValue())
                        lastValue = value;

                    try {
                        container.getEntrypoint().globalProperty(entry.getKey().path(), lastValue);
                    } catch (Exception e) {
                        CITResewn.logErrorLoading(lastValue == null ? "Errored while disposing global properties" : "Errored while parsing global properties: Line " + lastValue.position() + " of " + lastValue.propertiesIdentifier() + " in " + lastValue.packName());
                        e.printStackTrace();
                    }
                }
        }
    }

    @Override
    public Screen create(Screen parent) {
        return CITResewnConfigScreenFactoryFabric.create(parent);
    }

    @Override
    public String getModVersion() {
        return FabricLoader.getInstance().getModContainer(CITResewn.MOD_ID).orElseThrow().getMetadata().getVersion().toString();
    }
}
