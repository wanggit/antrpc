package antrpc.client;

import org.junit.Assert;
import org.junit.Test;

public class ClassIsAssignableFromTest {

    @Test
    public void testIsAssignableFrom() {
        Assert.assertFalse(AImpl.class.isAssignableFrom(AInterface.class));
        Assert.assertTrue(AInterface.class.isAssignableFrom(AImpl.class));
    }

    interface AInterface {}

    private class AImpl implements AInterface {}
}
