package io.github.wanggit.antrpc.commons.codec.serialize;

import java.util.Map;

public interface ISerializer {

    // 1
    void setConfigs(Map<String, String> configs);

    // 2
    void init();

    // 3
    byte[] serialize(Object object);

    // 3
    Object deserialize(byte[] buf);
}
