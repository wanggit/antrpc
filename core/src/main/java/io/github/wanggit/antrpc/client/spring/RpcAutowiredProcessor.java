package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.commons.annotations.RpcAutowired;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RpcAutowiredProcessor implements IRpcAutowiredProcessor, BeanPostProcessor {

    private final List<InfoWrapper> infoWrappers = new ArrayList<>();

    @Override
    public void init(BeanContainer beanContainer) {
        for (InfoWrapper infoWrapper : infoWrappers) {
            Object proxy = beanContainer.getOrCreateBean(infoWrapper.getField().getType());
            ReflectionUtils.setField(infoWrapper.getField(), infoWrapper.getBean(), proxy);
        }
        infoWrappers.clear();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        Field[] fields = FieldUtils.getFieldsWithAnnotation(bean.getClass(), RpcAutowired.class);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            ReflectionUtils.makeAccessible(field);
            infoWrappers.add(new InfoWrapper(bean, field));
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    static class InfoWrapper {
        private Object bean;
        private Field field;

        InfoWrapper(Object bean, Field field) {
            this.bean = bean;
            this.field = field;
        }

        public Object getBean() {
            return bean;
        }

        Field getField() {
            return field;
        }
    }
}
