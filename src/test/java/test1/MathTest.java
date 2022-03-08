package test1;

import org.junit.Assert;
import org.junit.Test;

public class MathTest {

    @Test
    public void testAdd() {
        Math math = new Math();
        Assert.assertEquals(4, math.add(2, 2));
    }
}
