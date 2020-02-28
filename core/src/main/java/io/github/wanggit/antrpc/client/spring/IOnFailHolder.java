package io.github.wanggit.antrpc.client.spring;

import java.lang.reflect.Method;
import java.util.Map;

public interface IOnFailHolder {

    Object doOnFail(Class clazz, Method method, Object[] args);

    void addOnFail(Class clazz, Object bean);

    Map<String, String> snapshot();
}
