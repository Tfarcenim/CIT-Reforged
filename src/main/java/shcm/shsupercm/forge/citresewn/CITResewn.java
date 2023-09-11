package shcm.shsupercm.forge.citresewn;

import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shcm.shsupercm.forge.citresewn.config.CITResewnConfig;
import shcm.shsupercm.forge.citresewn.datagenerator.ModDatagen;
import shcm.shsupercm.forge.citresewn.pack.CITSpriteSource;

@Mod(CITResewn.MODID)
public class CITResewn {

    public static final String MODID = "citresewn";
    public static final SpriteSourceType CIT = SpriteSources.register(MODID +":cit", CITSpriteSource.CODEC);
    public static final Logger LOG = LogManager.getLogger("CITResewn");
    public static CITResewn INSTANCE;

    public ActiveCITs activeCITs = null;

    public CITResewnConfig config = null;

    public CITResewn() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModDatagen::gather);
        if (FMLEnvironment.dist.isClient()) {
            onInitializeClient();
        } else {
            LOG.warn("this does nothing on the server");
        }
    }
    
    public void onInitializeClient() {
        INSTANCE = this;

        config = CITResewnConfig.read();
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,() -> new IExtensionPoint.DisplayTest(
                () -> "dQw4w9WgXcQ",     // Send any version from server to client, since we will be accepting any version as well
                (remoteVersion, isFromServer) -> true));// Accept any version on the client, from server or from save
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
