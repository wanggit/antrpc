package io.github.wanggit.antrpc.server.telnet.handler.command.util;

import org.junit.Assert;
import org.junit.Test;

public class ArrayClassNameUtilTest {

    @Test
    public void replaceArrayClassName() {
        Assert.assertEquals(
                "User#getName([I)",
                ArrayClassNameUtil.replaceArrayClassName("User#getName(int[])"));
        Assert.assertEquals(
                "User#getName([Ljava.lang.Integer;)",
                ArrayClassNameUtil.replaceArrayClassName("User#getName(java.lang.Integer[])"));
        Assert.assertEquals(
                "User#getName([Ljava2.lang.Integer;)",
                ArrayClassNameUtil.replaceArrayClassName("User#getName(java2.lang.Integer[])"));
        Assert.assertEquals(
                "User#getName([Ljava.lang.Integer$DD;)",
                ArrayClassNameUtil.replaceArrayClassName("User#getName(java.lang.Integer$DD[])"));
    }
}
