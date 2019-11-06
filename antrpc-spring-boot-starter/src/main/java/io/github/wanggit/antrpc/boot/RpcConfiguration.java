package io.github.wanggit.antrpc.boot;

import io.github.wanggit.antrpc.client.spring.OnFailProcessor;
import io.github.wanggit.antrpc.client.spring.RpcAutowiredProcessor;
import io.github.wanggit.antrpc.client.zk.register.ZkRegister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RpcConfiguration {

    @Bean
    public RpcAutowiredProcessor rpcAutowiredProcessor() {
        return new RpcAutowiredProcessor();
    }

    @Bean
    public OnFailProcessor onFailProcessor() {
        return new OnFailProcessor();
    }

    @Bean
    public ZkRegister zkRegister() {
        return new ZkRegister();
    }
}
