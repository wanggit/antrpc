package io.github.wanggit.antrpc.boot;

import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.spring.IOnFailProcessor;
import io.github.wanggit.antrpc.client.spring.IRpcAutowiredProcessor;
import io.github.wanggit.antrpc.client.spring.OnFailProcessor;
import io.github.wanggit.antrpc.client.spring.RpcAutowiredProcessor;
import io.github.wanggit.antrpc.client.zk.register.IRegister;
import io.github.wanggit.antrpc.client.zk.register.ZkRegister;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public class AntrpcStater implements ApplicationRunner {

    private ApplicationContext applicationContext;

    AntrpcStater(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private static final String ANTRPC_CONTEXT_BEAN_NAME = "antrpcContext";
    private IAntrpcContext context;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ConfigurableListableBeanFactory beanFactory =
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        // 1
        createAntrpcInstance();
        // 2
        doAntRpcBeanAnnotationCheck(beanFactory);
        // 5
        doRegisterAntrpcContextToSpring(beanFactory);
        // 6
        doAntrpcContextInit();
    }

    private void doAntrpcContextInit() {
        context.init((ConfigurableApplicationContext) applicationContext);
    }

    private void doRegisterAntrpcContextToSpring(ConfigurableListableBeanFactory beanFactory) {
        if (!beanFactory.containsBean(ANTRPC_CONTEXT_BEAN_NAME)) {
            beanFactory.registerSingleton(ANTRPC_CONTEXT_BEAN_NAME, context);
        }
    }

    private void doAntRpcBeanAnnotationCheck(ConfigurableListableBeanFactory beanFactory) {
        IRpcAutowiredProcessor rpcAutowiredProcessor = context.getRpcAutowiredProcessor();
        IOnFailProcessor onFailProcessor = context.getOnFailProcessor();
        IRegister register = context.getRegister();
        String[] names = beanFactory.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = internalGetBean(beanFactory, name);
            if (null != bean) {
                rpcAutowiredProcessor.checkBeanHasRpcAutowire(bean);
                onFailProcessor.checkHasOnRpcFail(bean);
                register.checkHasRpcService(bean);
            }
        }
    }

    private Object internalGetBean(ConfigurableListableBeanFactory beanFactory, String name) {
        try {
            return beanFactory.getBean(name);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("This Bean cannot be found in the Spring container. name = " + name);
            }
            return null;
        }
    }

    private void createAntrpcInstance() {
        IConfiguration configuration = applicationContext.getBean(IConfiguration.class);
        context = new AntrpcContext(configuration);
        ((AntrpcContext) context).setRegister(new ZkRegister());
        ((AntrpcContext) context).setOnFailProcessor(new OnFailProcessor());
        ((AntrpcContext) context).setRpcAutowiredProcessor(new RpcAutowiredProcessor());
    }
}
