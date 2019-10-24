package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.IAntrpcContext;

/** */
public interface BeanContainer {

    void setAntrpcContext(IAntrpcContext antrpcContext);

    Object getOrCreateBean(Class clazz);
}
