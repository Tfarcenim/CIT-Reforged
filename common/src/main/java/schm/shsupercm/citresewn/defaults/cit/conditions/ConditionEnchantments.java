package schm.shsupercm.citresewn.defaults.cit.conditions;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import schm.shsupercm.citresewn.api.CITConditionContainer;
import schm.shsupercm.citresewn.cit.CITCondition;
import schm.shsupercm.citresewn.cit.CITContext;
import schm.shsupercm.citresewn.cit.builtin.conditions.IdentifierCondition;
import schm.shsupercm.citresewn.cit.builtin.conditions.ListCondition;

public class ConditionEnchantments extends ListCondition<ConditionEnchantments.EnchantmentCondition> {
    //@Entrypoint(CITConditionContainer.ENTRYPOINT)
    public static final CITConditionContainer<ConditionEnchantments> CONTAINER = new CITConditionContainer<>(ConditionEnchantments.class, ConditionEnchantments::new,
            "enchantments", "enchantmentIDs");

    public ConditionEnchantments() {
        super(EnchantmentCondition.class, EnchantmentCondition::new);
    }

    public ResourceLocation[] getEnchantments() {
        ResourceLocation[] enchantments = new ResourceLocation[this.conditions.length];

        for (int i = 0; i < this.conditions.length; i++)
            enchantments[i] = this.conditions[i].getValue(null);

        return enchantments;
    }

    @Override
    public Set<Class<? extends CITCondition>> siblingConditions() {
        return Set.of(ConditionEnchantmentLevels.class);
    }

    protected static class EnchantmentCondition extends IdentifierCondition {
        @Override
        public boolean test(CITContext context) {
            return context.enchantments().containsKey(this.value);
        }

        @Override
        protected ResourceLocation getValue(CITContext context) {
            return this.value;
        }
    }
}
