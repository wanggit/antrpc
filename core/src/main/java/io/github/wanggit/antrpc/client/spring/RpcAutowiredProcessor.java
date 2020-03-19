package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.client.zk.zknode.IReportSubscriber;
import io.github.wanggit.antrpc.client.zk.zknode.SubscribeNode;
import io.github.wanggit.antrpc.commons.annotations.RpcAutowired;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.utils.ApplicationNameUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class RpcAutowiredProcessor implements IRpcAutowiredProcessor {

    private final List<InfoWrapper> infoWrappers = new ArrayList<>();

    @Override
    public void init(
            BeanContainer beanContainer,
            IReportSubscriber reportSubscriber,
            IConfiguration configuration) {
        boolean debugEnabled = log.isDebugEnabled();
        StringBuilder builder = new StringBuilder("RpcAutowired Details => ");
        for (InfoWrapper infoWrapper : infoWrappers) {
            Class<?> type = infoWrapper.getField().getType();
            SubscribeNode subscribeNode = new SubscribeNode();
            subscribeNode.setClassName(type.getName());
            subscribeNode.setHost(
                    ApplicationNameUtil.getApplicationName(
                            configuration.getExposeIp(),
                            configuration.getApplicationName(),
                            configuration.getPort()));
            subscribeNode.setTs(System.currentTimeMillis());
            reportSubscriber.report(subscribeNode);
            Object proxy = beanContainer.getOrCreateBean(type);
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

    @Override
    public Set<String> snapshot() {
        Set<String> set = new HashSet<>();
        for (InfoWrapper wrapper : infoWrappers) {
            set.add(wrapper.getField().getType().getName());
        }
        return set;
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
