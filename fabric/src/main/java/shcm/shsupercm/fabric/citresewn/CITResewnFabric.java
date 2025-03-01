package shcm.shsupercm.fabric.citresewn;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import schm.shsupercm.citresewn.CITResewn;
import schm.shsupercm.citresewn.cit.CITRegistry;
import schm.shsupercm.citresewn.defaults.CITResewnDefaultsCompatAPI;

/**
 * Main initializer for CIT Resewn. Contains various internal utilities(just logging for now).
 */
public class CITResewnFabric implements ClientModInitializer {
    //@Entrypoint(Entrypoint.CLIENT)
    public static final CITResewnFabric INSTANCE = new CITResewnFabric();

    @Override
    public void onInitializeClient() {
        CITRegistry.registerAll();

        //for (EntrypointContainer<CITResewnDefaultsCompatAPI> compat : FabricLoader.getInstance().getEntrypointContainers(CITResewnDefaultsCompatAPI.ENTRYPOINT, CITResewnDefaultsCompatAPI.class))
       //     compat.getEntrypoint().onInitializeClient();

        CITResewnCommandFabric.register();
        CITResewn.init();
    }
}
