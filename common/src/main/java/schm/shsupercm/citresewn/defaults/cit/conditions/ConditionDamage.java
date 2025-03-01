package schm.shsupercm.citresewn.defaults.cit.conditions;

import schm.shsupercm.citresewn.api.CITConditionContainer;
import schm.shsupercm.citresewn.cit.CITCondition;
import schm.shsupercm.citresewn.cit.CITContext;
import schm.shsupercm.citresewn.cit.builtin.conditions.IntegerCondition;

import java.util.Set;

public class ConditionDamage extends IntegerCondition {
    //@Entrypoint(CITConditionContainer.ENTRYPOINT)
    public static final CITConditionContainer<ConditionDamage> CONTAINER = new CITConditionContainer<>(ConditionDamage.class, ConditionDamage::new,
            "damage");

    protected Integer mask = null;

    public ConditionDamage() {
        super(true, false, true);
    }

    @Override
    protected int getValue(CITContext context) {
        int value = context.stack.isDamageableItem() ? context.stack.getDamageValue() : 0;
        if (mask != null)
            value &= mask;
        return value;
    }

    @Override
    protected int getPercentageTotalValue(CITContext context) {
        return context.stack.isDamageableItem() ? context.stack.getMaxDamage() : 0;
    }

    @Override
    public Set<Class<? extends CITCondition>> siblingConditions() {
        return Set.of(ConditionDamageMask.class);
    }

    @Override
    public <T extends CITCondition> T modifySibling(T sibling) {
        if (sibling instanceof ConditionDamageMask damageMask)
            this.mask = damageMask.getMask();
        return null;
    }
}
