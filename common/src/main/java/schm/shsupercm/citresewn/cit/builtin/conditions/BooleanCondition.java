package schm.shsupercm.citresewn.cit.builtin.conditions;

import schm.shsupercm.citresewn.cit.CITCondition;
import schm.shsupercm.citresewn.cit.CITContext;
import schm.shsupercm.citresewn.cit.CITParsingException;
import schm.shsupercm.citresewn.pack.format.PropertyGroup;
import schm.shsupercm.citresewn.pack.format.PropertyKey;
import schm.shsupercm.citresewn.pack.format.PropertyValue;

/**
 * Common condition parser for booleans.
 */
public abstract class BooleanCondition extends CITCondition {
	/**
     * Parsed boolean.
     */
    protected boolean value;

    /**
	 * Converts the given context to a boolean to compare the parsed value to.
     * @param context context to retrieve the compared value from
	 * @return the boolean value associated with the given context
     */
    protected boolean getValue(CITContext context) {
        throw new AssertionError("Not implemented by this condition");
    }

    @Override
    public void load(PropertyKey key, PropertyValue value, PropertyGroup properties) throws CITParsingException {
        if (value.value().equalsIgnoreCase("true"))
            this.value = true;
        else if (value.value().equalsIgnoreCase("false"))
            this.value = false;
        else
            throw new CITParsingException("Not a boolean", properties, value.position());
    }

    @Override
    public boolean test(CITContext context) {
        return getValue(context) == this.value;
    }
}
