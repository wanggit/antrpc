package io.github.wanggit.antrpc.commons;

import io.github.wanggit.antrpc.commons.codec.serialize.ISerializer;
import io.github.wanggit.antrpc.commons.codec.serialize.json.JsonSerializer;
import org.junit.Test;

public class HashMapTest {

    @Test
    public void testClear() {
        JsonSerializer jsonSerializer = new JsonSerializer();
        System.out.println(ISerializer.class.isInstance(jsonSerializer));
    }
}
