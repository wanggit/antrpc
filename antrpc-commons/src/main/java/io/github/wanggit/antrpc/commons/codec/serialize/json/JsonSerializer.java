package io.github.wanggit.antrpc.commons.codec.serialize.json;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializer;

import java.nio.charset.Charset;
import java.util.Map;

public class JsonSerializer implements ISerializer {

    public static final String TARGET_KEY = "target";

    private Class target;

    @Override
    public void setConfigs(Map<String, String> configs) {
        try {
            if (configs.containsKey(TARGET_KEY)) {
                target = Class.forName(configs.get(TARGET_KEY));
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("class not found.", e);
        }
    }

    @Override
    public void init() {}

    @Override
    public byte[] serialize(Object object) {
        String json = JSONObject.toJSONString(object);
        return json.getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public Object deserialize(byte[] buf) {
        String json = new String(buf, Charset.forName("UTF-8"));
        if (null == target) {
            return JSONObject.parseObject(json);
        } else {
            return JSONObject.parseObject(json, target);
        }
    }
}
