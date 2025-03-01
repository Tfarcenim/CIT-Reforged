package schm.shsupercm.citresewn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schm.shsupercm.citresewn.config.CITResewnConfig;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class CITResewn {

    public static final String MOD_ID = "citresewn";
    public static final String MOD_NAME = "CITResewn";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.
    public static void init() {



        // It is common for all supported loaders to provide a similar feature that can not be used directly in the
        // common code. A popular way to get around this is using Java's built-in service loader feature to create
        // your own abstraction layer. You can learn more about this in our provided services class. In this example
        // we have an interface in the common code and use a loader specific implementation to delegate our call to
        // the platform specific approach.

    }

    /**
     * Logs an info line in CIT Resewn's name.
     * @param message log message
     */
    public static void info(String message) {
        LOG.info("[citresewn] " + message);
    }

    /**
     * Logs a warning line in CIT Resewn's name if enabled in config.
     * @see CITResewnConfig#mute_warns
     * @param message warn message
     */
    public static void logWarnLoading(String message) {
        if (CITResewnConfig.INSTANCE.mute_warns)
            return;
        LOG.error("[citresewn] " + message);
    }

    /**
     * Logs an error line in CIT Resewn's name if enabled in config.
     * @see CITResewnConfig#mute_errors
     * @param message error message
     */
    public static void logErrorLoading(String message) {
        if (CITResewnConfig.INSTANCE.mute_errors)
            return;
        LOG.error("{citresewn} " + message);
    }
}