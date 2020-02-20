package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.commons.annotations.RpcAutowired;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RpcAutowiredProcessor implements IRpcAutowiredProcessor {

    private final List<InfoWrapper> infoWrappers = new ArrayList<>();

    @Override
    public void init(BeanContainer beanContainer) {
        boolean debugEnabled = log.isDebugEnabled();
        StringBuilder builder = new StringBuilder("RpcAutowired Details => ");
        for (InfoWrapper infoWrapper : infoWrappers) {
            Object proxy = beanContainer.getOrCreateBean(infoWrapper.getField().getType());
            ReflectionUtils.setField(infoWrapper.getField(), infoWrapper.getBean(), proxy);
            if (debugEnabled) {
                builder.append("\n bean=")
                        .append(infoWrapper.bean.getClass().getName())
                        .append(" field=")
                        .append(infoWrapper.field.getName());
            }
        }
        if (debugEnabled) {
            log.debug(builder.append("\n").toString());
        }
        infoWrappers.clear();
    }

    @Override
    public boolean checkBeanHasRpcAutowire(Object bean) {
        Field[] fields = FieldUtils.getFieldsWithAnnotation(bean.getClass(), RpcAutowired.class);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            ReflectionUtils.makeAccessible(field);
            infoWrappers.add(new InfoWrapper(bean, field));
        }
        return fields.length > 0;
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
