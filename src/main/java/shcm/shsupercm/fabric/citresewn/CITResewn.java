package shcm.shsupercm.fabric.citresewn;

import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;

@Mod("citresewn")
public class CITResewn {
    public static final Logger LOG = LogManager.getLogger("CITResewn");
    public static CITResewn INSTANCE;

    public ActiveCITs activeCITs = null;

    public CITResewnConfig config = null;

    public CITResewn() {
    	onInitializeClient();
    }
    
    public void onInitializeClient() {
        INSTANCE = this;

        config = CITResewnConfig.read();
    }

    public static void info(String message) {
        LOG.info("[citresewn] " + message);
    }

    public static void logWarnLoading(String message) {
        if (CITResewnConfig.INSTANCE().mute_warns)
            return;
        LOG.error("[citresewn] " + message);
    }

    public static void logErrorLoading(String message) {
        if (CITResewnConfig.INSTANCE().mute_errors)
            return;
        LOG.error("{citresewn} " + message);
    }
}
