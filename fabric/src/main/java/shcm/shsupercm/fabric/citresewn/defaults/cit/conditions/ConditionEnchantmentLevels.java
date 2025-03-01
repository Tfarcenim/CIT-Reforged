package shcm.shsupercm.fabric.citresewn.defaults.cit.conditions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import schm.shsupercm.citresewn.api.CITConditionContainer;
import schm.shsupercm.citresewn.cit.CITCondition;
import schm.shsupercm.citresewn.cit.CITContext;
import schm.shsupercm.citresewn.cit.builtin.conditions.IntegerCondition;
import schm.shsupercm.citresewn.cit.builtin.conditions.ListCondition;

public class ConditionEnchantmentLevels extends ListCondition<ConditionEnchantmentLevels.EnchantmentLevelCondition> {
    //@Entrypoint(CITConditionContainer.ENTRYPOINT)
    public static final CITConditionContainer<ConditionEnchantmentLevels> CONTAINER = new CITConditionContainer<>(ConditionEnchantmentLevels.class, ConditionEnchantmentLevels::new,
            "enchantment_levels", "enchantmentLevels");

    protected Set<ResourceLocation> enchantments = null;

    public ConditionEnchantmentLevels() {
        super(EnchantmentLevelCondition.class, EnchantmentLevelCondition::new);
    }

    @Override
    public Set<Class<? extends CITCondition>> siblingConditions() {
        return Set.of(ConditionEnchantments.class);
    }

    @Override
    public <T extends CITCondition> T modifySibling(T sibling) {
        if (sibling instanceof ConditionEnchantments conditionEnchantments) {
            if (enchantments == null) {
                enchantments = new HashSet<>();
                for (EnchantmentLevelCondition subCondition : this.conditions)
                    subCondition.enchantments = enchantments;
            }
            enchantments.addAll(Arrays.asList(conditionEnchantments.getEnchantments()));
        }

        return sibling;
    }

    protected static class EnchantmentLevelCondition extends IntegerCondition {
        protected Set<ResourceLocation> enchantments = null;

        protected EnchantmentLevelCondition() {
            super(true, false, false);
        }

        @Override
        public boolean test(CITContext context) {
            for (Map.Entry<ResourceLocation, Integer> entry : context.enchantments().entrySet())
                if ((enchantments == null || enchantments.contains(entry.getKey())) && entry.getValue() != null && (range ? min <= entry.getValue() && entry.getValue() <= max : entry.getValue() == min))
                    return true;

            return false;
        }
    }
}
