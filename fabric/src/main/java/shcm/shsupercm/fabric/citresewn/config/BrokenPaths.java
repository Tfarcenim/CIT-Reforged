package shcm.shsupercm.fabric.citresewn.config;

import net.minecraft.resources.ResourceLocation;
import schm.shsupercm.citresewn.config.CITResewnConfig;
import schm.shsupercm.citresewn.mixin.AbstractFileResourcePackMixin;

/**
 * Broken paths are resourcepack file paths that do not follow {@link ResourceLocation}'s specifications.<br>
 * When enabled in config, CIT Resewn will forcibly allow broken paths to load.<br>
 * If not enabled, broken paths has no effect on the game.
 * @see CITResewnConfig#broken_paths
 * @see CITResewnMixinConfiguration#broken_paths
 * @see AbstractFileResourcePackMixin
 */
public class BrokenPaths {
}
