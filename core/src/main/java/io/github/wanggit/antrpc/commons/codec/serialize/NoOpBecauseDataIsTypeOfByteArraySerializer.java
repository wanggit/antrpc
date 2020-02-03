package io.github.wanggit.antrpc.commons.codec.serialize;

import java.util.Map;

public class NoOpBecauseDataIsTypeOfByteArraySerializer implements ISerializer {
    @Override
    public void setConfigs(Map<String, String> configs) {}

    @Override
    public void init() {}

    @Override
    public byte[] serialize(Object object) {
        return (byte[]) object;
    }

    @Override
    public Object deserialize(byte[] buf) {
        return buf;
    }
}
