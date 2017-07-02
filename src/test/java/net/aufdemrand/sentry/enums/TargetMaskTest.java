package net.aufdemrand.sentry.enums;

import org.junit.Assert;
import org.junit.Test;

public class TargetMaskTest {
    @Test
    public void testSingle() throws Exception {
        final int flag = 0x1; // TargetMask.ALL = 0x1
        final boolean test = TargetMask.test(flag, TargetMask.ALL);
        Assert.assertTrue(test);
    }

    @Test
    public void testOnly() throws Exception {
        final int flag = 0x1; // (TargetMask.PLAYERS = 0x2) + (TargetMask.ALL = 0x1) = 0x3
        final boolean test = TargetMask.testOnly(flag, TargetMask.ALL);
        Assert.assertTrue(test);

        final int flag1 = 0x3; // TargetMask.ALL = 0x1
        final boolean test1 = TargetMask.testOnly(flag1, TargetMask.ALL);
        Assert.assertFalse(test1);
    }

    @Test
    public void setSingle() throws Exception {
        final int flag = 0x0; //0x0 = No flags on
        final int result = TargetMask.set(flag, TargetMask.ALL);
        Assert.assertEquals(0x1, result); // TargetMask.ALL = 0x1
    }
    @Test
    public void setMultiple() throws Exception {
        final int flag = 0x0; //0x0 = No flags on
        final int result = TargetMask.set(flag, TargetMask.ALL, TargetMask.PLAYERS);
        Assert.assertEquals(0x3, result); // (TargetMask.PLAYERS = 0x2) + (TargetMask.ALL = 0x1) = 0x3
    }
    @Test
    public void resetOne() throws Exception {
        final int flag = 0x1; // TargetMask.ALL = 0x1
        final int result = TargetMask.reset(flag, TargetMask.ALL);
        Assert.assertEquals(0x0, result); //0x0 = No flags on
    }
    @Test
    public void resetMultiple() throws Exception {
        final int flagAll = 0x3; // (TargetMask.PLAYERS = 0x2) + (TargetMask.ALL = 0x1) = 0x3
        final int flagOne = 0x3; // (TargetMask.PLAYERS = 0x2) + (TargetMask.ALL = 0x1) = 0x3
        final int resultResetAll = TargetMask.reset(flagAll, TargetMask.ALL, TargetMask.PLAYERS);
        final int resultResetOne = TargetMask.reset(flagOne, TargetMask.PLAYERS);
        Assert.assertEquals(0x0, resultResetAll); //0x0 = No flags on
        Assert.assertEquals(0x1, resultResetOne); // TargetMask.ALL = 0x1
    }
    @Test
    public void flipOne() throws Exception {
        final int flagOn = 0x1; // TargetMask.ALL = 0x1
        final int resultFlippingOff = TargetMask.flip(flagOn, TargetMask.ALL);
        Assert.assertEquals(0x0, resultFlippingOff); //0x0 = No flags on

        final int flagOff = 0x0; //0x0 = No flags on
        final int resultFlippingOn = TargetMask.flip(flagOff, TargetMask.ALL);
        Assert.assertEquals(0x1, resultFlippingOn); // TargetMask.ALL = 0x1
    }
    @Test
    public void flipMultiple() throws Exception {
        //Flip off both ALL and PLAYERS flag
        final int flagOnAll = 0x3; // (TargetMask.PLAYERS = 0x2) + (TargetMask.ALL = 0x1) = 0x3
        final int resultFlippingOffAll = TargetMask.flip(flagOnAll, TargetMask.ALL, TargetMask.PLAYERS);
        Assert.assertEquals(0x0, resultFlippingOffAll); //0x0 = No flags on

        //Flip on both ALL and PLAYERS flag
        final int flagOffAll = 0x0; //0x0 = No flags on
        final int resultFlippingOnAll = TargetMask.flip(flagOffAll, TargetMask.ALL, TargetMask.PLAYERS);
        Assert.assertEquals(0x3, resultFlippingOnAll); // (TargetMask.PLAYERS = 0x2) + (TargetMask.ALL = 0x1) = 0x3

        //Flip off ALL, leaving PLAYERS on
        final int flagOnSome = 0x3; // (TargetMask.PLAYERS = 0x2) + (TargetMask.ALL = 0x1) = 0x3
        final int resultFlippingOffSome = TargetMask.flip(flagOnSome, TargetMask.ALL);
        Assert.assertEquals(0x2, resultFlippingOffSome); // TargetMask.PLAYERS = 0x2

        //Flip on ALL
        final int flagOffOne = 0x0; //0x0 = No flags on
        final int resultFlippingOnSome = TargetMask.flip(flagOffOne, TargetMask.ALL);
        Assert.assertEquals(0x1, resultFlippingOnSome); // TargetMask.ALL = 0x1
    }
}