package shcm.shsupercm.fabric.citresewn.defaults.cit.conditions;

import schm.shsupercm.citresewn.api.CITConditionContainer;
import schm.shsupercm.citresewn.cit.CITContext;
import schm.shsupercm.citresewn.cit.builtin.conditions.IntegerCondition;

public class ConditionStackSize extends IntegerCondition {
   // @Entrypoint(CITConditionContainer.ENTRYPOINT)
    public static final CITConditionContainer<ConditionStackSize> CONTAINER = new CITConditionContainer<>(ConditionStackSize.class, ConditionStackSize::new,
            "stack_size", "stackSize", "amount");

    public ConditionStackSize() {
        super(true, false, false);
    }

    @Override
    protected int getValue(CITContext context) {
        return context.stack.getCount();
    }
}
