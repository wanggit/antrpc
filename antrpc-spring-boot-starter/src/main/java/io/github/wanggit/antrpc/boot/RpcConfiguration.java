package io.github.wanggit.antrpc.boot;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import io.github.wanggit.antrpc.client.spring.OnFailProcessor;
import io.github.wanggit.antrpc.client.spring.RpcAutowiredProcessor;
import io.github.wanggit.antrpc.client.zk.listener.ZkListener;
import io.github.wanggit.antrpc.client.zk.register.ZkRegister;
import io.github.wanggit.antrpc.client.zk.zknode.ZkNodeKeeper;
import io.github.wanggit.antrpc.commons.config.MetricsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

@Slf4j
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class RpcConfiguration implements ServletContextInitializer {

    @Autowired private RpcProperties rpcProperties;

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

    @Bean
    public ZkListener zkListener() {
        return new ZkListener();
    }

    @Bean
    public ZkNodeKeeper zkNodeKeeper() {
        return new ZkNodeKeeper();
    }

    @Bean
    @ConditionalOnProperty(prefix = "antrpc.metrics-config", name = "enable", havingValue = "true")
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    @ConditionalOnProperty(prefix = "antrpc.metrics-config", name = "enable", havingValue = "true")
    public HealthCheckRegistry healthCheckRegistry() {
        return new HealthCheckRegistry();
    }

    @Override
    public void onStartup(javax.servlet.ServletContext servletContext) throws ServletException {
        MetricsConfig metricsConfig = rpcProperties.getMetricsConfig();
        if (null != metricsConfig && metricsConfig.isEnable() && metricsConfig.isEnableServlets()) {
            servletContext.setAttribute(
                    HealthCheckServlet.HEALTH_CHECK_REGISTRY, healthCheckRegistry());
            servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry());
            ServletRegistration.Dynamic metricsServlet =
                    servletContext.addServlet("metricsServlet", new AdminServlet());
            metricsServlet.addMapping("/management/metrics/*");
            metricsServlet.setAsyncSupported(true);
            metricsServlet.setLoadOnStartup(2);
        }
    }
}
