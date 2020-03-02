package io.github.wanggit.antrpc.server.telnet.handler.command;

import org.junit.Test;

public class ArrayNameTest {

    @Test
    public void testArrayName() {
        System.out.println(byte[].class.getName());
        System.out.println(short[].class.getName());
        System.out.println(char[].class.getName());
        System.out.println(int[].class.getName());
        System.out.println(long[].class.getName());
        System.out.println(float[].class.getName());
        System.out.println(double[].class.getName());
        System.out.println(boolean[].class.getName());

        System.out.println(Byte[].class.getName());
        System.out.println(Short[].class.getName());
        System.out.println(Character[].class.getName());
        System.out.println(Integer[].class.getName());
        System.out.println(Long[].class.getName());
        System.out.println(Float[].class.getName());
        System.out.println(Double[].class.getName());
        System.out.println(String[].class.getName());
        System.out.println(ArrayNameTest[].class.getName());
    }
}
