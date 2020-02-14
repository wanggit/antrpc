package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.commons.annotations.RpcAutowired;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cglib.proxy.Enhancer;

import java.lang.reflect.Field;

@Slf4j
public class DefaultCallerProxyFactory implements CallerProxyFactory {
    @Override
    public Object proxy(
            String name,
            Object bean,
            ConfigurableListableBeanFactory beanFactory,
            CallerProxyMethodInterceptor callerProxyMethodInterceptor) {
        Field[] fields = FieldUtils.getFieldsWithAnnotation(bean.getClass(), RpcAutowired.class);
        if (fields.length > 0) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(bean.getClass());
            enhancer.setCallback(callerProxyMethodInterceptor);
            Object proxyedBean = enhancer.create();
            BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;
            beanDefinitionRegistry.removeBeanDefinition(name);
            beanFactory.registerSingleton(name, proxyedBean);
            return proxyedBean;
        }
        return bean;
    }
}
