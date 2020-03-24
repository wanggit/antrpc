package io.github.wanggit.antrpc.commons.codec.serialize;

import io.github.wanggit.antrpc.commons.codec.serialize.json.JsonSerializer;
import io.github.wanggit.antrpc.commons.codec.serialize.kryo.KryoSerializer;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;

import java.util.HashMap;

public class SerializerFactory implements ISerializerFactory {

    private static final ISerializerFactory instance = new SerializerFactory();

    public static ISerializerFactory getInstance() {
        return instance;
    }

    @Override
    public ISerializer createNewSerializerByByteCmd(byte cmd) {
        ISerializer serializer = null;
        if (cmd == ConstantValues.KRYO_SERIALIZER) {
            serializer = new KryoSerializer();
        } else if (cmd == ConstantValues.JSON_SERIALIZER) {
            serializer = new JsonSerializer();
        }
        if (null != serializer) {
            serializer.setConfigs(new HashMap<>());
            serializer.init();
            return serializer;
        }
        return null;
    }
}
