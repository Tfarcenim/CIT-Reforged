package schm.shsupercm.citresewn.platform;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.loading.FMLPaths;
import schm.shsupercm.citresewn.CITResewn;
import schm.shsupercm.citresewn.Shim;
import schm.shsupercm.citresewn.pack.format.PropertyKey;
import schm.shsupercm.citresewn.pack.format.PropertyValue;
import schm.shsupercm.citresewn.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public void dispose() {
        Shim.initDispose();
    }

    @Override
    public File getConfigDir() {
        return FMLPaths.CONFIGDIR.get().toFile();
    }

    @Override
    public void registerCITTypes() {
        Shim.initConditions();
        Shim.initTypes();
    }

    @Override
    public void callHandlers(Map<PropertyKey, Set<PropertyValue>> properties) {
        Shim.initGlobalProperties(properties);
    }

    @Override
    public Screen create(Screen parent) {
        return null;
    }

    @Override
    public String getModVersion() {
        return ModList.get().getModFileById(CITResewn.MOD_ID).versionString();
    }
}