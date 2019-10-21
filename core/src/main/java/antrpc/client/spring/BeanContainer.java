package antrpc.client.spring;

import antrpc.IAntrpcContext;

/** */
public interface BeanContainer {

    void setAntrpcContext(IAntrpcContext antrpcContext);

    Object getOrCreateBean(Class clazz);
}
