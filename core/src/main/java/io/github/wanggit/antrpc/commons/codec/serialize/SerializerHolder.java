package io.github.wanggit.antrpc.commons.codec.serialize;

import io.github.wanggit.antrpc.commons.config.IConfiguration;

public class SerializerHolder implements ISerializerHolder {

    private final ISerializer serializer;

    public SerializerHolder(IConfiguration configuration)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String serializeType = configuration.getSerializeConfig().getType();
        serializer = (ISerializer) Class.forName(serializeType).newInstance();
        serializer.setConfigs(configuration.getSerializeConfig().getMap());
        serializer.init();
    }

    @Override
    public ISerializer getSerializer() {
        return serializer;
    }
}
