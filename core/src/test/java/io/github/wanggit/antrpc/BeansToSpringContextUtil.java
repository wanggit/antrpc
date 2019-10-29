package io.github.wanggit.antrpc;

import io.github.wanggit.antrpc.client.spring.IOnFailProcessor;
import io.github.wanggit.antrpc.client.spring.IRpcAutowiredProcessor;
import io.github.wanggit.antrpc.client.spring.OnFailProcessor;
import io.github.wanggit.antrpc.client.spring.RpcAutowiredProcessor;
import io.github.wanggit.antrpc.client.zk.register.Register;
import io.github.wanggit.antrpc.client.zk.register.ZkRegister;
import org.springframework.context.support.GenericApplicationContext;

public abstract class BeansToSpringContextUtil {

    public static void toSpringContext(GenericApplicationContext applicationContext) {
        applicationContext
                .getBeanFactory()
                .registerSingleton(Register.class.getName(), new ZkRegister());
        applicationContext
                .getBeanFactory()
                .registerSingleton(IOnFailProcessor.class.getName(), new OnFailProcessor());
        applicationContext
                .getBeanFactory()
                .registerSingleton(
                        IRpcAutowiredProcessor.class.getName(), new RpcAutowiredProcessor());
    }
}
