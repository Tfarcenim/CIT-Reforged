package schm.shsupercm.citresewn.defaults.cit.types;

import schm.shsupercm.citresewn.api.CITTypeContainer;
import schm.shsupercm.citresewn.cit.*;
import schm.shsupercm.citresewn.defaults.cit.conditions.ConditionItems;
import schm.shsupercm.citresewn.pack.format.PropertyGroup;
import schm.shsupercm.citresewn.pack.format.PropertyKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TypeElytra extends CITType {
    //@Entrypoint(CITTypeContainer.ENTRYPOINT)
    public static final Container CONTAINER = new Container();

    public ResourceLocation texture;

    @Override
    public Set<PropertyKey> typeProperties() {
        return Set.of(PropertyKey.of("texture"));
    }

    @Override
    public void load(List<CITCondition> conditions, PropertyGroup properties, ResourceManager resourceManager) throws CITParsingException {
        for (CITCondition condition : conditions)
            if (condition instanceof ConditionItems items)
                for (Item item : items.items)
                    if (!(item instanceof ElytraItem))
                        warn("Non elytra item type condition", null, properties);

        texture = resolveAsset(properties.identifier, properties.getLastWithoutMetadata("citresewn", "texture"), "textures", ".png", resourceManager);
        if (texture == null)
            throw new CITParsingException("Texture not specified", properties, -1);
    }

    public static class Container extends CITTypeContainer<TypeElytra> {
        public Container() {
            super(TypeElytra.class, TypeElytra::new, "elytra");
        }

        public final List<Function<LivingEntity, ItemStack>> getItemInSlotCompatRedirects = new ArrayList<>();

        public Set<CIT<TypeElytra>> loaded = new HashSet<>();

        @Override
        public void load(List<CIT<TypeElytra>> parsedCITs) {
            loaded.addAll(parsedCITs);
        }

        @Override
        public void dispose() {
            loaded.clear();
        }

        public CIT<TypeElytra> getCIT(CITContext context) {
            return ((CITCacheElytra) (Object) context.stack).citresewn$getCacheTypeElytra().get(context).get();
        }

        public CIT<TypeElytra> getRealTimeCIT(CITContext context) {
            for (CIT<TypeElytra> cit : loaded)
                if (cit.test(context))
                    return cit;

            return null;
        }

        public ItemStack getVisualElytraItem(LivingEntity entity) {
            for (Function<LivingEntity, ItemStack> redirect : getItemInSlotCompatRedirects) {
                ItemStack stack = redirect.apply(entity);
                if (stack != null)
                    return stack;
            }

            return entity.getItemBySlot(EquipmentSlot.CHEST);
        }
    }

    public interface CITCacheElytra {
        CITCache.Single<TypeElytra> citresewn$getCacheTypeElytra();
    }
}
