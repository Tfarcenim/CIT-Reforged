package shcm.shsupercm.fabric.citresewn.config;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CITResewnMixinConfiguration implements IMixinConfigPlugin {
    private static final String MIXINS_ROOT = "shcm.shsupercm.fabric.citresewn.mixin";


    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return mixinClassName.startsWith(MIXINS_ROOT);
    }


    @Override
    public String getRefMapperConfig() { return null; }
    @Override
    public List<String> getMixins() { return null; }
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }
    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}
