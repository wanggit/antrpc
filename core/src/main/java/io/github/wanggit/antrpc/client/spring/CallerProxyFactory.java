package io.github.wanggit.antrpc.client.spring;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public interface CallerProxyFactory {

    Object proxy(
            String name,
            Object bean,
            ConfigurableListableBeanFactory beanFactory,
            CallerProxyMethodInterceptor callerProxyMethodInterceptor);
}
