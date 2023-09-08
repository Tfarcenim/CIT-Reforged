package shcm.shsupercm.fabric.citresewn.mixin.citenchantment;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderPhaseAccessor {
    @Accessor("RENDERTYPE_ARMOR_GLINT_SHADER") static RenderStateShard.ShaderStateShard ARMOR_GLINT_SHADER() { throw new RuntimeException(); }
    @Accessor("RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER") static RenderStateShard.ShaderStateShard ARMOR_ENTITY_GLINT_SHADER() { throw new RuntimeException(); }
    @Accessor("RENDERTYPE_GLINT_TRANSLUCENT_SHADER") static RenderStateShard.ShaderStateShard TRANSLUCENT_GLINT_SHADER() { throw new RuntimeException(); }
    @Accessor("RENDERTYPE_GLINT_SHADER") static RenderStateShard.ShaderStateShard GLINT_SHADER() { throw new RuntimeException(); }
    @Accessor("RENDERTYPE_GLINT_DIRECT_SHADER") static RenderStateShard.ShaderStateShard DIRECT_GLINT_SHADER() { throw new RuntimeException(); }
    @Accessor("RENDERTYPE_ENTITY_GLINT_SHADER") static RenderStateShard.ShaderStateShard ENTITY_GLINT_SHADER() { throw new RuntimeException(); }
    @Accessor("RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER") static RenderStateShard.ShaderStateShard DIRECT_ENTITY_GLINT_SHADER() { throw new RuntimeException(); }
    @Accessor("NO_CULL") static RenderStateShard.CullStateShard DISABLE_CULLING() { throw new RuntimeException(); }
    @Accessor("EQUAL_DEPTH_TEST") static RenderStateShard.DepthTestStateShard EQUAL_DEPTH_TEST() { throw new RuntimeException(); }
    @Accessor("COLOR_WRITE") static RenderStateShard.WriteMaskStateShard COLOR_MASK() { throw new RuntimeException(); }
    @Accessor("VIEW_OFFSET_Z_LAYERING") static RenderStateShard.LayeringStateShard VIEW_OFFSET_Z_LAYERING() { throw new RuntimeException(); }
    @Accessor("ITEM_ENTITY_TARGET") static RenderStateShard.OutputStateShard ITEM_TARGET() { throw new RuntimeException(); }
}
