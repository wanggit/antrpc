package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.commons.annotations.OnRpcFail;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

public class OnFailProcessor implements BeanPostProcessor, ApplicationContextAware {

    private IAntrpcContext antrpcContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        antrpcContext = applicationContext.getBean(IAntrpcContext.class);
    }

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
            antrpcContext.getOnFailHolder().addOnFail(onRpcFail.clazz(), bean);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }
}
