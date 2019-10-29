package io.github.wanggit.antrpc.commons.config;

import io.github.wanggit.antrpc.commons.codec.serialize.kryo.KryoSerializer;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SerializeConfig {

    private String type = KryoSerializer.class.getName();

    private Map<String, String> map = new HashMap<>();
}
