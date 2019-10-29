package io.github.wanggit.antrpc.client.spring;

/** */
public interface BeanContainer {

    Object getOrCreateBean(Class clazz);
}
