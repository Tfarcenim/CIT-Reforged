package shcm.shsupercm.fabric.citresewn.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.network.chat.Component;

/**
 * Mod Menu config button integration.
 */
//@Entrypoint("modmenu")
public class CITResewnModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (FabricLoader.getInstance().isModLoaded("cloth-config2"))
            return CITResewnConfigScreenFactoryFabric::create;

        return parent -> new AlertScreen(() -> Minecraft.getInstance().setScreen(parent), Component.literal("CIT Resewn"),
                Component.literal("CIT Resewn requires Cloth Config to be able to show the config."));
    }
}
