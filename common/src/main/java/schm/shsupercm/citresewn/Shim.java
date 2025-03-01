package schm.shsupercm.citresewn;

import net.minecraft.resources.ResourceLocation;
import schm.shsupercm.citresewn.api.CITConditionContainer;
import schm.shsupercm.citresewn.api.CITDisposable;
import schm.shsupercm.citresewn.api.CITGlobalProperties;
import schm.shsupercm.citresewn.api.CITTypeContainer;
import schm.shsupercm.citresewn.cit.CITRegistry;
import schm.shsupercm.citresewn.defaults.cit.conditions.*;
import schm.shsupercm.citresewn.defaults.cit.types.TypeArmor;
import schm.shsupercm.citresewn.defaults.cit.types.TypeElytra;
import schm.shsupercm.citresewn.defaults.cit.types.TypeEnchantment;
import schm.shsupercm.citresewn.defaults.cit.types.TypeItem;
import schm.shsupercm.citresewn.pack.GlobalProperties;
import schm.shsupercm.citresewn.pack.format.PropertyKey;
import schm.shsupercm.citresewn.pack.format.PropertyValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Shim {
    public static final Map<ResourceLocation, CITTypeContainer<?>> TYPE = new HashMap<>();

    public static final Map<ResourceLocation, CITGlobalProperties> GLOBAL_PROPERTY = new HashMap<>();

    public static final Map<ResourceLocation, CITConditionContainer<?>> CONDITION = new HashMap<>();

    public static final Set<CITDisposable> DISPOSABLES = new HashSet<>();

    static {
        TYPE.put(CITResewn.id("armor"), TypeArmor.CONTAINER);
        TYPE.put(CITResewn.id("elytra"), TypeElytra.CONTAINER);
        TYPE.put(CITResewn.id("enchantment"), TypeEnchantment.CONTAINER);
        TYPE.put(CITResewn.id("item"), TypeItem.CONTAINER);

        CONDITION.put(CITResewn.id("component"), ConditionComponents.CONTAINER);
        CONDITION.put(CITResewn.id("damage"), ConditionDamage.CONTAINER);
        CONDITION.put(CITResewn.id("damage_mask"), ConditionDamageMask.CONTAINER);
        CONDITION.put(CITResewn.id("enchantment_levels"), ConditionEnchantmentLevels.CONTAINER);

        CONDITION.put(CITResewn.id("enchantments"), ConditionEnchantments.CONTAINER);
        CONDITION.put(CITResewn.id("hand"), ConditionHand.CONTAINER);
        CONDITION.put(CITResewn.id("items"), ConditionItems.CONTAINER);
        CONDITION.put(CITResewn.id("stack_size"), ConditionStackSize.CONTAINER);

        GLOBAL_PROPERTY.put(CITResewn.id("default"), TypeEnchantment.CONTAINER);

        DISPOSABLES.add(TypeArmor.CONTAINER);
        DISPOSABLES.add(TypeElytra.CONTAINER);
        DISPOSABLES.add(TypeItem.CONTAINER);
        DISPOSABLES.add(TypeEnchantment.CONTAINER);

    }

    public static void initTypes() {
        CITResewn.info("Registering CIT Types");
        TYPE.forEach((id, citTypeContainer) -> {
            CITRegistry.TYPES.put(id, citTypeContainer);
            CITRegistry.getTypeToId().putIfAbsent(citTypeContainer.createType.get().getClass(), id);
        });
    }

    public static void initConditions() {
        CONDITION.forEach((location, citConditionContainer) -> {
            for (String alias : citConditionContainer.aliases) {
                final PropertyKey key = new PropertyKey(location.getNamespace(), alias);

                CITRegistry.CONDITIONS.put(key, citConditionContainer);
                CITRegistry.getConditionToId().putIfAbsent(citConditionContainer.createCondition.get().getClass(), key);
            }
        });
    }

    public static void initGlobalProperties(Map<PropertyKey, Set<PropertyValue>> properties) {
        GLOBAL_PROPERTY.forEach((location, citGlobalProperties) -> {
            String containerNamespace = "citresewn";
            for (Map.Entry<PropertyKey, Set<PropertyValue>> entry : properties.entrySet())
                if (entry.getKey().namespace().equals(containerNamespace)) {
                    PropertyValue lastValue = null;
                    for (PropertyValue value : entry.getValue())
                        lastValue = value;

                    try {
                        citGlobalProperties.globalProperty(entry.getKey().path(), lastValue);
                    } catch (Exception e) {
                        CITResewn.logErrorLoading(lastValue == null ? "Errored while disposing global properties" : "Errored while parsing global properties: Line " + lastValue.position() + " of " + lastValue.propertiesIdentifier() + " in " + lastValue.packName());
                        e.printStackTrace();
                    }
                }
        });
    }

    public static void initDispose() {
        DISPOSABLES.forEach(CITDisposable::dispose);
    }

}
