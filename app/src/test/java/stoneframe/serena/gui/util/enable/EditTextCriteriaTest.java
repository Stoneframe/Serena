package stoneframe.serena.gui.util.enable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EditTextCriteriaTest
{
    @Test
    public void isValidNonNegativeInteger_negativeValue_isFalse()
    {
        assertFalse(EditTextCriteria.isValidNonNegativeInteger("-1"));
    }

    @Test
    public void isValidNonNegativeInteger_zeroAndPositiveValue_areTrue()
    {
        assertTrue(EditTextCriteria.isValidNonNegativeInteger("0"));
        assertTrue(EditTextCriteria.isValidNonNegativeInteger("1"));
    }

    @Test
    public void isValidNonNegativeInteger_invalidValues_areFalse()
    {
        assertFalse(EditTextCriteria.isValidNonNegativeInteger(""));
        assertFalse(EditTextCriteria.isValidNonNegativeInteger("not a number"));
        assertFalse(EditTextCriteria.isValidNonNegativeInteger("2147483648"));
    }
}
