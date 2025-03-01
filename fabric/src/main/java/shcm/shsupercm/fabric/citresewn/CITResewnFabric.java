package shcm.shsupercm.fabric.citresewn;

import net.fabricmc.api.ClientModInitializer;
import schm.shsupercm.citresewn.CITResewn;
import schm.shsupercm.citresewn.cit.CITRegistry;

/**
 * Main initializer for CIT Resewn. Contains various internal utilities(just logging for now).
 */
public class CITResewnFabric implements ClientModInitializer {
    //@Entrypoint(Entrypoint.CLIENT)
    public static final CITResewnFabric INSTANCE = new CITResewnFabric();

    @Override
    public void onInitializeClient() {
        CITRegistry.registerAll();

        CITResewnCommandFabric.register();
        CITResewn.init();
    }
}
