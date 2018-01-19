package com.quanql.test.webui.testcase;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestNGSimpleTest {
    @Test
    public void testAdd() {
        String str = "TestNG is working fine";
        Assert.assertEquals("TestNG is working fine", str);
    }
}
