package io.github.wanggit.antrpc.boot;

import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import lombok.extern.slf4j.Slf4j;
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

    private IAntrpcContext context;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        IConfiguration configuration = applicationContext.getBean(IConfiguration.class);
        context = new AntrpcContext(configuration);
        context.init((ConfigurableApplicationContext) applicationContext);
    }
}
