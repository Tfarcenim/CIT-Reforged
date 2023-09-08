package shcm.shsupercm.fabric.citresewn.pack.cits;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.lwjgl.opengl.GL11;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;
import shcm.shsupercm.fabric.citresewn.ex.CITParseException;
import shcm.shsupercm.fabric.citresewn.mixin.citenchantment.BufferBuilderStorageAccessor;
import shcm.shsupercm.fabric.citresewn.mixin.citenchantment.RenderPhaseAccessor;
import shcm.shsupercm.fabric.citresewn.pack.CITPack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.*;
import static com.mojang.blaze3d.systems.RenderSystem.*;
import static shcm.shsupercm.util.logic.Loops.statelessFadingLoop;

public class CITEnchantment extends CIT {
    public static List<CITEnchantment> appliedContext = null;
    public static boolean shouldApply = false;

    public final ResourceLocation textureIdentifier;
    public final float speed, rotation, duration, r, g, b, a;
    public final int layer;
    public final boolean useGlint, blur;
    public final Blend blend;

    private final WrappedMethodIntensity methodIntensity = new WrappedMethodIntensity();

    public final Map<GlintRenderLayer, RenderType> renderLayers = new EnumMap<>(GlintRenderLayer.class);

    public CITEnchantment(CITPack pack, ResourceLocation identifier, Properties properties) throws CITParseException {
        super(pack, identifier, properties);
        try {
            textureIdentifier = resolvePath(identifier, properties.getProperty("texture"), ".png", id -> pack.resourcePack.hasResource(PackType.CLIENT_RESOURCES, id));
            if (textureIdentifier == null)
                throw new Exception("Cannot resolve texture");

            speed = Float.parseFloat(properties.getProperty("speed", "1"));

            rotation = Float.parseFloat(properties.getProperty("rotation", "0"));

            duration = Float.max(0f, Float.parseFloat(properties.getProperty("duration", "0")));

            layer = Integer.parseInt(properties.getProperty("layer", "0"));

            r = Math.max(0f, Float.parseFloat(properties.getProperty("r", "1")));
            g = Math.max(0f, Float.parseFloat(properties.getProperty("g", "1")));
            b = Math.max(0f, Float.parseFloat(properties.getProperty("b", "1")));
            a = Math.max(0f, Float.parseFloat(properties.getProperty("a", "1")));

            useGlint = switch (properties.getProperty("useGlint", "false").toLowerCase(Locale.ENGLISH)) {
                case "true" -> true;
                case "false" -> false;
                default -> throw new Exception("useGlint is not a boolean");
            };

            blur = switch (properties.getProperty("blur", "true").toLowerCase(Locale.ENGLISH)) {
                case "true" -> true;
                case "false" -> false;
                default -> throw new Exception("blur is not a boolean");
            };

            blend = Blend.getBlend(properties.getProperty("blend", "add"));
        } catch (Exception e) {
            throw new CITParseException(pack.resourcePack, identifier, (e.getClass() == Exception.class ? "" : e.getClass().getSimpleName() + ": ") + e.getMessage());
        }
    }

    public void activate() {
        for (GlintRenderLayer glintLayer : GlintRenderLayer.values()) {
            RenderType renderLayer = glintLayer.build(this);

            renderLayers.put(glintLayer, renderLayer);
            ((BufferBuilderStorageAccessor) Minecraft.getInstance().renderBuffers()).entityBuilders().put(renderLayer, new BufferBuilder(renderLayer.bufferSize()));
        }
    }

    @Override
    public void dispose() {
        appliedContext = null;
        for (RenderType renderLayer : renderLayers.values())
            ((BufferBuilderStorageAccessor) Minecraft.getInstance().renderBuffers()).entityBuilders().remove(renderLayer);
    }

    public enum GlintRenderLayer {
        ARMOR_GLINT("armor_glint", 8f, layer -> layer
                .setShaderState(RenderPhaseAccessor.ARMOR_GLINT_SHADER())
                .setWriteMaskState(RenderPhaseAccessor.COLOR_MASK())
                .setCullState(RenderPhaseAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderPhaseAccessor.EQUAL_DEPTH_TEST())
                .setLayeringState(RenderPhaseAccessor.VIEW_OFFSET_Z_LAYERING())),
        ARMOR_ENTITY_GLINT("armor_entity_glint", 0.16f, layer -> layer
                .setShaderState(RenderPhaseAccessor.ARMOR_ENTITY_GLINT_SHADER())
                .setWriteMaskState(RenderPhaseAccessor.COLOR_MASK())
                .setCullState(RenderPhaseAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderPhaseAccessor.EQUAL_DEPTH_TEST())
                .setLayeringState(RenderPhaseAccessor.VIEW_OFFSET_Z_LAYERING())),
        GLINT_TRANSLUCENT("glint_translucent", 8f, layer -> layer
                .setShaderState(RenderPhaseAccessor.TRANSLUCENT_GLINT_SHADER())
                .setWriteMaskState(RenderPhaseAccessor.COLOR_MASK())
                .setCullState(RenderPhaseAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderPhaseAccessor.EQUAL_DEPTH_TEST())
                .setOutputState(RenderPhaseAccessor.ITEM_TARGET())),
        GLINT("glint", 8f, layer -> layer
                .setShaderState(RenderPhaseAccessor.GLINT_SHADER())
                .setWriteMaskState(RenderPhaseAccessor.COLOR_MASK())
                .setCullState(RenderPhaseAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderPhaseAccessor.EQUAL_DEPTH_TEST())),
        DIRECT_GLINT("glint_direct", 8f, layer -> layer
                .setShaderState(RenderPhaseAccessor.DIRECT_GLINT_SHADER())
                .setWriteMaskState(RenderPhaseAccessor.COLOR_MASK())
                .setCullState(RenderPhaseAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderPhaseAccessor.EQUAL_DEPTH_TEST())),
        ENTITY_GLINT("entity_glint", 0.16f, layer -> layer
                .setShaderState(RenderPhaseAccessor.ENTITY_GLINT_SHADER())
                .setWriteMaskState(RenderPhaseAccessor.COLOR_MASK())
                .setCullState(RenderPhaseAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderPhaseAccessor.EQUAL_DEPTH_TEST())
                .setOutputState(RenderPhaseAccessor.ITEM_TARGET())),
        DIRECT_ENTITY_GLINT("entity_glint_direct", 0.16f, layer -> layer
                .setShaderState(RenderPhaseAccessor.DIRECT_ENTITY_GLINT_SHADER())
                .setWriteMaskState(RenderPhaseAccessor.COLOR_MASK())
                .setCullState(RenderPhaseAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderPhaseAccessor.EQUAL_DEPTH_TEST()));

        public final String name;
        private final Consumer<RenderType.CompositeState.CompositeStateBuilder> setup;
        private final float scale;

        GlintRenderLayer(String name, float scale, Consumer<RenderType.CompositeState.CompositeStateBuilder> setup) {
            this.name = name;
            this.scale = scale;
            this.setup = setup;
        }

        public RenderType build(CITEnchantment enchantment) {
            final float speed = enchantment.speed, rotation = enchantment.rotation, r = enchantment.r, g = enchantment.g, b = enchantment.b, a = enchantment.a;
            final WrappedMethodIntensity methodIntensity = enchantment.methodIntensity;
            //noinspection ConstantConditions
            RenderType.CompositeState.CompositeStateBuilder layer = RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(enchantment.textureIdentifier, enchantment.blur, false))
                    .setTexturingState(new RenderStateShard.TexturingStateShard("citresewn_glint_texturing", () -> {
                        float l = Util.getMillis() * CITResewnConfig.INSTANCE().citenchantment_scroll_multiplier * speed;
                        float x = (l % 110000f) / 110000f;
                        float y = (l % 30000f) / 30000f;
                        Matrix4f matrix4f = Matrix4f.createTranslateMatrix(-x, y, 0.0f);
                        matrix4f.multiply(Vector3f.ZP.rotationDegrees(rotation + 10f));
                        matrix4f.multiply(Matrix4f.createScaleMatrix(scale, scale, scale));
                        setTextureMatrix(matrix4f);

                        setShaderColor(r, g, b, a * methodIntensity.intensity);
                    }, () -> {
                        RenderSystem.resetTextureMatrix();

                        setShaderColor(1f, 1f, 1f, 1f);
                    }))
                    .setTransparencyState(enchantment.blend);

            this.setup.accept(layer);

            return RenderType.create("citresewn:enchantment_" + this.name + ":" + enchantment.propertiesIdentifier.toString(),
                    DefaultVertexFormat.POSITION_TEX,
                    VertexFormat.Mode.QUADS,
                    256,
                    layer.createCompositeState(false));
        }

        public VertexConsumer tryApply(VertexConsumer base, RenderType baseLayer, MultiBufferSource provider) {
            if (!shouldApply || appliedContext == null || appliedContext.size() == 0)
                return null;

            VertexConsumer[] layers = new VertexConsumer[Math.min(appliedContext.size(), appliedContext.get(0).pack.cap)];

            for (int i = 0; i < layers.length; i++)
                layers[i] = provider.getBuffer(appliedContext.get(i).renderLayers.get(GlintRenderLayer.this));

            provider.getBuffer(baseLayer); // refresh base layer for armor consumer

            return base == null ? VertexMultiConsumer.create(layers) : VertexMultiConsumer.create(VertexMultiConsumer.create(layers), base);
        }
    }

    public static class Blend extends RenderStateShard.TransparencyStateShard {
        private final int src, dst, srcAlpha, dstAlpha;

        private Blend(String name, int src, int dst, int srcAlpha, int dstAlpha) {
            super(name + "_glint_transparency", null, null);
            this.src = src;
            this.dst = dst;
            this.srcAlpha = srcAlpha;
            this.dstAlpha = dstAlpha;
        }

        private Blend(String name, int src, int dst) {
            this(name, src, dst, GL_ZERO, GL_ONE);
        }

        @Override
        public void setupRenderState() {
            enableBlend();
            blendFuncSeparate(src, dst, srcAlpha, dstAlpha);
        }

        @Override
        public void clearRenderState() {
            defaultBlendFunc();
            disableBlend();
        }

        public static Blend getBlend(String blendString) throws BlendFormatException {
            try { //check named blending function
                return Named.valueOf(blendString.toUpperCase(Locale.ENGLISH)).blend;
            } catch (IllegalArgumentException ignored) { // create custom blending function
                try {
                    String[] split = blendString.split(" ");
                    int src, dst, srcAlpha, dstAlpha;
                    if (split.length == 2) {
                        src = parseGLConstant(split[0]);
                        dst = parseGLConstant(split[1]);
                        srcAlpha = GL_ZERO;
                        dstAlpha = GL_ONE;
                    } else if (split.length == 4) {
                        src = parseGLConstant(split[0]);
                        dst = parseGLConstant(split[1]);
                        srcAlpha = parseGLConstant(split[2]);
                        dstAlpha = parseGLConstant(split[3]);
                    } else
                        throw new Exception();

                    return new Blend("custom_" + src + "_" + dst + "_" + srcAlpha + "_" + dstAlpha, src, dst, srcAlpha, dstAlpha);
                } catch (Exception e) {
                    throw new BlendFormatException();
                }
            }
        }

        private enum Named {
            REPLACE(new Blend("replace", 0, 0) {
                @Override
                public void setupRenderState() {
                    disableBlend();
                }
            }),
            GLINT(new Blend("glint", GL_SRC_COLOR, GL_ONE)),
            ALPHA(new Blend("alpha", GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)),
            ADD(new Blend("add", GL_SRC_ALPHA, GL_ONE)),
            SUBTRACT(new Blend("subtract", GL_ONE_MINUS_DST_COLOR, GL_ZERO)),
            MULTIPLY(new Blend("multiply", GL_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA)),
            DODGE(new Blend("dodge", GL_ONE, GL_ONE)),
            BURN(new Blend("burn", GL_ZERO, GL_ONE_MINUS_SRC_COLOR)),
            SCREEN(new Blend("screen", GL_ONE, GL_ONE_MINUS_SRC_COLOR)),
            OVERLAY(new Blend("overlay", GL_DST_COLOR, GL_SRC_COLOR));

            public final Blend blend;

            Named(Blend blend) {
                this.blend = blend;
            }
        }

        private static int parseGLConstant(String s) throws Exception {
            try {
                return GL11.class.getDeclaredField(s).getInt(null);
            } catch (NoSuchFieldException ignored) { }

            return s.startsWith("0x") ? Integer.parseInt(s.substring(2), 16) : Integer.parseInt(s);
        }

        public static class BlendFormatException extends Exception {
            public BlendFormatException() {
                super("Not a valid blending method");
            }
        }
    }

    public enum MergeMethod {
        AVERAGE {
            @Override
            public void applyIntensity(Map<ResourceLocation, Integer> stackEnchantments, CITEnchantment cit) {
                ResourceLocation enchantment = null;
                for (ResourceLocation enchantmentMatch : cit.enchantments)
                    if (stackEnchantments.containsKey(enchantmentMatch)) {
                        enchantment = enchantmentMatch;
                        break;
                    }

                if (enchantment == null) {
                    cit.methodIntensity.intensity = 0f;
                } else {
                    float sum = 0f;
                    for (Integer value : stackEnchantments.values())
                        sum += value;

                    cit.methodIntensity.intensity = (float) stackEnchantments.get(enchantment) / sum;
                }
            }
        },
        LAYERED {
            @Override
            public void applyIntensity(Map<ResourceLocation, Integer> stackEnchantments, CITEnchantment cit) {
                ResourceLocation enchantment = null;
                for (ResourceLocation enchantmentMatch : cit.enchantments)
                    if (stackEnchantments.containsKey(enchantmentMatch)) {
                        enchantment = enchantmentMatch;
                        break;
                    }
                if (enchantment == null) {
                    cit.methodIntensity.intensity = 0f;
                    return;
                }

                float max = 0f;
                for (Integer value : stackEnchantments.values())
                    if (value > max)
                        max = value;

                cit.methodIntensity.intensity = (float) stackEnchantments.get(enchantment) / max;
            }
        },
        CYCLE {
            @Override
            public void applyMethod(List<CITEnchantment> citEnchantments, ItemStack stack) {
                List<Map.Entry<CITEnchantment, Float>> durations = new ArrayList<>();
                for (CITEnchantment cit : citEnchantments)
                    durations.add(new HashMap.SimpleEntry<>(cit, cit.duration));

                for (Map.Entry<CITEnchantment, Float> intensity : statelessFadingLoop(durations, citEnchantments.get(0).pack.fade, ticks, 20).entrySet())
                    intensity.getKey().methodIntensity.intensity = intensity.getValue();
            }
        };

        public static int ticks = 0;

        public void applyIntensity(Map<ResourceLocation, Integer> stackEnchantments, CITEnchantment cit) {
            cit.methodIntensity.intensity = 1f;
        }

        public void applyMethod(List<CITEnchantment> citEnchantments, ItemStack stack) {
            Map<ResourceLocation, Integer> stackEnchantments = new LinkedHashMap<>();
            for (Tag nbtElement : stack.is(Items.ENCHANTED_BOOK) ? EnchantedBookItem.getEnchantments(stack) : stack.getEnchantmentTags())
                stackEnchantments.put(EnchantmentHelper.getEnchantmentId((CompoundTag) nbtElement), EnchantmentHelper.getEnchantmentLevel((CompoundTag) nbtElement));

            for (CITEnchantment cit : citEnchantments)
                if (!cit.enchantmentsAny)
                    applyIntensity(stackEnchantments, cit);
        }
    }

    private static class WrappedMethodIntensity {
        public float intensity = 1f;
    }

    public interface Cached {
        List<CITEnchantment> citresewn_getCachedCITEnchantment(Supplier<List<CITEnchantment>> realtime);
    }
}