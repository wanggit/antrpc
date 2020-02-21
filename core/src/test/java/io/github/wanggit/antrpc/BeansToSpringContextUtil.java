package io.github.wanggit.antrpc;

import org.springframework.context.support.GenericApplicationContext;

public abstract class BeansToSpringContextUtil {

    public static void toSpringContext(GenericApplicationContext applicationContext) {
        /*applicationContext
                .getBeanFactory()
                .registerSingleton(IRegister.class.getName(), new ZkRegister());
        applicationContext
                .getBeanFactory()
                .registerSingleton(IOnFailProcessor.class.getName(), new OnFailProcessor());
        applicationContext
                .getBeanFactory()
                .registerSingleton(
                        IRpcAutowiredProcessor.class.getName(), new RpcAutowiredProcessor());*/
    }
}
