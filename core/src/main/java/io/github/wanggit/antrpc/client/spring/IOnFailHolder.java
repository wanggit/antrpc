package io.github.wanggit.antrpc.client.spring;

import java.lang.reflect.Method;

public interface IOnFailHolder {

    Object doOnFail(Class clazz, Method method, Object[] args);

    void addOnFail(Class clazz, Object bean);
}
