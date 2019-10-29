package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.commons.annotations.OnRpcFail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class OnFailProcessor implements IOnFailProcessor, BeanPostProcessor {

    private final Map<Class, Object> cache = new ConcurrentHashMap<>();

    // 3
    @Override
    public void init(IOnFailHolder onFailHolder) {
        for (Map.Entry<Class, Object> entry : cache.entrySet()) {
            if (log.isInfoEnabled()) {
                log.info(
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
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
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
        return bean;
    }

    // 2
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }
}
