package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.commons.annotations.RpcAutowired;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class RpcAutowiredProcessor implements BeanPostProcessor, ApplicationContextAware {

    private IAntrpcContext antrpcContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        Field[] fields = FieldUtils.getFieldsWithAnnotation(bean.getClass(), RpcAutowired.class);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            ReflectionUtils.makeAccessible(field);
            Class<?> clazz = field.getType();
            Object proxy = antrpcContext.getBeanContainer().getOrCreateBean(clazz);
            ReflectionUtils.setField(field, bean, proxy);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        antrpcContext = applicationContext.getBean(IAntrpcContext.class);
    }
}
