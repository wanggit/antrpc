package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.commons.annotations.OnRpcFail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class OnFailProcessor implements IOnFailProcessor {

    private final Map<Class, Object> cache = new ConcurrentHashMap<>();

    // 3
    @Override
    public void init(IOnFailHolder onFailHolder) {
        for (Map.Entry<Class, Object> entry : cache.entrySet()) {
            if (log.isDebugEnabled()) {
                log.debug(
                        entry.getValue()
                                + " is registered as a failed callback for "
                                + entry.getKey().getName()
                                + ".");
            }
            onFailHolder.addOnFail(entry.getKey(), entry.getValue());
        }
        cache.clear();
    }

    // 1
    @Override
    public void checkHasOnRpcFail(Object bean) {
        OnRpcFail onRpcFail = AnnotationUtils.findAnnotation(bean.getClass(), OnRpcFail.class);
        if (null != onRpcFail) {
            if (!onRpcFail.clazz().isAssignableFrom(bean.getClass())) {
                throw new BeanCreationException(
                        bean.getClass().getName()
                                + " must implement the "
                                + onRpcFail.clazz().getName()
                                + " interface.");
            }
            cache.put(onRpcFail.clazz(), bean);
        }
    }
}
