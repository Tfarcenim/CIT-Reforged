package schm.shsupercm.citresewn.platform.services;

import schm.shsupercm.citresewn.pack.format.PropertyKey;
import schm.shsupercm.citresewn.pack.format.PropertyValue;

import java.io.File;
import java.util.Map;
import java.util.Set;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    void dispose();

    File getConfigDir();

    void registerCITTypes();

    void callHandlers(Map<PropertyKey, Set<PropertyValue>> properties);
}