package schm.shsupercm.citresewn.cit;

import net.minecraft.resources.ResourceLocation;
import schm.shsupercm.citresewn.CITResewn;
import schm.shsupercm.citresewn.api.CITConditionContainer;
import schm.shsupercm.citresewn.api.CITTypeContainer;
import schm.shsupercm.citresewn.cit.builtin.conditions.ConstantCondition;
import schm.shsupercm.citresewn.pack.format.PropertyGroup;
import schm.shsupercm.citresewn.pack.format.PropertyKey;
import schm.shsupercm.citresewn.pack.format.PropertyValue;
import schm.shsupercm.citresewn.platform.Services;

import java.util.*;

/**
 * Holds a static registry runtime for all types and conditions.
 * @see PackParser
 * @see CITTypeContainer
 * @see CITConditionContainer
 */
public final class CITRegistry { private CITRegistry(){}
    /**
     * Currently registered CIT types.
     */
    public static final Map<ResourceLocation, CITTypeContainer<? extends CITType>> TYPES = new HashMap<>();
    /**
     * Currently registered condition types.
     */
    public static final Map<PropertyKey, CITConditionContainer<? extends CITCondition>> CONDITIONS = new HashMap<>();

    /**
     * Fast id lookup map for types.
     * @see #idOfType(Class)
     */
    private static final Map<Class<? extends CITType>, ResourceLocation> TYPE_TO_ID = new IdentityHashMap<>();
    /**
     * Fast id lookup map for conditions.
     * @see #idOfCondition(Class)
     */
    private static final Map<Class<? extends CITCondition>, PropertyKey> CONDITION_TO_ID = new IdentityHashMap<>();

    /**
     * Loads all available CIT and condition types to registry. (internal use only)
     * @see CITTypeContainer
     * @see CITConditionContainer
     */
    public static void registerAll() {
        CITResewn.LOG.info("Registering CIT Conditions");

        Services.PLATFORM.registerCITTypes();
    }

    public static Map<Class<? extends CITType>, ResourceLocation> getTypeToId() {
        return TYPE_TO_ID;
    }

    public static Map<Class<? extends CITCondition>, PropertyKey> getConditionToId() {
        return CONDITION_TO_ID;
    }

    /**
     * Parses a condition from the given property.<br>
     *
     * @param key the condition's key in the group
     * @param value the condition's value
     * @param properties the containing property group
     * @return the parsed condition or an always-failing {@link ConstantCondition} if unrecognized
     * @throws CITParsingException if errored while parsing hte condition
     */
    public static CITCondition parseCondition(PropertyKey key, PropertyValue value, PropertyGroup properties) throws CITParsingException {
        CITConditionContainer<? extends CITCondition> conditionContainer = CONDITIONS.get(key);
        if (conditionContainer == null) {
            CITResewn.logWarnLoading(properties.messageWithDescriptorOf("Unknown condition type \"" + key.toString() + "\"", value.position()));
            return ConstantCondition.FALSE;
        }

        CITCondition condition = conditionContainer.createCondition.get();
        condition.load(key, value, properties);
        return condition;
    }

    /**
     * Parses a CIT type from the given property group.<br>
     * If the group does not contain a "citresewn:type" property, defaults to "citresewn:item".
     * @param properties group of properties to parse the CIT type from
     * @return a new instance of the group's CIT type
     * @throws UnknownCITTypeException if the given type is unrecognized in the registry
     */
    public static CITType parseType(PropertyGroup properties) throws UnknownCITTypeException {
        ResourceLocation type = ResourceLocation.fromNamespaceAndPath("citresewn", "item");

        PropertyValue propertiesType = properties.getLastWithoutMetadata("citresewn", "type");
        if (propertiesType != null) {
            String value = propertiesType.value();
            if (!value.contains(":"))
                value = "citresewn:" + value;
            type = ResourceLocation.tryParse(value);
        }

        CITTypeContainer<? extends CITType> typeContainer = TYPES.get(type);
        if (typeContainer == null)
            // assert (propertiesType != null) because the default citresewn:item should always be registered
            throw new UnknownCITTypeException(properties, propertiesType == null ? -1 : propertiesType.position());

        return typeContainer.createType.get();
    }

    /**
     * @see #TYPE_TO_ID
     * @return the id of the given CIT type's class.
     */
    public static ResourceLocation idOfType(Class<? extends CITType> clazz) {
        return TYPE_TO_ID.get(clazz);
    }

    /**
     * @see #CONDITION_TO_ID
     * @return the first key of the given condition's class.
     */
    public static PropertyKey idOfCondition(Class<? extends CITCondition> clazz) {
        return CONDITION_TO_ID.get(clazz);
    }
}
