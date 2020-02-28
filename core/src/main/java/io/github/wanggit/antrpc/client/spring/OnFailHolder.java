package io.github.wanggit.antrpc.client.spring;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OnFailHolder implements IOnFailHolder {

    private final ConcurrentHashMap<Class, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object doOnFail(Class clazz, Method method, Object[] args) {
        Object obj = cache.get(clazz);
        if (null == obj) {
            return null;
        }
        ReflectionUtils.makeAccessible(method);
        return ReflectionUtils.invokeMethod(method, obj, args);
    }

    @Override
    public void addOnFail(Class clazz, Object bean) {
        cache.put(clazz, bean);
    }

    @Override
    public Map<String, String> snapshot() {
        Map<String, String> map = new HashMap<>();
        cache.forEach(
                (key, value) -> {
                    map.put(key.getName(), value.getClass().getName());
                });
        return map;
    }
}
