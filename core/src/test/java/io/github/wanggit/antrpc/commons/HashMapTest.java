package io.github.wanggit.antrpc.commons;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class HashMapTest {

    @Test
    public void testClear() {
        Map<String, String> map1 = new HashMap<>();
        map1.put("Name", "1");
        map1.put("Age", "100");
        map1.put("Address", "xxxxxxxxxx");
        Map<String, String> map2 = new HashMap<>(map1);
        System.out.println(map2);
        map2.clear();
        System.out.println(map2);
        System.out.println(map1);
    }
}
